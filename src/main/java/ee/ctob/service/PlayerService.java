//package ee.ctob.service;
//
//import ee.ctob.data.Bet;
//import ee.ctob.data.Player;
//import ee.ctob.websocket.config.WebSocketProperties;
//import ee.ctob.websocket.data.EnumMessage;
//import ee.ctob.websocket.data.Request;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//import static ee.ctob.websocket.data.EnumMessage.BETS_LIMIT;
//import static ee.ctob.websocket.data.EnumMessage.BET_ACCEPTED;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class PlayerService {
//
//    private final WebSocketProperties webSocketProperties;
//    private final GameService gameService;
//
//    private final Map<WebSocketSession, Player> players = new ConcurrentHashMap<>();
//    private List<Bet> currentBets = new CopyOnWriteArrayList<>();
//
//    public void playerAdd(Player player) {
//        players.put(player.getSession(), player);
//
//        log.info("Player joined () -> {} ", player.getNickname());
//    }
//
//    public void playerRemove(WebSocketSession session, EnumMessage enumMessage) {
//        Player player = players.remove(session);
//        currentBets.removeIf(bet -> bet.getPlayer().getSession().equals(session));
//
//        log.info("Player removed {} , reason {}" , player.getNickname(), enumMessage);
//
//        checkPlayers();
//    }
//
//    public void validatePlayer(UUID validationUUID, WebSocketSession session) {
//        if(players.get(session).getValidationUUID().equals(validationUUID)) {
//            return;
//        }
//        throw new RuntimeException();
//    }
//    private boolean checkPlayers() {
//        if (players.isEmpty()) {
//            log.info("No players");
//            return true;
//        }
//        return false;
//    }
//
//    public void playerBet(Request request, WebSocketSession session) {
//        Player player = players.get(session);
//
//        if(!player.getNickname().equals(request.getNickname()) && request.getNickname() != null) {
//            player.setNickname(request.getNickname());
//        }
//        player.setLastActivity(System.currentTimeMillis());
//
//        int individualBetsCount = 0;
//        for(Bet bet : currentBets) {
//            if(bet.getPlayer().getSession().equals(session)) {
//                individualBetsCount++;
//            }
//            if(individualBetsCount >= gameProperties.getMaxIndividualBets()) {
//                sendMessage(new TextMessage(BETS_LIMIT.name()), session);
//                return;
//            }
//        }
//
//        log.info("Player {} bet {} on number {}", player.getNickname(), request.getAmount(), request.getNumber());
//
//        currentBets.add(
//                Bet.builder()
//                        .player(player)
//                        .number(request.getNumber())
//                        .amount(request.getAmount())
//                        .build());
//
//        sendMessage(new TextMessage(BET_ACCEPTED.name()), session);
//    }
//
//    public void startRound() {
//        gameService.setBets(currentBets);
//        gameService.gameStartLoop();
//    }
//}
