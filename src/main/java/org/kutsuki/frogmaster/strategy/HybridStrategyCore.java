package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.HybridInputsCore;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategyCore extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("500000");
    private static final LocalTime END = LocalTime.of(16, 00);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);

    private boolean marketShort;
    private boolean marketBuy;
    private boolean marketSell;
    private boolean marketCover;
    private BigDecimal mom;
    private BigDecimal accel;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private Input input;

    public HybridStrategyCore(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.input = HybridInputsCore.getInput();
	this.lastMom = BigDecimal.ZERO;
	this.longPos = null;
	this.marketShort = false;
	this.marketBuy = false;
	this.marketSell = false;
	this.marketCover = false;
	this.shortPos = null;
    }

    @Override
    public BigDecimal getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public BigDecimal getStrategyMargin() {
	BigDecimal margin = BigDecimal.ZERO;

	if (longPos != null) {
	    margin = margin.add(getMaintenanceMargin());
	}

	if (shortPos != null) {
	    margin = margin.add(getMaintenanceMargin());
	}

	return margin;
    }

    private long count = 0;

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (marketBuy) {
	    if (shortPos != null) {
		addBankroll(bar, shortPos.subtract(bar.getOpen()));

		longPos = bar.getOpen();
		shortPos = null;

		System.out.println(count + " " + bar.getDateTime() + " Long " + longPos + " " + getBankroll());
		count++;
		System.out.println(count + " " + bar.getDateTime() + " Long " + longPos);
	    } else {
		longPos = bar.getOpen();
		shortPos = null;

		count++;
		System.out.println(count + " " + bar.getDateTime() + " Long " + longPos);
	    }

	    marketBuy = false;
	} else if (marketShort) {
	    if (longPos != null) {
		addBankroll(bar, bar.getOpen().subtract(longPos));
		longPos = null;

		System.out.println(count + " " + bar.getDateTime() + " Short " + bar.getOpen() + " " + getBankroll());
	    }

	    count++;
	    System.out.println(count + " " + bar.getDateTime() + " Short " + bar.getOpen());

	    shortPos = bar.getOpen();
	    marketShort = false;
	} else if (marketCover) {
	    if (shortPos != null) {
		addBankroll(bar, shortPos.subtract(bar.getOpen()));
		shortPos = null;
		System.out
			.println(count + " " + bar.getDateTime() + " CoverOut " + bar.getOpen() + " " + getBankroll());
	    }

	    marketCover = false;
	} else if (marketSell) {
	    if (longPos != null) {
		addBankroll(bar, bar.getOpen().subtract(longPos));
		longPos = null;

		System.out.println(count + " " + bar.getDateTime() + " SellOut " + bar.getOpen() + " " + getBankroll());
	    }

	    marketSell = false;
	}
    }

    @Override
    public void strategy(Bar bar) {
	if (bar.getDateTime().toLocalTime().equals(NINE_TWENTYFIVE)) {
	    mom = bar.getClose().subtract(getPrevBar(8).getClose());
	    accel = mom.subtract(lastMom);
	    lastMom = mom;

	    if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
		highPrice = bar.getClose().add(input.getUpAmount());
		lowPrice = bar.getClose().subtract(input.getDownAmount());
		marketShort = true;
	    } else {
		marketBuy = true;
	    }
	} else if (isDay(bar.getDateTime().toLocalTime())) {
	    mom = bar.getClose().subtract(getPrevBar(8).getClose());
	    accel = mom.subtract(lastMom);
	    lastMom = mom;

	    if (shortPos == null) {
		if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		    marketShort = true;
		}
	    } else if (shortPos != null) {
		if (bar.getLow().compareTo(lowPrice) <= 0) {
		    marketBuy = true;
		} else if (bar.getClose().compareTo(highPrice) >= 0) {
		    marketBuy = true;
		}
	    }
	} else {
	    if (longPos != null) {
		marketSell = true;
	    } else if (shortPos != null) {
		marketCover = true;
	    }
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getClose().subtract(longPos);
	} else if (shortPos != null) {
	    unrealized = shortPos.subtract(bar.getClose());
	}

	return unrealized;
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
