package ee.ctob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ctob.GuessGameProperties;
import ee.ctob.data.Bet;
import ee.ctob.data.Player;
import ee.ctob.data.Result;
import ee.ctob.data.Winner;
import ee.ctob.websocket.config.WebSocketProperties;
import ee.ctob.websocket.data.Reason;
import ee.ctob.websocket.data.Request;
import ee.ctob.websocket.data.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static ee.ctob.data.enums.BetResult.LOSE;
import static ee.ctob.data.enums.BetResult.WIN;
import static ee.ctob.websocket.data.Reason.TIMEOUT;
import static org.springframework.web.socket.CloseStatus.SESSION_NOT_RELIABLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService extends GameRoomService {

    private final GuessGameProperties gameProperties;
    private final WebSocketProperties webSocketProperties;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean active = true;
    private List<Bet> currentBets = new CopyOnWriteArrayList<>();
    private final Map<WebSocketSession, Player> players = new ConcurrentHashMap<>();


    public void playerAdd(Player player) {
        players.put(player.getSession(), player);
        active = true;

        log.info("Player joined () -> {} ", player.getNickname());
    }

    public void playerRemove(WebSocketSession session, Reason reason) {
        Player player = players.remove(session);
        currentBets.removeIf(bet -> bet.getPlayer().getSession().equals(session));

        log.info("Player removed {} , reason {}" , player.getNickname(), reason);

        checkPlayers();
    }

    public void playerBet(Request request, WebSocketSession session) {
        Player player = players.get(session);
        if(!player.getNickname().equals(request.getNickname()) && request.getNickname() != null) {
            player.setNickname(request.getNickname());
        }
        player.setLastActivity(System.currentTimeMillis());

        int individualBetsCount = 0;
        for(Bet bet : currentBets) {
            if(bet.getPlayer().getSession().equals(session)) {
                individualBetsCount++;
            }
            if(individualBetsCount >= gameProperties.getMaxIndividualBets()) {
                sendMessage(new TextMessage("BETS_LIMIT_ERROR"), session);
                return;
            }
        }

        log.info("Player {} bet {} on number {}", player.getNickname(), request.getAmount(), request.getNumber());

        currentBets.add(
                Bet.builder()
                        .player(player)
                        .number(request.getNumber())
                        .amount(request.getAmount())
                        .build());
    }

    public void gameStartLoop() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (!active || checkPlayers()) return;
            playRound();
        }, 0, gameProperties.getRoundSeconds(), TimeUnit.SECONDS);
    }

    private void playRound() {
        connectionsTimeout();
        if(currentBets.isEmpty()) {
            return;
        }
        try {
            log.info("Round started");

            List<Winner> winners = new ArrayList<>();

            Thread.sleep(TimeUnit.SECONDS.toMillis(gameProperties.getRoundSeconds()));

            int winningNumber = new Random().nextInt(10) + 1;
            log.info("Winning number {} ",  winningNumber);
            for (Bet bet : currentBets) {
                if(!bet.getPlayer().getSession().isOpen()) {
                    continue;
                }

                Result.ResultBuilder result = Result.builder();
                result.betNumber(bet.getNumber())
                        .betAmount(bet.getAmount())
                        .winNumber(winningNumber);

                if (bet.getNumber() == winningNumber) {
                    result.betResult(WIN);
                    double winAmount = BigDecimal.valueOf(bet.getAmount() * gameProperties.getPayoutMultiplier())
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    result.winAmount(winAmount);

                    winners.add(
                            Winner.builder()
                                    .nickname(bet.getPlayer().getNickname())
                                    .winAmount(winAmount)
                                    .build());
                } else {
                    result.betResult(LOSE);
                }

                sendMessage(buildResponse(result.build()), bet.getPlayer().getSession());
            }

            if(!winners.isEmpty()) {
                log.info("Players won {} ", winners);
                broadcastAll(new TextMessage(objectMapper.writeValueAsString(winners)));
            }

            currentBets.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectionsTimeout() {
        long now = System.currentTimeMillis();
        for(Player player : players.values()) {
            if(now - player.getLastActivity() > TimeUnit.SECONDS.toMillis(webSocketProperties.getTimeout())) {
                try {
                    WebSocketSession session = player.getSession();
                    sendMessage(new TextMessage("SESSION_TIMEOUT"), session);
                    session.close(SESSION_NOT_RELIABLE);
                    playerRemove(session, TIMEOUT);
                    log.info("Session timeout {}", player.getNickname());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean checkPlayers() {
        if (players.isEmpty()) {
            active = false;
            log.info("No players");
            return true;
        }
        return false;
    }

    private Response buildResponse(Result result) {
        Response.ResponseBuilder response = Response.builder()
                .betResult(result.getBetResult())
                .betNumber(result.getBetNumber())
                .betAmount(result.getBetAmount())
                .winNumber(result.getWinNumber());

        if(result.getBetResult().equals(LOSE)) {
            return response.build();
        }

        return response.winAmount(result.getWinAmount()).build();
    }

    private void broadcastAll(TextMessage textMessage) {
        for (Player player : players.values()) {
            sendMessage(textMessage, player.getSession());
        }
    }

    @SneakyThrows
    private void sendMessage(Response response, WebSocketSession session) {
        log.info("Send response {} ", response);
        sendMessage(new TextMessage(objectMapper.writeValueAsString(response)), session);
    }

    private void sendMessage(TextMessage textMessage, WebSocketSession session) {
        try {
            if(session.isOpen()) {
                session.sendMessage(textMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
}
