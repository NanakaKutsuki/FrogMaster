package org.kutsuki.frogmaster2.strategy;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.TimeInput;

public class TimeShort extends AbstractStrategy {
    private TimeInput input;

    @Override
    public void setup(Symbol symbol, BarMap barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.input = (TimeInput) input;
    }

    @Override
    protected void strategy(Bar bar) {
	if (getMarketPosition() == 0 && bar.getTime().equals(input.getShort1())) {
	    marketSellShort();
	} else if (getMarketPosition() == -1 && bar.getTime().equals(input.getLong1())) {
	    marketBuyToCover();
	}
    }
}
