package org.kutsuki.frogmaster2.strategy;

import java.time.LocalTime;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $378710.00 LowestEquity -$15549.78 ROI 17.5737x Inputs: (8, -650, -75, 1025, 1050, 11, -1400, -775, 2600, 2025)
//1. Total $375642.00 LowestEquity -$16516.48 ROI 16.6830x Inputs: (8, -650, -75, 1025, 1050, 10, -1425, -300, 1500, 1975)
public class HybridLimitRE extends AbstractStrategy {
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(16, 0);

    private boolean initialized;
    private Input input;
    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;

    private int mom2;
    private int accel2;
    private int lastMom2;

    @Override
    public void setup(Symbol symbol, BarMap barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.initialized = false;
	this.input = (Input) input;
    }

    @Override
    protected void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getTime().equals(START)) {
		marketBuy();
		initialized = true;
	    }
	} else {
	    mom = bar.getClose() - getPrevBar(input.getLength()).getClose();
	    accel = mom - lastMom;
	    lastMom = mom;

	    mom2 = bar.getClose() - getPrevBar(input.getLengthRE()).getClose();
	    accel2 = mom2 - lastMom2;
	    lastMom2 = mom2;

	    if (isDay(bar.getTime())) {
		if (getMarketPosition() == 1) {
		    if (mom < input.getMomST() && accel < input.getAccelST()) {
			highPrice = bar.getClose() + input.getUpAmount();
			lowPrice = bar.getClose() - input.getDownAmount();
			marketSellShort();
			limitCover(lowPrice);
		    }
		} else if (getMarketPosition() <= 0) {
		    if (bar.getLow() <= lowPrice) {
			if (input.getLengthRE() > 0 && mom2 < input.getMomRE() && accel2 < input.getAccelRE()) {
			    highPrice = bar.getClose() + input.getUpAmountRE();
			    lowPrice = bar.getClose() - input.getDownAmountRE();
			    marketSellShort();
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
	    } else if (bar.getTime().equals(END) && getMarketPosition() == 0) {
		marketBuy();
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }
}
