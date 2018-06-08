package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.HybridInputs;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategy extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("17000");
    private static final LocalTime END = LocalTime.of(15, 45);
    private static final LocalTime NINE_THIRTYFIVE = LocalTime.of(9, 35);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);
    private static final LocalTime START = LocalTime.of(9, 29);

    private boolean initialized;
    private boolean coverLong;
    private boolean coverShort;
    private boolean marketShort;
    private boolean marketBuy;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private Input input;
    private LocalDateTime buyDateTime;

    public HybridStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.buyDateTime = LocalDateTime.of(getStartDate(), NINE_TWENTYFIVE);
	this.coverLong = false;
	this.coverShort = false;
	this.initialized = false;
	this.input = HybridInputs.getInputFromLastYear(ticker.getYear());
	this.lastMom = BigDecimal.ZERO;
	this.longPos = null;
	this.marketShort = false;
	this.marketBuy = false;
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

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (coverShort) {
	    shortPos = bar.getOpen();
	    coverShort = false;
	    marketShort = false;
	}

	if (coverLong) {
	    longPos = bar.getOpen();
	    coverLong = false;
	    marketBuy = false;
	}

	if (marketBuy) {
	    if (shortPos != null) {
		addBankroll(bar, shortPos.subtract(bar.getOpen()));

		longPos = bar.getOpen();
		shortPos = null;
	    } else {
		longPos = bar.getOpen();
		shortPos = null;
	    }

	    initialized = true;
	    marketBuy = false;
	}

	if (marketShort) {
	    if (longPos != null) {
		addBankroll(bar, bar.getOpen().subtract(longPos));
		longPos = null;
	    }

	    shortPos = bar.getOpen();
	    marketShort = false;
	}

	if (isLimit(bar)) {
	    BigDecimal gain = lowPrice;
	    if (lowPrice.compareTo(bar.getOpen()) == 1) {
		gain = bar.getOpen();
	    }

	    addBankroll(bar, shortPos.subtract(gain));
	    shortPos = null;

	    BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());
	    BigDecimal accel = mom.subtract(lastMom);
	    lastMom = mom;

	    if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
		highPrice = bar.getClose().add(input.getUpAmount());
		lowPrice = bar.getClose().subtract(input.getDownAmount());
		coverShort = true;
	    } else {
		coverLong = true;
	    }
	}
    }

    @Override
    public void strategy(Bar bar) {
	if (!initialized) {
	    marketBuy = bar.getDateTime().isEqual(buyDateTime);
	} else {
	    if (isDay(bar)) {
		BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());
		BigDecimal accel = mom.subtract(lastMom);

		if (shortPos == null && mom.compareTo(input.getMomST()) == -1
			&& accel.compareTo(input.getAccelST()) == -1) {
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		    marketShort = true;
		}

		if (!marketBuy && isStopLoss(bar)) {
		    if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
			marketBuy = false;
		    } else {
			marketBuy = true;
		    }
		}

		lastMom = mom;
	    }
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getLow().subtract(longPos);
	} else if (shortPos != null) {
	    unrealized = shortPos.subtract(bar.getHigh());
	}

	return unrealized;
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), NINE_TWENTYFIVE);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), NINE_THIRTYFIVE);
    }

    private boolean isDay(Bar bar) {
	return bar.getDateTime().toLocalTime().isAfter(START) && bar.getDateTime().toLocalTime().isBefore(END);
    }

    private boolean isStopLoss(Bar bar) {
	return shortPos != null && bar.getClose().compareTo(highPrice) >= 0;
    }

    private boolean isLimit(Bar bar) {
	return shortPos != null && bar.getLow().compareTo(lowPrice) <= 0;
    }
}
