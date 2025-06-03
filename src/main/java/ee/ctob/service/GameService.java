package ee.ctob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ctob.GuessGameProperties;
import ee.ctob.data.Bet;
import ee.ctob.data.Player;
import ee.ctob.data.Result;
import ee.ctob.data.Winner;
import ee.ctob.websocket.data.Request;
import ee.ctob.websocket.data.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static ee.ctob.data.enums.BetResult.LOSE;
import static ee.ctob.data.enums.BetResult.WIN;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean active = true;
    private List<Bet> currentBets = new CopyOnWriteArrayList<>();
    private final Map<WebSocketSession, Player> players = new ConcurrentHashMap<>();

    private final GuessGameProperties properties;

    public void closeSession() {

    }

    public void playerAdd(WebSocketSession session, Player player) {
        players.put(session, player);
        active = true;
        log.info("Player joined () -> {} ", player.getNickname());
    }

    public void playerRemove(WebSocketSession session) {
        Player player = players.remove(session);
        log.info("Player left {}" , player.getNickname());
        checkPlayers();
    }

    public void playerBet(Request request, WebSocketSession session) {

        if(players.get(session).getNickname() != request.getNickname() && request.getNickname() != null) {
            players.remove(session);
            players.put(session, new Player(session, request.getNickname()));
        }

        log.info("Player {} bet {} on number {}", players.get(session).getNickname(), request.getAmount(), request.getNumber());
        currentBets.add(
                Bet.builder()
                        .player(players.get(session))
                        .number(request.getNumber())
                        .amount(request.getAmount())
                        .build());
    }

    public void gameStartLoop() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (!active || checkPlayers()) return;
            playRound();
        }, 0, properties.getRoundSeconds(), TimeUnit.SECONDS);
    }

    private boolean checkPlayers() {
        if (players.isEmpty()) {
            active = false;
            log.info("No players");
            return true;
        }
        return false;
    }

    private void playRound() {
        try {
            log.info("Round started");

            List<Winner> winners = new ArrayList<>();

            Thread.sleep(properties.getRoundSeconds()*1000);

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
                    double winAmount = bet.getAmount() * properties.getPayoutMultiplier();
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


    private final ObjectMapper objectMapper = new ObjectMapper();
}
