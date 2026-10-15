package org.okq550.domain;

public record Trade(String symbol, int quantity, long priceInCents) {
    public Trade {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (priceInCents <= 0) {
            throw new IllegalArgumentException("Price in cents must be positive");
        }
    }
}
