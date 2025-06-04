package ee.ctob.websocket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.DoubleAdder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class TestRTPGuessGame {
    private static final int ROUNDS = 1_000_000;
    private static final int THREADS = 24;
    private static final double BET_AMOUNT = 1.0;
    private static final int FIXED_BET_NUMBER = 5;

    @Test
    void simulateRtpOverMillionRounds() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        Random random = new Random();

        DoubleAdder totalBet = new DoubleAdder();
        DoubleAdder totalWon = new DoubleAdder();

        CountDownLatch latch = new CountDownLatch(ROUNDS);

        for (int i = 0; i < ROUNDS; i++) {
            executor.execute(() -> {
                int winningNumber = random.nextInt(10) + 1;

                totalBet.add(BET_AMOUNT);

                if (winningNumber == FIXED_BET_NUMBER) {
                    totalWon.add(BigDecimal.valueOf(BET_AMOUNT * 9.9)
                            .setScale(2, RoundingMode.HALF_DOWN)
                            .doubleValue());
                }

                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        double rtp = totalWon.doubleValue() / totalBet.doubleValue();

        log.info("Total bet: {}, Total won: {}, RTP: {}",
                BigDecimal.valueOf(totalBet.doubleValue())
                        .setScale(2, RoundingMode.HALF_DOWN)
                        .doubleValue(),
                BigDecimal.valueOf(totalWon.doubleValue())
                        .setScale(2, RoundingMode.HALF_DOWN)
                        .doubleValue(),
                BigDecimal.valueOf(rtp)
                        .setScale(2, RoundingMode.HALF_DOWN)
                        .doubleValue());

        assertTrue(rtp > 0 && rtp < 2, "RTP should be within realistic bounds");
    }
}