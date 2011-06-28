package com.example.algorithimictrader;

import com.example.algorithmictrader.AveragingAlgorithm;
import com.example.algorithmictrader.Trade;
import com.example.algorithmictrader.Price;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author isyed
 */
public class AverageTradingAlgorithmTest {

    static List<Price> prices;
    static AveragingAlgorithm<Price, Trade> atAlgo;

    @BeforeClass
    public static void setUpClass() {
        String[] products = {"YP", "BDSA", "A", "BC", "CD"};
        atAlgo = new AveragingAlgorithm<Price, Trade>(1000, 4, 800);
        System.out.print("PRODUCT SET ON INITIALIZATION: ");
        for (String st : products) {
            System.out.print(st + " ");
        }
        System.out.println("\n");

        double[] pr = {0, 1, 2, 3, 4, 5, 0, 2, 19};
        prices = new ArrayList<Price>();
        System.out.println("EXAMPLE PRODUCT:PRICE INPUT:");
        for (int j = 0; j < products.length; j++) {
            for (int k = 0; k < 9; k++) {
                Price p = new Price(products[j], new BigDecimal(pr[k]));
                prices.add(p);
                System.out.print(products[j] + ":" + p.getPrice() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of buildTrades method, of class AverageTradingAlgorithm.
     */
    @org.junit.Test
    public void testBuildTrades() {
        System.out.println("PRICE INPUT STREAM ALONG WITH TRADES MADE: ");
        List<Trade> trList = new ArrayList();
        for (int i = 0; i < prices.size(); i++) {
            Price pri = prices.get(i);
            System.out.print(pri.getProdName() + ":" + pri.getPrice() + " ");
            Trade t = atAlgo.processStream(pri);
            if (t != null) {
                System.out.println(t.getProductName() + ":" + t.getPrice() + ":" + t.getDirection());
                trList.add(t);
            }
        }
        assertEquals(15, trList.size());
        System.out.println("\n");
    }
}
