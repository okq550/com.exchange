package org.okq550.book;

import org.okq550.domain.Order;
import org.okq550.domain.Side;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.TreeMap;

public class OrderBook {
    private final TreeMap<Long, ArrayDeque<Order>> bids = new TreeMap<>();
    private final TreeMap<Long, ArrayDeque<Order>> asks = new TreeMap<>();
    private final HashMap<Long, Order> ordersById = new HashMap<>();

    private TreeMap<Long, ArrayDeque<Order>> sideMapFor(Side side) {
        if (side == Side.BUY) {
            return this.bids;
        }
        return this.asks;
    }

    public void add(Order order) {
        // This will point to the same TreeMap
        // Put the order in the correct side
        TreeMap<Long, ArrayDeque<Order>> sideMap = sideMapFor(order.side());
        ArrayDeque<Order> queue = sideMap.get(order.priceInCents());

        if (queue == null) {
            queue = new ArrayDeque<Order>();
            sideMap.put(order.priceInCents(), queue);
        }

        queue.addLast(order);
        this.ordersById.put(order.id(), order);
    }

    public boolean cancel(long orderId) {

        Order order = this.ordersById.remove(orderId);

        if (order == null) {
            return false;
        }

        TreeMap<Long, ArrayDeque<Order>> sideMap = sideMapFor(order.side());
        ArrayDeque<Order> queue = sideMap.get(order.priceInCents());
        queue.remove(order);

        if (queue.isEmpty()) {
            sideMap.remove(order.priceInCents());
        }

        return true;
    }

    // The first order in line at the best price on this side, or null if that side is empty.
    public Order peekBest(Side side) {
        TreeMap<Long, ArrayDeque<Order>> sideMap = sideMapFor(side);
        if (sideMap.isEmpty()) {
            return null;
        }
        Long bestPrice;
        if (side == Side.BUY) {
            bestPrice = sideMap.lastKey();    // highest bid
        } else {
            bestPrice = sideMap.firstKey();   // lowest ask
        }
        return sideMap.get(bestPrice).peekFirst();
    }

    // Replace the front order at the best price with a copy that has less quantity, keeping its place in line.
    public void reduceBest(Side side, int newQuantity) {
        TreeMap<Long, ArrayDeque<Order>> sideMap = sideMapFor(side);
        Long bestPrice;
        if (side == Side.BUY) {
            bestPrice = sideMap.lastKey();
        } else {
            bestPrice = sideMap.firstKey();
        }
        ArrayDeque<Order> queue = sideMap.get(bestPrice);

        Order old = queue.pollFirst();                       // take the front order out
        Order reduced = new Order(old.id(), old.symbol(), old.side(), newQuantity, old.priceInCents());
        queue.addFirst(reduced);                             // put the smaller copy back at the FRONT
        ordersById.put(reduced.id(), reduced);               // keep the index in sync
    }

    public Long bestBid() {
        // Highest bid price, or null if there are no bids
        // The best price for the sellers (asks).
        if (this.bids.isEmpty()) return null;
        return this.bids.lastKey();
    }

    public Long bestAsk() {
        // Lowest ask price, or null if there are no asks.
        // The best price for the buyers (bids).
        if (this.asks.isEmpty()) return null;
        return this.asks.firstKey();
    }
}
