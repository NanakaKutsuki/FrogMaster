package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.TimeInput;

/**
 * 
 * 1. Total $152949.88 LowestEquity -$28068.18 ROI 4.4895x Inputs:
 * ("23:15","00:00","15:45","00:00",2150,3475)
 * 
 * 
 * 1. Total $141908.78 LowestEquity -$22794.78 ROI 4.9283x Inputs:
 * ("23:15","00:00","15:45","00:00",1675,450) 2. Total $137872.76 LowestEquity
 * -$22046.12 ROI 4.9159x Inputs: ("23:15","00:00","15:45","00:00",1700,300)
 *
 */
public class TimeLong extends AbstractStrategy {
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
	    if (getMarketPosition() >= 0 && bar.getTime().equals(input.getShort1())) {
		marketSellShort();
	    } else if (getMarketPosition() <= 0 && bar.getTime().equals(input.getLong1())) {
		marketBuy();
	    }
	}
    }
}
