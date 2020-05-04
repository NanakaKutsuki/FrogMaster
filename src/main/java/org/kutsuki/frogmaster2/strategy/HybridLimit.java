package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $294122.22 LowestEquity -$13709.36 ROI 14.9230x Inputs: (8, -650, -75, 1075, 1050)
//2. Total $293266.40 LowestEquity -$20862.68 ROI 10.9172x Inputs: (8, -650, -175, 1250, 925)
//3. Total $293216.48 LowestEquity -$14791.12 ROI 14.1030x Inputs: (8, -650, -150, 1025, 1050
public class HybridLimit extends AbstractStrategy {
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
    public void setup(Symbol symbol, List<LocalDateTime> keyList, List<Bar> barList, AbstractInput input) {
	setTickerBarMap(symbol, keyList, barList);
	this.initialized = false;
	this.lastMom = 0;
	this.input = (Input) input;
    }

    @Override
    protected void strategy(Bar bar) {
	mom = bar.getClose() - getPrevBar(input.getLength()).getClose();
	accel = mom - lastMom;
	lastMom = mom;

	if (!initialized) {
	    if (bar.getTime().equals(START)) {
		marketBuy();
		initialized = true;
	    }

	    lastMom = bar.getClose() - getPrevBar(input.getLength()).getClose();
	} else {
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
