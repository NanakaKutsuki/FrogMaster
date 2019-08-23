package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $362594.34 LowestEquity -$14242.68 ROI 17.9124x Inputs: (8, -600,
//-25, 575, 1100, 11, -1300, -750, 1325, 2025)
// 1. Total $360624.64 LowestEquity -$14309.42 ROI 17.7565x Inputs: (8, -600,
// -25, 575, 1100, 4, -1125, -525, 1075, 1300)
// 1. Total $360243.56 LowestEquity -$19465.46 ROI 14.1464x Inputs: (8, -600,
// -25, 575, 1100, 7, -1575, -550, 2225, 1875)
public class HybridTimeLimitRE extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime GO_SHORT = LocalTime.of(15, 45);
    private static final LocalTime GO_LONG = LocalTime.of(23, 20);

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
