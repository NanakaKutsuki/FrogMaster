package org.kutsuki.frogmaster2.strategy;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
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
    private TimeInput input;

    @Override
    public void setup(Symbol symbol, BarMap barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.input = (TimeInput) input;
    }

    @Override
    protected void strategy(Bar bar) {
	if (getMarketPosition() == 1 && bar.getTime().equals(input.getShort1())) {
	    marketSell();
	} else if (getMarketPosition() == 0 && bar.getTime().equals(input.getLong1())) {
	    marketBuy();
	}
    }
}
