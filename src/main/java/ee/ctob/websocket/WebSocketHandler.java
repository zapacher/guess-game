package ee.ctob.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ctob.GuessGameProperties;
import ee.ctob.data.Player;
import ee.ctob.service.GameService;
import ee.ctob.websocket.data.Request;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;

import static ee.ctob.websocket.data.EnumMessage.BAD_REQUEST;
import static ee.ctob.websocket.data.EnumMessage.DISCONNECTED;

@Component
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
        Player player = new Player(session, System.currentTimeMillis());
        gameService.playerAdd(player);
        sendMessage(new TextMessage(player.getValidationUUID().toString()), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        gameService.playerRemove(session, DISCONNECTED);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Request request;
        try {
            request = objectMapper.readValue(message.getPayload(), Request.class);
            validateRequest(request);
            gameService.validatePlayer(request.getValidationUUID(), session);
        } catch (Exception e) {
            sendMessage(new TextMessage(BAD_REQUEST.name()), session);
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

    private void validateRequest(Request request) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Request>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            for (ConstraintViolation<Request> violation : violations) {
                System.out.println(violation.getPropertyPath() + ": " + violation.getMessage());
            }
            throw new RuntimeException();
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
}
