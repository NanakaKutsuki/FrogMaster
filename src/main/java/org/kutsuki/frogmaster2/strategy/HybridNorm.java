package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridNorm extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 1500000;
    private static final int COST_PER_CONTRACT_RE = 2500000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(16, 00);

    private boolean initialized;
    private double mom;
    private double accel;
    private int highPrice;
    private int lowPrice;
    private double lastMom;

    private int high;
    private int low;

    @Override
    public void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
	this.initialized = false;
	this.lastMom = 0;
    }

    @Override
    protected int getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    protected int getCostPerContractRE() {
	return COST_PER_CONTRACT_RE;
    }

    @Override
    protected void strategy(Bar bar) {
	if (bar.getLow() < low) {
	    low = bar.getLow();
	}

	if (bar.getHigh() > high) {
	    high = bar.getHigh();
	}

	double close = normalize(bar.getClose());
	if (!initialized) {
	    if (bar.getTime().equals(START)) {
		marketBuy();
		initialized = true;
	    }

	    lastMom = close - normalize(getPrevBar(8).getClose());
	} else {
	    if (isDay(bar.getTime())) {
		lastMom = close - normalize(getPrevBar(8).getClose());
		accel = mom - lastMom;
		lastMom = mom;

		if (getMarketPosition() == 1) {
		    if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
			highPrice = bar.getClose() + getInput().getUpAmount();
			lowPrice = bar.getClose() - getInput().getDownAmount();
			marketSellShort();
		    }
		} else if (getMarketPosition() == -1) {
		    if (bar.getLow() <= lowPrice) {
			marketBuy();
		    } else if (bar.getClose() >= highPrice) {
			marketBuy();
		    }
		}
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }

    private double normalize(int price) {
	return (double) (price - low) / (double) (high - low);
    }
}
