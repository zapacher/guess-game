package ee.ctob.data;

import lombok.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;


@Value
@Setter
public class Player {
    WebSocketSession session;
    String nickname;
    UUID playerUUID = UUID.randomUUID();
}
