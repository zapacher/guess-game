package ee.ctob;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Data
@Primary
@ConfigurationProperties(prefix = "game")
public class GuessGameProperties {
    int betTimeoutSeconds;
    double payoutMultiplier;
    int maxBetNumber;
}