package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridTest extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 3000000;
    private static final int COST_PER_CONTRACT_RE = 3000000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(16, 00);

    private int onHigh;
    private int onLow;
    private int highPrice;
    private int lowPrice;

    @Override
    public void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
	this.onHigh = 0;
	this.onLow = 999999;
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
	if (isDay(bar.getTime())) {
	    if (getMarketPosition() == 0) {
		if (bar.getClose() > onHigh) {
		    marketSellShort();
		    highPrice = bar.getClose() + getInput().getUpAmount();
		    lowPrice = bar.getClose() - getInput().getDownAmount();
		} else if (bar.getLow() < onLow) {
		    marketBuy();
		    highPrice = bar.getClose() + getInput().getUpAmountAH();
		    lowPrice = bar.getClose() - getInput().getDownAmountAH();
		}
	    } else if (getMarketPosition() == 1) {
		if (bar.getHigh() >= highPrice) {
		    marketSell();
		} else if (bar.getClose() <= lowPrice) {
		    marketSell();
		}
	    } else if (getMarketPosition() == -1) {
		if (bar.getLow() <= lowPrice) {
		    marketBuy();
		} else if (bar.getClose() >= highPrice) {
		    marketBuy();
		}
	    }
	} else {
	    if (bar.getTime().equals(END)) {
		onHigh = bar.getHigh();
		onLow = bar.getLow();
	    }

	    if (onHigh > bar.getHigh()) {
		onHigh = bar.getHigh();
	    }

	    if (onLow < bar.getLow()) {
		onLow = bar.getLow();
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }
}
