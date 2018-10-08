package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridTest extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 5000000;
    private static final int COST_PER_CONTRACT_RE = 5000000;
    private static final LocalTime START = LocalTime.of(9, 25);
    private static final LocalTime END = LocalTime.of(15, 55);
    private static final LocalTime FOUR_PM = LocalTime.of(16, 0);

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
	if (bar.getTime().equals(START)) {
	    flipCore(bar);
	} else if (bar.getTime().equals(END)) {
	    flipAfter(bar);
	} else if (bar.getTime().isAfter(START) && bar.getTime().isBefore(FOUR_PM)) {
	    coreHours(bar);
	} else {
	    afterHours(bar);
	}
    }

    private void flipCore(Bar bar) {
	mom = bar.getClose() - getPrevBar(8).getClose();
	accel = mom - lastMom;
	lastMom = mom;

	if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
	    if (getMarketPosition() == 1) {
		marketSellShort();
	    }

	    highPrice = bar.getClose() + getInput().getUpAmount();
	    lowPrice = bar.getClose() - getInput().getDownAmount();
	} else if (getMarketPosition() <= 0) {
	    marketBuy();
	}
    }

    private void flipAfter(Bar bar) {
	mom2 = bar.getClose() - getPrevBar(getInput().getLengthAH()).getClose();
	accel2 = mom2 - lastMom2;
	lastMom2 = mom2;

	if (mom2 < getInput().getMomAH() && accel2 < getInput().getAccelAH()) {
	    if (getMarketPosition() == 1) {
		marketSellShort();
	    }

	    highPrice = bar.getClose() + getInput().getUpAmountAH();
	    lowPrice = bar.getClose() - getInput().getDownAmountAH();
	} else if (getMarketPosition() == -1) {
	    marketBuy();
	}
    }

    private void coreHours(Bar bar) {
	mom = bar.getClose() - getPrevBar(8).getClose();
	accel = mom - lastMom;
	lastMom = mom;

	if (getMarketPosition() == 1 && mom < getInput().getMomST() && accel < getInput().getAccelST()) {
	    highPrice = bar.getClose() + getInput().getUpAmount();
	    lowPrice = bar.getClose() - getInput().getDownAmount();
	    marketSellShort();
	} else if (getMarketPosition() == -1) {
	    if (bar.getLow() <= lowPrice) {
		if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
		    highPrice = bar.getClose() + getInput().getUpAmount();
		    lowPrice = bar.getClose() - getInput().getDownAmount();
		} else {
		    marketBuy();
		}
	    } else if (bar.getClose() >= highPrice) {
		marketBuy();
	    }
	}
    }

    private void afterHours(Bar bar) {
	mom2 = bar.getClose() - getPrevBar(getInput().getLengthAH()).getClose();
	accel2 = mom2 - lastMom2;
	lastMom2 = mom2;

	if (getMarketPosition() == 1 && mom2 < getInput().getMomAH() && accel2 < getInput().getAccelAH()) {
	    highPrice = bar.getClose() + getInput().getUpAmountAH();
	    lowPrice = bar.getClose() - getInput().getDownAmountAH();
	    marketSellShort();
	} else if (getMarketPosition() == -1) {
	    if (bar.getLow() <= lowPrice) {
		if (mom2 < getInput().getMomAH() && accel2 < getInput().getAccelAH()) {
		    highPrice = bar.getClose() + getInput().getUpAmountAH();
		    lowPrice = bar.getClose() - getInput().getDownAmountAH();
		} else {
		    marketBuy();
		}
	    } else if (bar.getClose() >= highPrice) {
		marketBuy();
	    }
	}
    }
}
