package org.kutsuki.frogmaster2.strategy;

import java.time.LocalTime;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $367395.76 LowestEquity -$13878.00 ROI 18.4825x Inputs: (8, -600, -25, 575, 1100, 11, -1300, -750, 1325, 2025)
//1. Total $367012.06 LowestEquity -$13962.34 ROI 18.3852x Inputs: (8, -600, -25, 575, 1100, 10, -1400, -225, 1250, 2025)
//1. Total $365895.92 LowestEquity -$13729.84 ROI 18.5453x Inputs: (8, -600, -25, 575, 1100, 4, -1125, -525, 1075, 1300)
//1. Total $365832.72 LowestEquity -$15288.20 ROI 17.1848x Inputs: (8, -600, -25, 575, 1100, 12, -1000, -975, 1075, 2025)
//1. Total $363797.90 LowestEquity -$20033.10 ROI 13.9744x Inputs: (8, -600, -25, 575, 1100, 7, -1575, -550, 2250, 1950)

//1. Total $375338.58 LowestEquity -$20462.92 ROI 14.1836x Inputs: (8, -600, -25, 575, 1100, 11, -1325, -225, 1575, 325)
//1. Total $373458.72 LowestEquity -$16932.78 ROI 16.2849x Inputs: (8, -600, -25, 575, 1100, 10, -1400, -225, 1250, 2025)
//1. Total $373280.48 LowestEquity -$18693.80 ROI 15.1164x Inputs: (8, -600, -25, 575, 1100, 4, -325, -525, 1225, 350)
//1. Total $372694.64 LowestEquity -$18693.80 ROI 15.0926x Inputs: (8, -600, -25, 575, 1100, 11, -1300, -750, 1325, 2025)
//1. Total $370835.26 LowestEquity -$17999.68 ROI 15.4517x Inputs: (8, -600, -25, 575, 1100, 12, -1525, -700, 1325, 1200)
//1. Total $369099.98 LowestEquity -$24551.54 ROI 12.0812x Inputs: (8, -600, -25, 575, 1100, 7, -800, -800, 2325, 700)
public class HybridTimeLimitRE extends AbstractStrategy {
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
