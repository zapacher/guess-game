package ee.ctob.data;

import ee.ctob.data.enums.BetResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    BetResult betResult;
    int betNumber;
    double betAmount;
    int winNumber;
    double winAmount;
}
