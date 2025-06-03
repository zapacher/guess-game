package ee.ctob.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Winner {
    String nickname;
    double winAmount;
}
