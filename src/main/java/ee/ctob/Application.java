package ee.ctob;

import ee.ctob.websocket.config.WebSocketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        GuessGameProperties.class, WebSocketProperties.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}