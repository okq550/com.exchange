package org.okq550.Experementing;

import org.okq550.domain.Order;
import org.okq550.domain.Side;
import org.okq550.exchange.Exchange;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {
    static final int TRADERS = 4;
    static final int ORDERS_PER_TRADER = 250_000;

    public static void main(String[] args) throws InterruptedException {
        Exchange exchange = new Exchange();
        exchange.start();
        AtomicLong nextId = new AtomicLong(0);

        Thread[] traders = new Thread[TRADERS];
        for (int t = 0; t < TRADERS; t++) {
            traders[t] = new Thread(() -> {
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < ORDERS_PER_TRADER; i++) {
                    Side side = r.nextBoolean() ? Side.BUY : Side.SELL;
                    int qty = r.nextInt(1, 101);
                    long price = r.nextLong(10000, 10101);
                    Order order = new Order(nextId.incrementAndGet(), "AAPL", side, qty, price);
                    try {
                        exchange.submit(order);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }, "trader-" + t);
        }

        long start = System.nanoTime();

        for (Thread trader : traders) {
            trader.start();
        }
        for (Thread trader : traders) {
            trader.join();
        }

        while (!exchange.isIdle()) {
            Thread.onSpinWait();
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Orders: " + (TRADERS * ORDERS_PER_TRADER) + " in " + elapsedMs + " ms");

        exchange.stop();
    }
}