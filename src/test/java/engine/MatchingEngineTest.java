package engine;

import org.junit.jupiter.api.Test;
import org.okq550.domain.Order;
import org.okq550.domain.Side;
import org.okq550.domain.Trade;
import org.okq550.engine.MatchingEngine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchingEngineTest {
    @Test
    void buyBelowBestAskRestsWithoutTrading() {
        MatchingEngine engine = new MatchingEngine();
        engine.submit(new Order(1, "AAPL", Side.SELL, 100, 10200));

        List<Trade> trades = engine.submit(new Order(2, "AAPL", Side.BUY, 100, 10150));

        assertTrue(trades.isEmpty());
        assertEquals(10150L, engine.bestBid());
        assertEquals(10200L, engine.bestAsk());
    }

    @Test
    void buyCrossesBestAskTradesAtRestingPrice() {
        MatchingEngine engine = new MatchingEngine();
        engine.submit(new Order(1, "AAPL", Side.SELL, 100, 10100));   // ask 100 @ 101.00
        engine.submit(new Order(2, "AAPL", Side.SELL, 100, 10150));   // ask 100 @ 101.50

        List<Trade> trades = engine.submit(new Order(3, "AAPL", Side.BUY, 100, 10150));   // buy 100 @ 101.50

        assertEquals(1, trades.size());                                // exactly one trade
        assertEquals(new Trade("AAPL", 100, 10100), trades.getFirst()); // at the resting price, 101.00
        assertNull(engine.bestBid());                                  // buy fully filled: no bids left
        assertEquals(10150L, engine.bestAsk());                        // 101.00 ask used up: 101.50 is best
    }

    @Test
    void buyPartiallyFillsRestingAskAndAskKeepsRemainder() {
        MatchingEngine engine = new MatchingEngine();
        engine.submit(new Order(1, "AAPL", Side.SELL, 80, 10100L));   // ask 80 @ 101.00

        List<Trade> trades = engine.submit(new Order(2, "AAPL", Side.BUY, 50, 10100L));   // buy 50 @ 101.00

        assertEquals(1, trades.size());
        assertEquals(new Trade("AAPL", 50, 10100L), trades.getFirst());
        assertNull(engine.bestBid());                // buyer fully filled
        assertEquals(10100L, engine.bestAsk());        // seller's leftover still in the book
    }

    @Test
    void sellSweepsTwoBidLevelsAndPartiallyFillsSecond() {
        MatchingEngine engine = new MatchingEngine();
        engine.submit(new Order(1, "AAPL", Side.BUY, 200, 10100L));   // bid 200 @ 101.00
        engine.submit(new Order(2, "AAPL", Side.BUY, 100, 10050L));   // bid 100 @ 100.50

        List<Trade> trades = engine.submit(new Order(3, "AAPL", Side.SELL, 250, 10050L));

        assertEquals(2, trades.size());
        assertEquals(new Trade("AAPL", 200, 10100L), trades.get(0));
        assertEquals(new Trade("AAPL", 50, 10050L), trades.get(1));
        assertEquals(10050L, engine.bestBid());   // what's left of the second bid
        assertNull(engine.bestAsk());           // seller fully filled
    }
}
