package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridAH extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
    private static final LocalTime START = LocalTime.of(15, 55);
    private static final LocalTime END = LocalTime.of(9, 25);
    private static final LocalTime MIDN = LocalTime.of(23, 55);

    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;

    @Override
    public void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
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
	if (bar.getTime().equals(START)) {
	    mom = bar.getClose() - getPrevBar(getInput().getLength()).getClose();
	    accel = mom - lastMom;
	    lastMom = mom;

	    if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
		highPrice = bar.getClose() + getInput().getUpAmount();
		lowPrice = bar.getClose() - getInput().getDownAmount();
		marketSellShort();
	    } else {
		marketBuy();
	    }
	} else if (isDay(bar.getTime())) {
	    mom = bar.getClose() - getPrevBar(getInput().getLength()).getClose();
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
	} else {
	    if (getMarketPosition() == 1) {
		marketSell();
	    } else if (getMarketPosition() == -1) {
		marketBuyToCover();
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return (time.isAfter(START) && (time.isBefore(MIDN) || time.equals(MIDN)))
		|| ((time.isAfter(LocalTime.MIN) || time.equals(LocalTime.MIN)) && time.isBefore(END));
    }
}
