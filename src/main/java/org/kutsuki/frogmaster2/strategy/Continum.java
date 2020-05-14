package org.kutsuki.frogmaster2.strategy;

import java.time.LocalTime;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.TimeInput;

public class Continum extends AbstractStrategy {
    private static final LocalTime START = LocalTime.of(9, 25);

    private boolean initialized;
    private TimeInput input;

    @Override
    public void setup(Symbol symbol, BarMap barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.initialized = false;
	this.input = (TimeInput) input;
    }

    @Override
    protected void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getTime().equals(START)) {
		marketBuy();
		initialized = true;
	    }
	} else {
	    if (getMarketPosition() <= 0 && bar.getTime().equals(input.getLong1())) {
		marketBuy();
	    } else if (getMarketPosition() >= 0 && bar.getTime().equals(input.getShort1())) {
		marketSellShort();
	    } else if (getMarketPosition() == -1 && bar.getTime().equals(input.getLong2())) {
		marketBuyToCover();
	    }
	}
    }
}
