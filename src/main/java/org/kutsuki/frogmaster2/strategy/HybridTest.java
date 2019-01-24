package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

/**
 * 1 min 1. Total $254716.62 LowestEquity -$12452.78 ROI 13.9549x Inputs: (28,
 * -675, -225, 1450, 900)
 *
 */
public class HybridTest extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 3000000;
    private static final int COST_PER_CONTRACT_RE = 3000000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(16, 00);

    private boolean initialized;
    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;

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
	if (!initialized) {
	    if (bar.getTime().equals(START)) {
		marketBuy();
		initialized = true;
	    }

	    lastMom = bar.getClose() - getPrevBar(getInput().getLength()).getClose();
	} else {
	    if (isDay(bar.getTime())) {
		mom = bar.getClose() - getPrevBar(getInput().getLength()).getClose();
		accel = mom - lastMom;
		lastMom = mom;

		if (getMarketPosition() == 1) {
		    if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
			highPrice = bar.getClose() + getInput().getUpAmount();
			lowPrice = bar.getClose() - getInput().getDownAmount();
			marketSellShort();
			limitCover(lowPrice);
		    }
		} else if (getMarketPosition() <= 0) {
		    if (bar.getLow() <= lowPrice) {
			if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
			    highPrice = bar.getClose() + getInput().getUpAmountAH();
			    lowPrice = bar.getClose() - getInput().getDownAmountAH();
			    limitCover(lowPrice);
			} else {
			    marketBuy();
			}
		    } else if (bar.getClose() >= highPrice) {
			marketBuy();
		    } else if (getMarketPosition() == -1) {
			limitCover(lowPrice);
		    }
		}
	    } else if (bar.getTime().equals(END) && getMarketPosition() <= 0 && bar.getLow() <= lowPrice) {
		marketBuy();
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }
}
