package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

/**
 * 
 * 1. Realized $220670.88 Unrealized $46700.06 LowestEquity $-7635.92 ROI
 * 19.8997x Inputs: (-600, -100, 1000, 1000, -300, -700, 300, 500) 2. Realized
 * $213675.08 Unrealized $49975.06 LowestEquity $-7635.92 ROI 19.6228x Inputs:
 * (-600, -100, 1000, 1000, -500, -700, 300, 800) 3. Realized $221202.46
 * Unrealized $46612.56 LowestEquity $-7860.92 ROI 19.6045x Inputs: (-600, -100,
 * 1000, 1000, -300, -700, 400, 500) 4. Realized $222740.04 Unrealized $51350.06
 * LowestEquity $-8182.96 ROI 19.6017x Inputs: (-600, -100, 1000, 1000, -600,
 * -600, 300, 500) 5. Realized $217032.32 Unrealized $49975.06 LowestEquity
 * $-7860.92 ROI 19.5453x Inputs: (-600, -100, 1000, 1000, -600, -700, 400, 500)
 * 6. Realized $216861.84 Unrealized $49975.06 LowestEquity $-7860.92 ROI
 * 19.5329x Inputs: (-600, -100, 1000, 1000, -500, -700, 400, 500) 7. Realized
 * $220124.42 Unrealized $46612.56 LowestEquity $-7860.92 ROI 19.5256x Inputs:
 * (-600, -100, 1000, 1000, -400, -700, 400, 500) 8. Realized $214863.40
 * Unrealized $46700.06 LowestEquity $-7635.92 ROI 19.4675x Inputs: (-600, -100,
 * 1000, 1000, -400, -700, 300, 800) 9. Realized $207695.30 Unrealized $44787.56
 * LowestEquity $-7169.64 ROI 19.4672x Inputs: (-600, -100, 800, 1100, -300,
 * -500, 400, 400)
 *
 *
 */
public class HybridTest extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(16, 00);

    private boolean initialized;
    private int mom;
    private int accel;
    private int mom2;
    private int accel2;
    private int highPrice;
    private int lowPrice;
    private int lastMom;
    private int lastMom2;

    @Override
    public void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
	this.initialized = false;
	this.lastMom = 0;
	this.lastMom2 = 0;
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

	    lastMom = bar.getClose() - getPrevBar(8).getClose();
	    lastMom2 = bar.getClose() - getPrevBar(4).getClose();
	} else {
	    if (bar.getTime().isAfter(START) && bar.getTime().isBefore(END)) {
		mom = bar.getClose() - getPrevBar(8).getClose();
		accel = mom - lastMom;
		lastMom = mom;

		if (getMarketPosition() == 1 && mom < getInput().getMomST() && accel < getInput().getAccelST()) {
		    highPrice = bar.getClose() + getInput().getUpAmount();
		    lowPrice = bar.getClose() - getInput().getDownAmount();
		    marketSellShort();
		}
	    } else {
		mom2 = bar.getClose() - getPrevBar(4).getClose();
		accel2 = mom2 - lastMom2;
		lastMom2 = mom2;

		if (getMarketPosition() == 1 && mom2 < getInput().getMomAH() && accel2 < getInput().getAccelAH()) {
		    highPrice = bar.getClose() + getInput().getUpAmountAH();
		    lowPrice = bar.getClose() - getInput().getDownAmountAH();
		    marketSellShort();
		}
	    }
	}

	if (getMarketPosition() == -1) {
	    if (bar.getLow() <= lowPrice) {
		marketBuy();
	    } else if (bar.getClose() >= highPrice) {
		marketBuy();
	    }
	}
    }
}
