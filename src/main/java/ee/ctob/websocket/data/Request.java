package ee.ctob.websocket.data;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.UUID;

@Data
public class Request {
    @NotNull
    UUID validationUUID;

    @Min(1)
    @Max(10)
    int number;

    @Positive
    double amount;

    String nickname;
}
