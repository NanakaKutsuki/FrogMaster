package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $294122.22 LowestEquity -$13709.36 ROI 14.9230x Inputs: (8, -650, -75, 1075, 1050)
//2. Total $293266.40 LowestEquity -$20862.68 ROI 10.9172x Inputs: (8, -650, -175, 1250, 925)
//3. Total $293216.48 LowestEquity -$14791.12 ROI 14.1030x Inputs: (8, -650, -150, 1025, 1050
public class HybridLimit extends AbstractStrategy {
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
    private Input input;

    @Override
    public void setup(Symbol symbol, TreeMap<LocalDateTime, Bar> barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.initialized = false;
	this.lastMom = 0;
	this.input = (Input) input;
    }

    private Input getInput() {
	return input;
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
			marketBuy();
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
