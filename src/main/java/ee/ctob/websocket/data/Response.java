package ee.ctob.websocket.data;

import ee.ctob.websocket.data.enums.Action;
import ee.ctob.websocket.data.enums.Result;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Response {
    Action action;
    Result result;
    BigDecimal ammoint;
    List<String> nicknamesWon;
}
