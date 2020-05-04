package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $343050.76 LowestEquity -$15529.06 ROI 15.9343x Inputs: (8, -600,
// -25, 575, 1100, 4, -1075, -525, 1050, 1275)
public class HybridTimeRE extends AbstractStrategy {
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime GO_SHORT = LocalTime.of(15, 45);
    private static final LocalTime GO_LONG = LocalTime.of(18, 50);

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
    public void setup(Symbol symbol, List<LocalDateTime> keyList, List<Bar> barList, AbstractInput input) {
	setTickerBarMap(symbol, keyList, barList);
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
		    }
		} else if (getMarketPosition() <= 0) {
		    if (bar.getLow() <= lowPrice) {
			if (input.getLengthRE() > 0 && mom2 < input.getMomRE() && accel2 < input.getAccelRE()) {
			    highPrice = bar.getClose() + input.getUpAmountRE();
			    lowPrice = bar.getClose() - input.getDownAmountRE();
			} else {
			    marketBuy();
			}
		    } else if (bar.getClose() >= highPrice) {
			marketBuy();
		    }
		}
	    } else if (getMarketPosition() == 1 && bar.getTime().equals(GO_SHORT)) {
		marketSellShort();
	    } else if (getMarketPosition() == -1 && bar.getTime().equals(GO_LONG)) {
		marketBuy();
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(GO_SHORT);
    }
}
