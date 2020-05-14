package org.kutsuki.frogmaster2.strategy;

import java.time.LocalTime;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.LineInput;

// 3,-373,402
public class HybridTest extends AbstractStrategy {
    private static final LocalTime CORE_TIME = LocalTime.of(9, 25);
    private static final LocalTime AH_TIME = LocalTime.of(15, 45);
    private static final LocalTime ON_TIME = LocalTime.of(23, 55);

    private boolean initialized;
    private LineInput input;
    private int po;
    private int lastPO;
    private int ahOpen;
    private int onOpen;

    @Override
    public void setup(Symbol symbol, BarMap barMap, AbstractInput input) {
	setTickerBarMap(symbol, barMap);
	this.initialized = false;
	this.input = (LineInput) input;
	this.po = 0;
	this.lastPO = 0;
    }

    @Override
    protected void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getTime().equals(CORE_TIME)) {
		marketBuy();
		initialized = true;
	    }
	} else {
	    po = bar.getPo();

	    runCoreHours(bar);
	    runAfterHours(bar);
	    // runOverNight(bar);

	    lastPO = po;
	}
    }

    private void runCoreHours(Bar bar) {
	boolean goLong = po > input.getCoreLine() && lastPO < input.getCoreLine();
	boolean goShort = po < input.getCoreLine() && lastPO > input.getCoreLine();

	if (bar.getTime().equals(ON_TIME) && getMarketPosition() == -1) {
	    marketBuy();
	} else if ((bar.getTime().equals(LocalTime.MIN) || bar.getTime().isAfter(LocalTime.MIN))
		&& bar.getTime().isBefore(AH_TIME)) {
	    flipPosition(goLong, goShort);
	}
    }

    private void runAfterHours(Bar bar) {
	boolean goLong = po > input.getAhLine() && lastPO < input.getAhLine();
	boolean goShort = po < input.getAhLine() && lastPO > input.getAhLine();

	if (bar.getTime().equals(AH_TIME) && getMarketPosition() == 1) {
	    marketSellShort();
	    ahOpen = 0;

	    if (po > input.getAhLine()) {
		ahOpen = bar.getOpen();
	    }
	} else if (bar.getTime().isAfter(AH_TIME) && bar.getTime().isBefore(ON_TIME)) {
	    if (getMarketPosition() == 1) {
		ahOpen = 0;
	    }

	    flipPosition(goLong, goShort);
	}
    }

    // private void runAfterHours(Bar bar) {
    // boolean goLong = po > input.getAhLine() && lastPO < input.getAhLine();
    // boolean goShort = po < input.getAhLine() && lastPO > input.getAhLine();
    // boolean escape = ahOpen > 0 && bar.getClose() - ahOpen > input.getAhEscape();
    // // boolean escape = false;
    //
    // if (bar.getTime().equals(AH_TIME) && getMarketPosition() == 1) {
    // marketSellShort();
    // ahOpen = 0;
    //
    // if (po > input.getAhLine()) {
    // ahOpen = bar.getOpen();
    // }
    // } else if (bar.getTime().isAfter(AH_TIME) && bar.getTime().isBefore(ON_TIME))
    // {
    // if (getMarketPosition() == 1) {
    // ahOpen = 0;
    // }
    //
    // flipPosition(goLong || escape, goShort);
    // }
    // }

    private void runOverNight(Bar bar) {
	boolean goLong = po > input.getOnLine() && lastPO < input.getOnLine();
	boolean goShort = po < input.getOnLine() && lastPO > input.getOnLine();
	boolean escape = onOpen > 0 && onOpen - bar.getClose() > input.getOnEscape();

	if (bar.getTime().equals(ON_TIME) && getMarketPosition() == -1) {
	    marketBuy();
	    onOpen = 0;

	    if (po < input.getOnLine()) {
		onOpen = bar.getOpen();
	    }
	} else if ((bar.getTime().equals(LocalTime.MIN) || bar.getTime().isAfter(LocalTime.MIN))
		&& bar.getTime().isBefore(CORE_TIME)) {
	    if (getMarketPosition() == -1) {
		onOpen = 0;
	    }

	    flipPosition(goLong, goShort || escape);
	}
    }

    private void flipPosition(boolean goLong, boolean goShort) {
	if (getMarketPosition() == -1 && goLong) {
	    marketBuy();
	} else if (getMarketPosition() == 1 && goShort) {
	    marketSellShort();
	}
    }
}