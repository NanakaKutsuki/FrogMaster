package org.kutsuki.frogmaster2.strategy;

import java.time.LocalTime;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.LineInput;

// 1. Total $329395.00 LowestEquity -$24766.00 ROI 10.7065x Inputs: (-133, -731,
// 1108, 10:50)
public class HybridTest extends AbstractStrategy {
    // private static final LocalTime CORE_TIME = LocalTime.of(8, 0);
    private static final LocalTime AH_TIME = LocalTime.of(16, 0);

    private boolean initialized;
    private LineInput input;
    private int po;
    private int lastPO;

    @Override
    public void setup(Symbol symbol, BarMap barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	checkPrecalc();

	this.initialized = false;
	this.input = (LineInput) input;
	this.po = 0;
	this.lastPO = 0;
    }

    @Override
    protected void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getTime().equals(input.getCoreTime())) {
		marketBuy();
		initialized = true;
	    }
	} else {
	    po = bar.getPo();

	    runCoreHours(bar);
	    runAfterHours(bar);

	    lastPO = po;
	}
    }

    private void runCoreHours(Bar bar) {
	if (bar.getTime().equals(input.getCoreTime())) {
	    startSession(bar, input.getCoreLine());
	} else if (bar.getTime().isAfter(input.getCoreTime()) && bar.getTime().isBefore(AH_TIME)) {
	    runStrategy(bar, input.getCoreLine());
	}
    }

    private void runAfterHours(Bar bar) {
	if (bar.getTime().equals(AH_TIME)) {
	    startSession(bar, input.getAhLine());
	} else if ((bar.getTime().isAfter(AH_TIME) && bar.getTime().isBefore(LocalTime.MAX))
		|| bar.getTime().equals(LocalTime.MIN)
		|| (bar.getTime().isAfter(LocalTime.MIN) && bar.getTime().isBefore(input.getCoreTime()))) {
	    runStrategy(bar, input.getAhLine());
	}
    }

    private void startSession(Bar bar, int line) {
	if (getMarketPosition() == -1 && po > line) {
	    marketBuy();
	} else if (getMarketPosition() == 1 && po < line) {
	    marketSellShort();
	}
    }

    private void runStrategy(Bar bar, int line) {
	boolean goLong = false;
	boolean goShort = false;

	goLong = po > line && lastPO < line;
	goShort = po < line && lastPO > line;

	if (getMarketPosition() == -1 && goLong) {
	    marketBuy();
	} else if (getMarketPosition() == 1 && goShort) {
	    marketSellShort();
	}
    }
}