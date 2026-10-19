package org.okq550.Experementing;

import org.okq550.domain.Order;
import org.okq550.domain.Side;
import org.okq550.exchange.Exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9000);
        System.out.println("Listening on port 9000...");
        AtomicLong nextId = new AtomicLong();

        while (true) {
            Socket client = server.accept(); // Wait until someone connects
            Thread.startVirtualThread(() -> {
                System.out.println("Client connected from " + client.getInetAddress());
                long id = nextId.incrementAndGet(); // 1, 2, 3 ... never repeated
                try {
                    handle(client, id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    static void handle(Socket client, long id) throws IOException {
        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
            String line;
            Exchange exchange = new Exchange();
            while ((line = in.readLine()) != null) {
                // BUY AAPL 100 101.25
                String[] parts = line.split(" ");
                if(parts.length != 4) {
                    System.out.println("  rejected: expected SIDE SYMBOL QUANTITY PRICE, e.g. BUY AAPL 100 101.25");
                    continue;
                }
                Side side = Side.valueOf(parts[0]);
                String symbol = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                long priceInCents = new BigDecimal(parts[3]).movePointRight(2).longValueExact();
                Order order = new Order(id, symbol, side, quantity, priceInCents);
                exchange.submit(order);
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
