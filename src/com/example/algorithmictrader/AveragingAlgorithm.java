package com.example.algorithmictrader;

import com.streams.io.SInput;
import com.streams.io.SOutput;
import com.example.algorithmictrader.Trade.TRADE;
import com.streams.server.StreamProcessingServer;
import java.math.BigDecimal;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements an averaging algorithm, which averages over a price window of given size.
 * Makes trades if the simple average of a moving window of prices has an upward trend
 * Uses Atomic References to manage concurrency. No locking is involved.
 * 
 * @author isyed
 * @version 0.1
 */
public final class AveragingAlgorithm<I extends SInput, O extends SOutput> implements StreamProcessingServer.ProcessingAlgorithm<Price, Trade> {

    //Adjustable parameters
    private final int noOfProducts;
    private final int windowSize;
    private final int tradeOrderSize;
    //Map holding atomic references to price window (associated with each product) over which the averaging takes place
    private final Map<String, AtomicReference<PriceWindow>> atomicProductMap;

    public AveragingAlgorithm(int noOfProducts, int windowSize, int tradeOrderSize) {
        this.noOfProducts = noOfProducts;
        this.windowSize = windowSize;
        this.tradeOrderSize = tradeOrderSize;
        atomicProductMap = new HashMap<String, AtomicReference<PriceWindow>>(2 * noOfProducts, 0.75f);
    }

    /**
     * Makes trades if the simple average of a moving window of prices has an upward trend
     * Uses Atomic References to manage concurrency. No locking is involved.
     * @param price from Market Data Providers
     * @return Trade sent to Bank, Null if no trade
     */
    public Trade processStream(Price price) {
        PriceWindow oldpw;
        PriceWindow newpw;
        Trade trade = null;
        AtomicReference<PriceWindow> atomicpw;
        do {
            atomicpw = atomicProductMap.get(price.getProdName());
            if (atomicpw != null) {
                oldpw = atomicpw.get();
            } else {
                //Each product's stream is allocated a separate window over which different
                //queries and aggregations for that instrument can be performed
                oldpw = new PriceWindow(windowSize, price.getPrice());
                AtomicReference<PriceWindow> apw = new AtomicReference<PriceWindow>();
                apw.set(oldpw);
                //TODO: Add logic for stream sampling (by product name), which could be turned on for
                //holding very large windows in main memory if required. (This is an approximation technique)
                atomicProductMap.put(price.getProdName(), apw);
                return null;
            }
            newpw = new PriceWindow(windowSize, oldpw.prices);
            newpw.addPrice(price.getPrice());
        } while (!atomicpw.compareAndSet(oldpw, newpw));
        if ((newpw.average).compareTo(newpw.oldest) == 1) {
            trade = new Trade(price.getProdName(), TRADE.BUY, price.getPrice(), tradeOrderSize);
        }
        return trade;
    }

    /**
     * Window over which averaging takes place. The window size is adjustable.
     */
    private static class PriceWindow {

        private final Deque<BigDecimal> prices = new LinkedList<BigDecimal>();
        private final int windowSize;
        private BigDecimal average = new BigDecimal(0);
        private BigDecimal oldest = new BigDecimal(0);

        public PriceWindow(int windowSize) {
            this.windowSize = windowSize;
        }

        public PriceWindow(int windowSize, BigDecimal price) {
            this.windowSize = windowSize;
            prices.add(price);
        }

        public PriceWindow(int windowSize, Deque<BigDecimal> prices) {
            this.windowSize = windowSize;
            for (BigDecimal d : prices) {
                this.prices.offer(d);
            }
        }

        /**
         * Adds a price value to the window and resets the value of the average and the oldest price
         * Note: No trades can be made until there are atleast a minimum number of price values(=windowSize) in the
         * window.
         * @param p the price to add to the window
         */
        public void addPrice(BigDecimal p) {
            prices.offer(p);
            if (prices.size() > windowSize) {
                prices.poll();
                BigDecimal sum = new BigDecimal(0);
                for (BigDecimal pr : prices) {
                    sum = sum.add(pr);
                }
                average = sum.divide(new BigDecimal(windowSize));
                oldest = prices.peek();
            }
        }
    }
}
