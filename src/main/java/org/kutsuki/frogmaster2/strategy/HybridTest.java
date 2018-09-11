package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridTest extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 3000000;
    private static final int COST_PER_CONTRACT_RE = 3000000;
    private static final LocalTime END = LocalTime.of(16, 00);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);

    private int mom;
    private int accel;
    private int highPrice;
    private int lowPrice;
    private int lastMom;

    private int mom2;
    private int accel2;
    private List<Integer> lastMomList;

    @Override
    public void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
	this.lastMom = 0;
	this.lastMomList = new ArrayList<Integer>();
	for (int i = 0; i < 12; i++) {
	    this.lastMomList.add(0);
	}
    }

    private int getLastMom(int lengthA) {
	return lastMomList.get(lengthA - 1);
    }

    private void addMom(int mom) {
	lastMomList.add(0, mom);
	lastMomList.remove(lastMomList.size() - 1);
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
	if (getMarketPosition() == 0 && bar.getDateTime().toLocalTime().equals(NINE_TWENTYFIVE)) {
	    marketBuy();
	} else {
	    if (isDay(bar.getDateTime().toLocalTime())) {
		mom = bar.getClose() - getPrevBar(8).getClose();
		accel = mom - lastMom;
		lastMom = mom;

		if (getMarketPosition() == 1) {
		    if (mom < getInput().getMomST() && accel < getInput().getAccelST()) {
			highPrice = bar.getClose() + getInput().getUpAmount();
			lowPrice = bar.getClose() - getInput().getDownAmount();
			marketSellShort();
		    }
		}
	    } else {
		mom2 = bar.getClose() - getPrevBar(getInput().getLengthAH()).getClose();
		accel2 = mom2 - getLastMom(getInput().getLengthAH2());
		addMom(mom);

		if (getMarketPosition() == 1) {
		    if (mom2 < getInput().getMomAH() || accel2 < getInput().getAccelAH()) {
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

    private boolean isDay(LocalTime time) {
	return time.isAfter(NINE_TWENTYFIVE) && time.isBefore(END);
    }
}
