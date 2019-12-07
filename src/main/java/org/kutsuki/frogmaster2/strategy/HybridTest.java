package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $141021.10 LowestEquity -$36382.64 ROI 3.3273x Inputs: (18, -1100, -125, 2400, 575)
public class HybridTest extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
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
    public void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, AbstractInput input) {
	setTickerBarMap(ticker, barMap);
	this.initialized = false;
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
		if (!skip(bar.getTime())) {
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
		}
	    } else if (bar.getTime().equals(START) && getMarketPosition() <= 0) {
		marketBuy();
	    }
	}
    }

    private boolean skip(LocalTime time) {
	int hour = time.getHour();
	int min = time.getMinute();

	return (hour == 16 && min == 10) || (hour == 16 && min == 15) || (hour == 16 && min == 20)
		|| (hour == 16 && min == 25) || (hour == 16 && min == 30) || (hour == 16 && min == 55) || hour == 17
		|| (hour == 18 && min == 0);
    }

    private boolean isDay(LocalTime time) {
	return (time.isAfter(END) && time.isBefore(LocalTime.MAX)) || time.equals(LocalTime.MIN)
		|| (time.isAfter(LocalTime.MIN) && time.isBefore(START));
    }
}