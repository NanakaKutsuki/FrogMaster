package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Ticker;

public class TheLyon extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("25000");
    private static final BigDecimal BEAR_SHORT = new BigDecimal("4.75");
    private static final BigDecimal BEAR_LONG = new BigDecimal("5");
    private static final BigDecimal BULL_SHORT = new BigDecimal("12.75");
    private static final BigDecimal BULL_LONG = new BigDecimal("9.25");
    private static final BigDecimal NEUTRAL_SHORT = new BigDecimal("9.25");
    private static final BigDecimal NEUTRAL_LONG = new BigDecimal("7.25");
    private static final BigDecimal DOWN = new BigDecimal("0.92");
    private static final BigDecimal NEUTRAL = new BigDecimal("0.9825");
    private static final LocalTime START = LocalTime.of(9, 25);

    private boolean marketShort;
    private boolean marketBuy;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal lastHigh;
    private BigDecimal lastLow;

    public TheLyon(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.longPos = null;
	this.marketShort = false;
	this.marketBuy = false;
	this.shortPos = null;
	this.lastHigh = new BigDecimal("9999");
	this.lastLow = new BigDecimal("-9999");
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
	}
    }

    @Override
    public void strategy(Bar bar) {
	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == -1
		&& bar.getLow().compareTo(lastLow.subtract(BEAR_SHORT)) <= 0 && getMarketPosition() >= 0
		&& bar.getClose().compareTo(DOWN.multiply(highY(0))) <= 0) {
	    marketShort = true;
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == 1
		&& bar.getHigh().compareTo(lastHigh.add(BEAR_LONG)) >= 0 && getMarketPosition() <= 0
		&& bar.getClose().compareTo(DOWN.multiply(highY(0))) <= 0) {
	    marketBuy = true;
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == -1
		&& bar.getLow().compareTo(lastLow.subtract(BULL_SHORT)) <= 0 && getMarketPosition() >= 0
		&& bar.getClose().compareTo(DOWN.multiply(highY(0))) == 1) {
	    marketShort = true;
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == 1
		&& bar.getHigh().compareTo(lastHigh.add(BULL_LONG)) >= 0 && getMarketPosition() <= 0
		&& bar.getClose().compareTo(DOWN.multiply(highY(0))) == 1) {
	    marketBuy = true;
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == -1
		&& bar.getLow().compareTo(lastLow.subtract(NEUTRAL_SHORT)) <= 0 && getMarketPosition() >= 0
		&& bar.getClose().compareTo(DOWN.multiply(highY(0))) == 1
		&& bar.getClose().compareTo(NEUTRAL.multiply(highY(0))) <= 0) {
	    marketShort = true;
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == 1
		&& bar.getHigh().compareTo(lastHigh.add(NEUTRAL_LONG)) >= 0 && getMarketPosition() <= 0
		&& bar.getClose().compareTo(DOWN.multiply(highY(0))) == 1
		&& bar.getClose().compareTo(NEUTRAL.multiply(highY(0))) <= 0) {
	    marketBuy = true;
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == -1) {
	    lastHigh = bar.getHigh();
	}

	if (bar.getClose().subtract(bar.getOpen()).compareTo(BigDecimal.ZERO) == 1) {
	    lastLow = bar.getLow();
	}
    }

    private int getMarketPosition() {
	int mp = 0;

	if (longPos != null && shortPos == null) {
	    mp = 1;
	} else if (longPos == null && shortPos != null) {
	    mp = -1;
	}

	return mp;
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
	return LocalDateTime.of(getStartDate(), START);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), START);
    }
}
