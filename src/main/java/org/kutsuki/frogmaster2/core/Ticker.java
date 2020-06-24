package org.kutsuki.frogmaster2.core;

import java.math.BigDecimal;

public class Ticker {
    public static final Ticker ES = new Ticker("ES", 50, 25, 275, BigDecimal.valueOf(100));
    public static final Ticker GC = new Ticker("GC", 100, 1, 27, BigDecimal.TEN);
    public static final Ticker VX = new Ticker("VX", 1000, 1, 301, BigDecimal.valueOf(100));

    private String ticker;
    private int dollarValue;
    private int minTick;
    private int commission;
    private BigDecimal divisor;

    private Ticker(String ticker, int dollarValue, int minTick, int commission, BigDecimal divisor) {
	this.ticker = ticker;
	this.dollarValue = dollarValue;
	this.minTick = minTick;
	this.commission = commission;
	this.divisor = divisor;
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

    public BigDecimal getDivisor() {
	return divisor;
    }
}
