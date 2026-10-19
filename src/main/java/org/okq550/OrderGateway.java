package org.okq550;

import org.okq550.domain.Order;
import org.okq550.domain.Side;
import org.okq550.domain.Trade;
import org.okq550.exchange.Exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class OrderGateway {
    private static final Exchange exchange = new Exchange();
    private static final AtomicLong nextId = new AtomicLong(0);

    public static void main(String[] args) throws IOException {
        exchange.start();
        ServerSocket serverSocket = new ServerSocket(9000);
        System.out.println("Gateway listening on port 9000...");

        while (true) {
            Socket client = serverSocket.accept();
            Thread.startVirtualThread(() -> {
                try {
                    handle(client);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    static void handle(Socket client) throws IOException {
        BlockingQueue<String> mailbox = new ArrayBlockingQueue<>(1000);
        Thread courier = null;
        // Parse each line, exchange.submit(order).get(), write the trades back.
        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            exchange.subscribe(mailbox);
            courier = Thread.startVirtualThread(() -> {
               try {
                while (true) {
                    String message = mailbox.take();
                    out.println(message);
                }
               } catch (InterruptedException e) {
                   // The client disconnected: stop delivering
               }
            });


            String line;
            while ((line = in.readLine()) != null) {
                // BUY AAPL 100 101.25
                String[] parts = line.split(" ");
                if(parts.length != 4) {
                    System.out.println("  rejected: expected SIDE SYMBOL QUANTITY PRICE, e.g. BUY AAPL 100 101.25");
                    continue;
                }
                try {
                    Side side = Side.valueOf(parts[0]);
                    String symbol = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    long priceInCents = new BigDecimal(parts[3]).movePointRight(2).longValueExact();
                    Order order = new Order(nextId.incrementAndGet(), symbol, side, quantity, priceInCents);
                    List<Trade> trades = exchange.submit(order).get();
                    if (trades.isEmpty()) {
                        out.println("  no trades");
                    }
                    for (Trade trade : trades) {
                        out.println("  TRADE " + trade.quantity() + " " + trade.symbol() + " @ " + price(trade.priceInCents()));
                    }
                }
                catch (IllegalArgumentException e) {
                    System.out.println("  rejected: " + e.getMessage());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            exchange.unsubscribe(mailbox);
            if(courier != null ) courier.interrupt();
        }
    }

    static String price(Long cents) {
        if(cents == null) {
            return "-";
        }
        return BigDecimal.valueOf(cents, 2).toString();
    }
}
