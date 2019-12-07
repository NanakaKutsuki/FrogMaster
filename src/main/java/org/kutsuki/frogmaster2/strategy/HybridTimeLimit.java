package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $330827.08 LowestEquity -$23986.82 ROI 11.0324x Inputs: (8, -600, -75, 575, 1100)
//2. Total $330401.58 LowestEquity -$22996.36 ROI 11.3946x Inputs: (8, -600, -25, 575, 1100)
//3. Total $328359.56 LowestEquity -$25360.34 ROI 10.4705x Inputs: (8, -600, -50, 575, 1100)
//4. Total $328353.14 LowestEquity -$22807.38 ROI 11.3982x Inputs: (8, -600, 0, 575, 1100)
//5. Total $328274.94 LowestEquity -$22536.08 ROI 11.5039x Inputs: (8, -600, -25, 600, 1100)
//6. Total $328128.90 LowestEquity -$23616.26 ROI 11.0793x Inputs: (8, -625, -75, 575, 1100)
//7. Total $327168.86 LowestEquity -$20914.04 ROI 12.1561x Inputs: (8, -600, -25, 625, 1100)
//8. Total $326422.70 LowestEquity -$30552.94 ROI 8.9301x Inputs: (8, -600, -50, 575, 1000)
//9. Total $325787.28 LowestEquity -$22770.52 ROI 11.3236x Inputs: (8, -625, 0, 575, 1100)
public class HybridTimeLimit extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
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
