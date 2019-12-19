package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.TimeInput;

/**
 * 1. Total $171499.52 LowestEquity -$18884.84 ROI 6.8917x Inputs: (0, 15, 45,
 * 23, 15, 0, 9, 40, 14, 15)
 *
 */
public class Continum extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
    private static final LocalTime START = LocalTime.of(9, 30);

    private boolean initialized;
    private TimeInput input;

    @Override
    public void setup(Symbol symbol, TreeMap<LocalDateTime, Bar> barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.initialized = false;
	this.input = (TimeInput) input;
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
	} else {
	    if (getMarketPosition() == 1
		    && (bar.getTime().equals(input.getShort1()) || bar.getTime().equals(input.getShort2()))) {
		marketSellShort();
	    } else if (getMarketPosition() == -1
		    && (bar.getTime().equals(input.getLong1()) || bar.getTime().equals(input.getLong2()))) {
		marketBuy();
	    }
	}
    }
}
