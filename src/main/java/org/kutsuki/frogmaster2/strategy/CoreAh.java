package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $312909.72 LowestEquity -$16112.50 ROI 14.1508x Inputs: (8, -600,
// -25, 575, 1100, 5, -825, -150, 600, 475)
public class CoreAh extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(16, 0);

    private Input input;
    private boolean initialized;
    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;

    private int mom2;
    private int accel2;
    private int lastMom2;

    @Override
    public void setup(Symbol symbol, TreeMap<LocalDateTime, Bar> barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.input = (Input) input;
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
	mom = bar.getClose() - getPrevBar(input.getLength()).getClose();
	accel = mom - lastMom;
	lastMom = mom;

	mom2 = bar.getClose() - getPrevBar(input.getLengthRE()).getClose();
	accel2 = mom2 - lastMom2;
	lastMom2 = mom2;

	if (!initialized) {
	    if (bar.getTime().equals(START)) {
		marketBuy();
		initialized = true;
	    }
	} else {
	    if (bar.getTime().equals(START)) {
		flipCore(bar);
	    } else if (input.getLengthRE() > 0 && bar.getTime().equals(END)) {
		flipAfter(bar);
	    } else if (bar.getTime().isAfter(START) && bar.getTime().isBefore(END)) {
		coreHours(bar);
	    } else if (input.getLengthRE() > 0) {
		afterHours(bar);
	    }
	}
    }

    private void flipCore(Bar bar) {
	if (mom < input.getMomST() && accel < input.getAccelST()) {
	    if (getMarketPosition() == 1) {
		marketSellShort();
	    }

	    highPrice = bar.getClose() + input.getUpAmount();
	    lowPrice = bar.getClose() - input.getDownAmount();
	    limitCover(lowPrice);
	} else if (getMarketPosition() <= 0) {
	    marketBuy();
	}
    }

    private void flipAfter(Bar bar) {
	if (mom2 < input.getMomRE() && accel2 < input.getAccelRE()) {
	    if (getMarketPosition() == 1) {
		marketSellShort();
	    }

	    highPrice = bar.getClose() + input.getUpAmountRE();
	    lowPrice = bar.getClose() - input.getDownAmountRE();
	    limitCover(lowPrice);
	} else if (getMarketPosition() == -1) {
	    marketBuy();
	}
    }

    private void coreHours(Bar bar) {
	if (getMarketPosition() == 1 && mom < input.getMomST() && accel < input.getAccelST()) {
	    highPrice = bar.getClose() + input.getUpAmount();
	    lowPrice = bar.getClose() - input.getDownAmount();
	    marketSellShort();
	    limitCover(lowPrice);
	} else if (getMarketPosition() <= 0) {
	    if (bar.getLow() <= lowPrice) {
		marketBuy();
	    } else if (bar.getClose() >= highPrice) {
		marketBuy();
	    } else if (getMarketPosition() == -1) {
		limitCover(lowPrice);
	    }
	}
    }

    private void afterHours(Bar bar) {
	if (getMarketPosition() == 1 && mom2 < input.getMomRE() && accel2 < input.getAccelRE()) {
	    highPrice = bar.getClose() + input.getUpAmountRE();
	    lowPrice = bar.getClose() - input.getDownAmountRE();
	    marketSellShort();
	    limitCover(lowPrice);
	} else if (getMarketPosition() <= 0) {
	    if (bar.getLow() <= lowPrice) {
		marketBuy();
	    } else if (bar.getClose() >= highPrice) {
		marketBuy();
	    } else if (getMarketPosition() == -1) {
		limitCover(lowPrice);
	    }
	}
    }
}
