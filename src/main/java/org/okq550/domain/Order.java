package org.okq550.domain;

public record Order(long id, String symbol, Side side, int quantity, long priceInCents) {
    public Order {
        if (priceInCents <= 0) {
            throw new IllegalArgumentException("Price in cents must be positive");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }
    }
}
