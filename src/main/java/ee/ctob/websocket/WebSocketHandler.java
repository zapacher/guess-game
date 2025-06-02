package ee.ctob.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ctob.GuessGameProperties;
import ee.ctob.service.GameService;
import ee.ctob.data.Player;
import ee.ctob.websocket.data.Request;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Controller
public class WebSocketHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final GuessGameProperties properties;

    public WebSocketHandler(GameService gameService, GuessGameProperties properties) {
        this.gameService = gameService;
        this.properties = properties;
        gameService.gameStartLoop();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String nickname = "Player_" + UUID.randomUUID();
        gameService.playerAdd(session, new Player(session, nickname));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        gameService.playerRemove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Request request;
        try {
            request = objectMapper.readValue(message.getPayload(), Request.class);
            validateBet(request);
        } catch (Exception e) {
            sendMessage(new TextMessage("Invalid message format"), session);
            return;
        }
        gameService.playerBet(request, session);
    }


    private void sendMessage(TextMessage textMessage, WebSocketSession session) {
        try {
            session.sendMessage(textMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateBet(Request request) {
        if(request.getNumber() > properties.getMaxBetNumber() || request.getNumber()< properties.getMinBetNumber()) {
            throw new RuntimeException("BadRequest");
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
}
