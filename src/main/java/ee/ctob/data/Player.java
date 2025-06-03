package ee.ctob.data;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;


@Data
public class Player {
    WebSocketSession session;
    String nickname = "Player_" + UUID.randomUUID();
    UUID validationUUID = UUID.randomUUID();
    long lastActivity;

    public Player(WebSocketSession session, long lastActivity) {
        this.session = session;
        this.lastActivity = lastActivity;
    }
}
