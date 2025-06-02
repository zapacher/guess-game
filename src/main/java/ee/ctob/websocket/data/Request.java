package ee.ctob.websocket.data;

import lombok.Data;

@Data
public class Request {
    int number;
    double amount;
    String nickname;
}
