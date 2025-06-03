package ee.ctob.websocket.config;

import ee.ctob.GuessGameProperties;
import ee.ctob.service.GameService;
import ee.ctob.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameService gameService;
    private final GuessGameProperties properties;
    private final WebSocketProperties webSocketProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (properties.isAvailable()) {
            registry.addHandler(new WebSocketHandler(gameService, properties), "/game/guess")
                    .setAllowedOrigins("*");
        }
    }
}