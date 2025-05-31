package ee.ctob.websocket.data;

import lombok.Value;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Value
public class Request {
    @Min(1)
    @Max(10)
    int number;

    @DecimalMin("0.01")
    BigDecimal amount;

    @NotBlank
    String nickname;
}
