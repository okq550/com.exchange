package org.okq550.exchange;

import org.okq550.domain.Order;
import org.okq550.domain.Trade;
import org.okq550.engine.MatchingEngine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;


public class Exchange {

    // A queued item: the order plus the box its result goes into
    private record Request(Order order, CompletableFuture<List<Trade>> result) { }

    private final BlockingQueue<Request> inbound = new ArrayBlockingQueue<>(10_000);
    private final MatchingEngine engine = new MatchingEngine();
    private final Thread engineThread = new Thread(this::run, "engine");
    private final List<BlockingQueue<String>> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(BlockingQueue<String> mailbox) {
        subscribers.add(mailbox);
    }

    public void unsubscribe(BlockingQueue<String> mailbox) {
        subscribers.remove(mailbox);
    }

    public void publish(String message) {
        for (BlockingQueue<String> mailbox : subscribers) {
            if (!mailbox.offer(message)) {
                System.out.println("Slow subscriber: message dropped");
            }
        }
    }

    public void start() {
        engineThread.start();
    }

    // Called by any client thread. Returns a receipt that the engine fills in later.
    public CompletableFuture<List<Trade>> submit(Order order) throws InterruptedException {
        CompletableFuture<List<Trade>> result = new CompletableFuture<>();
        inbound.put(new Request(order, result));
        return result;
    }

    public boolean isIdle() {
        return inbound.isEmpty();
    }

    public void stop() {
        engineThread.interrupt();
    }

    // Runs only on the engine thread: the single writer.
    private void run() {
        try {
            while (true) {
                Request request = inbound.take();
                List<Trade> trades = engine.submit(request.order());
                request.result().complete(trades);

                for(Trade trade : trades) {
                    publish("MARKET TRADE " + trade.quantity() + " " + trade.symbol() + " @ " + trade.priceInCents());
                }
                publish("MARKET BEST bid=" + engine.bestBid() + " ask=" + engine.bestAsk());
            }
        } catch (InterruptedException e) {
            // stop() was called
        }
    }
}