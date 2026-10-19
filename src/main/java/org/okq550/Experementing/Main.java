package org.okq550.Experementing;

import org.okq550.book.OrderBook;
import org.okq550.domain.Order;
import org.okq550.domain.Side;
import org.okq550.domain.Trade;
import org.okq550.engine.MatchingEngine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
// import org.okq550.domain.Side;
// import org.okq550.domain.Trade;

// import java.util.ArrayDeque;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        int counter = 1;
        MatchingEngine engine = new MatchingEngine();

        while(true) {
            System.out.print("> ");
            String line = in.nextLine();

            if(line.equals("exit")) {
                break;
            }

            try {
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
                Order order = new Order(counter, symbol, side, quantity, priceInCents);
                List<Trade> trades = engine.submit(order);

                if(trades.isEmpty()) {
                    System.out.println("No trades found");
                }

                for(Trade trade : trades) {
                    System.out.println("  TRADE " + trade.quantity() + " " + trade.symbol() + " @ " + BigDecimal.valueOf(trade.priceInCents(), 2));
                }
                System.out.println("best bid: " + price(engine.bestBid()) + "  best ask: " + price(engine.bestAsk()));

                counter++;
            }
            catch (Exception e) {
                System.out.println("  rejected: " + e.getMessage());
            }
        }
    }

    static String price(Long cents) {
        if(cents == null) {
            return "-";
        }
        return BigDecimal.valueOf(cents, 2).toString();
    }

}
