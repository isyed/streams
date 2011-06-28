package com.example.algorithmictrader;

import com.streams.io.SOutput;
import java.math.BigDecimal;

/**
 * A trade domain object
 * 
 * @author isyed
 * @version 0.1
 */
public final class Trade extends SOutput {

    final String productName;
    final TRADE Direction;
    final BigDecimal price;
    final int quantitiy;

    public static enum TRADE {

        BUY, SELL
    };

    public Trade(String productName, TRADE Direction, BigDecimal price, int quantitiy) {
        this.productName = productName;
        this.Direction = Direction;
        this.price = price;
        this.quantitiy = quantitiy;
    }

    public TRADE getDirection() {
        return Direction;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantitiy() {
        return quantitiy;
    }
}
