package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.ShortInputs;
import org.kutsuki.frogmaster.Ticker;

public class ShortStrategy2 extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("20000");
    private static final LocalTime START = LocalTime.of(7, 59);
    private static final LocalTime END = LocalTime.of(15, 45);

    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private boolean marketShort;
    private boolean marketBuyToCover;
    private Input input;
    private LocalDateTime buyDateTime;
    private LocalDateTime sellDateTime;
    private LocalDateTime shortEndDateTime;

    public ShortStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.buyDateTime = LocalDateTime.of(getStartDate(), getEightAM());
	this.highPrice = null;
	this.input = ShortInputs.getInputFromLastYear(ticker.getYear());
	this.marketShort = false;
	this.marketBuyToCover = false;
	this.lastMom = null;
	this.longPos = null;
	this.lowPrice = null;
	this.shortPos = null;
	this.sellDateTime = getEndDateTime();
	this.shortEndDateTime = LocalDateTime.of(getEndDate(), LocalTime.MIDNIGHT);
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

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (longPos != null && bar.getDateTime().isEqual(sellDateTime)) {
	    addBankroll(bar, bar.getClose().subtract(longPos));
	    longPos = null;
	}

	if (marketShort) {
	    shortPos = bar.getOpen();
	    marketShort = false;
	}

	if (marketBuyToCover) {
	    addBankroll(bar, shortPos.subtract(bar.getOpen()));
	    shortPos = null;
	}

	if (isLimit(bar)) {
	    BigDecimal gain = lowPrice;
	    if (lowPrice.compareTo(bar.getOpen()) == 1) {
		gain = bar.getOpen();
	    }

	    addBankroll(bar, shortPos.subtract(gain));

	    shortPos = null;
	}
    }

    @Override
    public void strategy(Bar bar) {
	if (longPos == null && bar.getDateTime().isEqual(buyDateTime)) {
	    longPos = bar.getClose();
	}

	if (isDay(bar)) {
	    BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());

	    if (lastMom != null) {
		BigDecimal accel = mom.subtract(lastMom);

		if (shortPos == null && mom.compareTo(input.getMomST()) == -1
			&& accel.compareTo(input.getAccelST()) == -1) {
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		    marketShort = true;
		}
	    }

	    marketBuyToCover = isStopLoss(bar);
	    lastMom = mom;
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = unrealized.add(bar.getLow().subtract(longPos));
	}

	if (shortPos != null) {
	    unrealized = unrealized.add(shortPos.subtract(bar.getHigh()));
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

    private boolean isDay(Bar bar) {
	return !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)
		&& !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)
		&& bar.getDateTime().isBefore(shortEndDateTime) && bar.getDateTime().toLocalTime().isAfter(START)
		&& bar.getDateTime().toLocalTime().isBefore(END);
    }

    private boolean isStopLoss(Bar bar) {
	return shortPos != null && bar.getClose().compareTo(highPrice) >= 0;
    }

    private boolean isLimit(Bar bar) {
	return shortPos != null && bar.getLow().compareTo(lowPrice) <= 0;
    }
}
