package ee.ctob.websocket.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.ctob.data.enums.BetResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    BetResult betResult;
    Integer betNumber;
    Double betAmount;
    Integer winNumber;
    Double winAmount;
}
