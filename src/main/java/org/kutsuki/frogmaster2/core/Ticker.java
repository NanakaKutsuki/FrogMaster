package org.kutsuki.frogmaster2.core;

public class Ticker {
    public static final Ticker ES = new Ticker("ES", 50, 25, 269);
    public static final Ticker GC = new Ticker("GC", 100, 1, 27);
    public static final Ticker US = new Ticker("US", 1000, 3125, 269000);

    private String ticker;
    private int dollarValue;
    private int minTick;
    private int commission;

    private Ticker(String ticker, int dollarValue, int minTick, int commission) {
	this.ticker = ticker;
	this.dollarValue = dollarValue;
	this.minTick = minTick;
	this.commission = commission;
    }

    public int getCommission() {
	return commission;
    }

    public int getDollarValue() {
	return dollarValue;
    }

    public int getMinimumTick() {
	return minTick;
    }

    public String getTicker() {
	return ticker;
    }
}
