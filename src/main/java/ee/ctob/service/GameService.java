package ee.ctob.service;

import ee.ctob.GuessGameProperties;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GuessGameProperties configuration;
    public GameService(GuessGameProperties configuration) {
        this.configuration = configuration;
    }

}
