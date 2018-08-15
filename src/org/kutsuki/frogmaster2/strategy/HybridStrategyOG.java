package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.Bar;
import org.kutsuki.frogmaster2.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridStrategyOG extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 1200000000;
    private static final LocalTime END = LocalTime.of(16, 00);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);

    private boolean initialized;
    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;
    private Input input;

    public HybridStrategyOG(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	super(ticker, barMap);
	this.initialized = false;
	this.input = input;
	this.lastMom = 0;
    }

    @Override
    public int getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getDateTime().toLocalTime().equals(NINE_TWENTYFIVE)) {
		marketBuy();
		initialized = true;
	    }

	    lastMom = bar.getClose() - getPrevBar(8).getClose();
	} else {
	    if (isDay(bar.getDateTime().toLocalTime())) {
		mom = bar.getClose() - getPrevBar(8).getClose();
		accel = mom - lastMom;
		lastMom = mom;

		if (getMarketPosition() != -1) {
		    if (mom < input.getMomST() && accel < input.getAccelST()) {
			highPrice = bar.getClose() + input.getUpAmount();
			lowPrice = bar.getClose() - input.getDownAmount();
			marketSellShort();
		    }
		} else if (getMarketPosition() == -1) {
		    if (bar.getLow() <= lowPrice) {
			marketBuy();
		    } else if (bar.getClose() >= highPrice) {
			marketBuy();
		    }
		}
	    }
	}
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), NINE_TWENTYFIVE);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), LocalTime.of(17, 00));
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(NINE_TWENTYFIVE) && time.isBefore(END);
    }
}
