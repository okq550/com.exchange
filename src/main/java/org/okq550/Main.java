package org.okq550;

import org.okq550.book.OrderBook;
import org.okq550.domain.Order;
import org.okq550.domain.Side;
// import org.okq550.domain.Side;
// import org.okq550.domain.Trade;

// import java.util.ArrayDeque;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {

        Order OrderA = new Order(1, "AAPL", Side.BUY, 10, 10100);
        Order OrderB = new Order(2, "AAPL", Side.BUY, 10, 10150);
        Order OrderC = new Order(3, "AAPL", Side.SELL, 10, 10200);
        Order OrderD = new Order(4, "AAPL", Side.SELL, 10, 10175);

//        List<Order> bids = new ArrayList<>();
//        bids.add(a);
//        bids.add(b);
//        bids.add(c);
//
//        for(Order o : bids) {
//            System.out.println(o);
//        }

//        ArrayDeque<Order> queue = new ArrayDeque<>();
//        queue.addLast(OrderA);
//        queue.addLast(OrderB);
//
//        queue.peekFirst();
//        queue.pollFirst();

        OrderBook orderBook = new OrderBook();
        orderBook.add(OrderA);
        orderBook.add(OrderB);
        orderBook.add(OrderC);
        orderBook.add(OrderD);
        orderBook.cancel(4);

        System.out.println("Best bid: " + orderBook.bestBid());
        System.out.println("Best ask: " + orderBook.bestAsk());
    }

}
