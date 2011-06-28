package com.example.algorithmictrader;

import com.streams.server.StreamProcessingServer;
import java.math.BigDecimal;

/**
 * Automated Trader takes price objects from Market Data Providors(Inputs) and makes trades based
 * on the provided trading algorithm which are then sent to Banks(Outputs).
 *
 * @author isyed
 * @version 0.1
 */
public class AutomaticTrader {

    //Start AutomaticTrader
    public static void main(String args[]) {
        System.out.print("PRODUCT SET ON INITIALIZATION: ");
        String[] products = {"YP", "BDSA", "A", "BC", "CD", "SHUTDOWN"};
        for (String st : products) {
            System.out.print(st + " ");
        }
        System.out.println("\n");

        //Initialize Stream Processing Server
        StreamProcessingServer<Price, Trade> autoTrader = new StreamProcessingServer<Price, Trade>(100000, 4, new AveragingAlgorithm<Price, Trade>(1000, 4, 800));

        System.out.println("PRODUCT:PRICE INPUT STREAM:");
        double[] pr = {0, 1, 2, 3, 4, 5, 0, 2, 19};
        for (int j = 0; j < products.length; j++) {
            if (!products[j].equals("SHUTDOWN")) {
                for (int k = 0; k < 9; k++) {
                    Price p = new Price(products[j], new BigDecimal(pr[k]));
                    autoTrader.offerInputQueue(p);
                    System.out.print(products[j] + ":" + p.getPrice() + " ");
                }
            } else if (products[j].equals("SHUTDOWN")) {//Special shutdown server input
                Price p = new Price(products[j], new BigDecimal(0));
                p.setShutdown(true);
                System.out.print(products[j] + ":" + p.getPrice() + " ");
                autoTrader.offerInputQueue(p);
            }
            System.out.println();
        }
        System.out.println();
        //Start Stream Processing Server
        autoTrader.startProcessing();
        /********DELAY IS FOR TESTING PURPOSES ONLY********/
        try {
            Thread.sleep(10);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        /********DELAY IS FOR TESTING PURPOSES ONLY********/
        System.out.println("TRADES MADE: ");
        Trade t;
        while ((t = autoTrader.pollOutputQueue()) != null) {
            System.out.print(t.getProductName() + ":" + t.getPrice() + ":" + t.getDirection() + " ");
        }
        System.out.println();
        //shutting AutomatedTrader programmatically
        //autoTrader.stopTrades();
    }
}
