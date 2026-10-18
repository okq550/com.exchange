package org.okq550.exchange;

import org.okq550.domain.Order;
import org.okq550.domain.Trade;
import org.okq550.engine.MatchingEngine;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Exchange {
    private final BlockingQueue<Order> inbound = new ArrayBlockingQueue<>(100_000);
    private final MatchingEngine matchingEngine = new MatchingEngine();
    private final Thread engineThread = new Thread(this::run, "engine");
    private int tradeCount = 0;
//    private final Thread engineThread = new Thread(() -> run());

    public void start() {
        engineThread.start();
    }

    // Called by any number of trader threads.
    public void submit(Order order) throws InterruptedException {
        inbound.put(order);
    }

    public boolean isIdle() {
        return inbound.isEmpty();
    }

    public void stop() {
        engineThread.interrupt();
    }

    static String price(Long cents) {
        if(cents == null) {
            return "-";
        }
        return BigDecimal.valueOf(cents, 2).toString();
    }

    // Runs only in the engine thread: the single writer.
    private void run() {
        try {
          while (true) {
              Order order = inbound.take();
              List<Trade> trades = matchingEngine.submit(order);
              this.tradeCount += trades.size();
          }
        } catch (InterruptedException e) {
            System.out.println("Engine stopped. Trades executed: " + tradeCount);
        }
    }
}
