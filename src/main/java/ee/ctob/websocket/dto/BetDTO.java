package ee.ctob.websocket.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Builder
public class BetDTO {
    @Min(1)
    @Max(10)
    private int number;

    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String nickname;
}
