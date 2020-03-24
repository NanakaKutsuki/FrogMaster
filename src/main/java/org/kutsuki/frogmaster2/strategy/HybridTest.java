package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;

//1. Total $272643.12 LowestEquity -$42888.04 ROI 5.5769x Inputs: (8, -625, -100, 625, 1025)
//2. Total $271231.12 LowestEquity -$48613.04 ROI 4.9664x Inputs: (8, -625, -100, 650, 1050)
//3. Total $270136.04 LowestEquity -$44474.80 ROI 5.3519x Inputs: (8, -625, -100, 575, 1025)
//4. Total $269760.40 LowestEquity -$48613.04 ROI 4.9395x Inputs: (8, -625, -100, 675, 1050)
//5. Total $269342.76 LowestEquity -$49926.78 ROI 4.8160x Inputs: (8, -650, -75, 1025, 1050)
//6. Total $269254.72 LowestEquity -$42726.28 ROI 5.5259x Inputs: (8, -650, -50, 625, 1025)
//7. Total $269190.80 LowestEquity -$40763.58 ROI 5.7564x Inputs: (8, -650, -75, 1100, 1100)
//8. Total $268985.20 LowestEquity -$47980.60 ROI 4.9830x Inputs: (8, -650, -100, 1025, 1050)
//9. Total $268616.12 LowestEquity -$37879.00 ROI 6.1217x Inputs: (8, -650, -75, 1100, 1050)
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
    public void setup(Symbol symbol, TreeMap<LocalDateTime, Bar> barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
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
		    }
		} else if (getMarketPosition() <= 0) {
		    if (bar.getLow() <= lowPrice) {
			if (input.getLength() > 0 && mom2 < input.getMomST() && accel2 < input.getAccelST()) {
			    highPrice = bar.getClose() + input.getUpAmount();
			    lowPrice = bar.getClose() - input.getDownAmount();
			} else {
			    marketBuy();
			}
		    } else if (bar.getClose() >= highPrice) {
			marketBuy();
		    }
		}
	    }
	}
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }
}
