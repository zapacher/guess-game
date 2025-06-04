package ee.ctob.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ctob.websocket.data.EnumMessage;
import ee.ctob.websocket.data.Request;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static ee.ctob.websocket.data.EnumMessage.*;
import static ee.ctob.websocket.testutils.ObjectCreator.nonValidRequests;
import static ee.ctob.websocket.testutils.ObjectCreator.validRequests;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestUnitWebSocketHandler {

    @LocalServerPort
    private int port;

    private static BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    private String response;
    private UUID validationUUID;
    private WebSocketSession session;

    @BeforeEach
    public void setup() {
        messages = new LinkedBlockingQueue<>();
    }

    @BeforeAll
    public void setConnection() throws ExecutionException, InterruptedException {
        StandardWebSocketClient client = new StandardWebSocketClient();
        session = client.doHandshake(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.offer(message.getPayload());
            }
        }, "ws://localhost:" + port + "/game/guess").get();
        response = messages.poll(15, TimeUnit.SECONDS);
        validationUUID = UUID.fromString(response);
    }

    @Test
    void testPlayerAddedAndRemovedOnConnectionEvents() {
        assertAll("assert handshake",
                ()-> assertNotNull(response, "response"),
                ()-> assertNotNull(validationUUID, "validationUUID ")
        );
    }

    @Test
    void requests() {
        List<Request> requestListError = nonValidRequests(validationUUID);
        for(Request request : requestListError) {
            sendMessage(objectToTextMessage(request));
            assertEquals(BAD_REQUEST, EnumMessage.valueOf(response), "BAD_REQUEST");
        }
        sendMessage(objectToTextMessage(validRequests(validationUUID, null)));
        sendMessage(objectToTextMessage(validRequests(validationUUID, null)));
        assertEquals(BETS_LIMIT, EnumMessage.valueOf(response), "BETS_LIMIT");
    }

    @Test
    void requestValid() throws InterruptedException, ExecutionException {
        setConnection();
        sendMessage(objectToTextMessage(validRequests(validationUUID, null)));
        assertEquals(BET_ACCEPTED, EnumMessage.valueOf(response), "BET_ACCEPTED");

        setConnection();
        sendMessage(objectToTextMessage(validRequests(validationUUID, "Chuck Norris")));
        assertEquals(BET_ACCEPTED, EnumMessage.valueOf(response), "BET_ACCEPTED");
    }

    private <T> TextMessage objectToTextMessage(T object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(object.toString());
            return new TextMessage(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(TextMessage textMessage) {
        try {
            session.sendMessage(textMessage);
            response = messages.poll(15, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


