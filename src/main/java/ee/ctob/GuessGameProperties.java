package ee.ctob;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Data
@Primary
@ConfigurationProperties(prefix = "game")
public class GuessGameProperties {
    boolean available;
    int maxIndividualBets;
    int roundSeconds;
    double payoutMultiplier;
}