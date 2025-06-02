package ee.ctob.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Bet {
    Player player;
    int number;
    double amount;
}
