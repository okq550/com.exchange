package org.okq550;

import org.okq550.domain.Order;
import org.okq550.domain.Side;
import org.okq550.exchange.Exchange;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        Exchange exchange = new Exchange();
        exchange.start();

        AtomicLong nextId = new AtomicLong();

        Thread traderA = new Thread(() -> {
            for(int i = 0; i < 5; i++) {
                long id = nextId.incrementAndGet(); // 1, 2, 3 ... never repeated
                Order order = new Order(id, "AAPL", Side.BUY, 100, 10100);
                try {
                    exchange.submit(order);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "trader-A");

        Thread traderB = new Thread(() -> {
            for(int i = 0; i < 5; i++) {
                long id = nextId.incrementAndGet();
                Order order = new Order(id, "AAPL", Side.SELL, 50, 10050);
                try {
                    exchange.submit(order);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "trader-B");

        traderA.start();
        traderA.join();
        traderB.start();
        traderB.join();

        Thread.sleep(500);
        exchange.stop();

//        BlockingQueue<String> inbox = new ArrayBlockingQueue<>(1000);
//
//        Thread engine = new Thread(() -> {
//            try {
//                for (int i = 0; i < 10; i++) {
//                    String order = inbox.take();
//                    System.out.println("Engine processed: " + order);
//                }
//            } catch (InterruptedException e) {
//                return;
//            }
//        });
//        engine.start();
//
//        Thread traderA = new Thread(() -> {
//            for (int i = 0; i < 5; i++) {
//                try { inbox.put("A-" + i); } catch (InterruptedException e) { return; }
//            }
//        });
//        Thread traderB = new Thread(() -> {
//            for (int i = 0; i < 5; i++) {
//                try { inbox.put("B-" + i); } catch (InterruptedException e) { return; }
//            }
//        });
//        traderA.start();
//        traderB.start();
//
//        traderA.join();
//        traderB.join();
//        engine.join();
//        Object lock = new Object();
//        int[] tradesExecuted = {0};
//
//        Thread a = new Thread(() -> {
//            for (int i = 0; i < 100_000; i++) {
//                synchronized (lock) {
//                    tradesExecuted[0]++;
//                }
////                System.out.println("Trader A sends order " + i);
//            }
//        });
//        Thread b = new Thread(() -> {
//            for (int i = 0; i < 100_000; i++) {
//                synchronized (lock) {
//                    tradesExecuted[0]++;
//                }
////                System.out.println("Trader B sends order " + i);
//            }
//        });
//
//        a.start();
//        b.start();
//        a.join();
//        b.join();
////        System.out.println("Both traders done");
//        System.out.println("Traders executed: " + tradesExecuted[0]);
    }
}
