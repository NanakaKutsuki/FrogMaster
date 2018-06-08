package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Ticker;

public class LongStrategy2 extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("12000");
    private static final BigDecimal LONG_SAFETY = new BigDecimal("46.75");
    private static final BigDecimal STOP_LIMIT = new BigDecimal("14.25");
    private static final LocalTime START_TIME = LocalTime.of(18, 25);
    private static final LocalTime END_TIME = LocalTime.of(16, 00);

    private boolean marketBuy;
    private boolean marketSell;
    private BigDecimal stopPrice;
    private BigDecimal longPrice;
    private BigDecimal longPos;

    public LongStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.longPos = null;
	this.longPrice = BigDecimal.ZERO;
	this.marketBuy = false;
	this.marketSell = false;
	this.stopPrice = null;
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

	return margin;
    }

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (marketBuy) {
	    longPos = bar.getOpen();
	    marketBuy = false;
	}

	if (marketSell) {
	    if (longPos != null) {
		addBankroll(bar, bar.getOpen().subtract(longPos));
		longPos = null;
		stopPrice = null;
	    }

	    marketSell = false;
	}

	if (stopPrice != null && bar.getLow().compareTo(stopPrice) <= 0) {
	    BigDecimal stop = stopPrice;
	    if (stopPrice.compareTo(bar.getOpen()) == 1) {
		stop = bar.getOpen();
	    }

	    addBankroll(bar, stop.subtract(longPos));
	    longPos = null;
	    stopPrice = null;
	}
    }

    @Override
    public void strategy(Bar bar) {
	LocalTime time = bar.getDateTime().toLocalTime();

	if (time.equals(START_TIME)) {
	    marketBuy = true;
	    longPrice = bar.getClose();
	    stopPrice = bar.getClose().subtract(STOP_LIMIT);
	} else if ((time.isAfter(START_TIME) && time.isBefore(LocalTime.MAX))
		|| ((time.equals(LocalTime.MIN) || time.isAfter(LocalTime.MIN)) && time.isBefore(END_TIME))) {
	    if (bar.getClose().compareTo(longPrice.add(LONG_SAFETY)) == 1) {
		marketSell = true;
	    }
	} else if (longPos != null) {
	    marketSell = true;
	    stopPrice = null;
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getClose().subtract(longPos);
	}

	return unrealized;
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), getEightAM());
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), getEightAM());
    }
}
