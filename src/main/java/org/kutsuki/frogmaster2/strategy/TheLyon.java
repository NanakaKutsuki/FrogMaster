package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class TheLyon extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 2500000;
    private static final int BEAR_SHORT = 475;
    private static final int BEAR_LONG = 500;
    private static final int BULL_SHORT = 1275;
    private static final int BULL_LONG = 925;
    private static final int NEUTRAL_SHORT = 925;
    private static final int NEUTRAL_LONG = 725;
    private static final int DOWN = 92;
    private static final int NEUTRAL = 9825;
    private static final LocalTime START = LocalTime.of(9, 25);

    private int highY;
    private int lastHigh;
    private int lastLow;
    private int lastYear;

    @Override
    public void init(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
	this.highY = 0;
	this.lastHigh = 999999;
	this.lastLow = -999999;
	this.lastYear = 0;
    }

    @Override
    public int getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public void strategy(Bar bar) {
	if (bar.getDateTime().getYear() > lastYear) {
	    highY = bar.getHigh();
	    lastYear = bar.getDateTime().getYear();
	}

	if (bar.getHigh() > highY) {
	    highY = bar.getHigh();
	}

	if (bar.getClose() - bar.getOpen() < 0 && bar.getLow() <= lastLow - BEAR_SHORT && getMarketPosition() >= 0
		&& bar.getClose() <= DOWN * highY(0)) {
	    marketSellShort();
	}

	if (bar.getClose() - bar.getOpen() > 0 && bar.getHigh() >= lastHigh + BEAR_LONG && getMarketPosition() <= 0
		&& bar.getClose() <= DOWN * highY(0)) {
	    marketBuy();
	}

	if (bar.getClose() - bar.getOpen() < 0 && bar.getLow() <= lastLow - BULL_SHORT && getMarketPosition() >= 0
		&& bar.getClose() > DOWN * highY(0)) {
	    marketSellShort();
	}

	if (bar.getClose() - bar.getOpen() > 0 && bar.getHigh() >= lastHigh + BULL_LONG && getMarketPosition() <= 0
		&& bar.getClose() > DOWN * highY(0)) {
	    marketBuy();
	}

	if (bar.getClose() - bar.getOpen() < 0 && bar.getLow() <= lastLow - NEUTRAL_SHORT && getMarketPosition() >= 0
		&& bar.getClose() > DOWN * highY(0) && bar.getClose() <= NEUTRAL * highY(0)) {
	    marketSellShort();
	}

	if (bar.getClose() - bar.getOpen() > 0 && bar.getHigh() >= lastHigh + NEUTRAL_LONG && getMarketPosition() <= 0
		&& bar.getClose() > DOWN * highY(0) && bar.getClose() <= NEUTRAL * highY(0)) {
	    marketBuy();
	}

	if (bar.getClose() - bar.getOpen() < 0) {
	    lastHigh = bar.getHigh();
	}

	if (bar.getClose() - bar.getOpen() > 0) {
	    lastLow = bar.getLow();
	}
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), START);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), START);
    }

    private int highY(int year) {
	return highY;
    }
}
