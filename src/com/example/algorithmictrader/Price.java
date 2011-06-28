package com.example.algorithmictrader;

import com.streams.io.SInput;
import java.math.BigDecimal;

/**
 * A price domain object
 *
 * @author isyed
 * @version 0.1
 */
public final class Price extends SInput {

    final String prodName;
    final BigDecimal price;

    public Price(String productName, BigDecimal price) {
        this.prodName = productName;
        this.price = price;
    }

    public String getProdName() {
        return prodName;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
