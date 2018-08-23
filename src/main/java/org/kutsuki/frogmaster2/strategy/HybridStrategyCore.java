package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridStrategyCore extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 50000000;
    private static final LocalTime END = LocalTime.of(16, 00);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);

    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;

    @Override
    public void init(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
	this.lastMom = 0;
    }

    @Override
    public int getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public void strategy(Bar bar) {
	if (bar.getDateTime().toLocalTime().equals(NINE_TWENTYFIVE)) {
	    mom = bar.getClose() - getPrevBar(8).getClose();
	    accel = mom - lastMom;
	    lastMom = mom;

	    if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
		highPrice = bar.getClose() + getInput().getUpAmount();
		lowPrice = bar.getClose() - getInput().getDownAmount();
		marketSellShort();
	    } else {
		marketBuy();
	    }
	} else if (isDay(bar.getDateTime().toLocalTime())) {
	    mom = bar.getClose() - getPrevBar(8).getClose();
	    accel = mom - lastMom;
	    lastMom = mom;

	    if (getMarketPosition() != -1) {
		if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
		    highPrice = bar.getClose() + getInput().getUpAmount();
		    lowPrice = bar.getClose() - getInput().getDownAmount();
		    marketSellShort();
		}
	    } else if (getMarketPosition() == -1) {
		if (bar.getLow() <= lowPrice) {
		    marketBuy();
		} else if (bar.getClose() >= highPrice) {
		    marketBuy();
		}
	    }
	} else {
	    if (getMarketPosition() == 1) {
		marketSell();
	    } else if (getMarketPosition() == -1) {
		marketBuyToCover();
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(NINE_TWENTYFIVE) && time.isBefore(END);
    }
}
