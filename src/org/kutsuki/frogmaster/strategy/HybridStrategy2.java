package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.HybridInputs2;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategy2 extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("16000");
    private static final BigDecimal COST_PER_CONTRACT_BAR = new BigDecimal("10000");
    private static final LocalTime END = LocalTime.of(15, 45);
    private static final LocalTime NINE_THIRTY = LocalTime.of(9, 30);
    private static final LocalTime SEVEN_FIFTY_FIVE = LocalTime.of(7, 55);
    private static final LocalTime START = LocalTime.of(7, 59);

    private boolean initialized;
    private boolean coverLong;
    private boolean coverLose;
    private boolean coverShort;
    private boolean marketShort;
    private boolean marketBuy;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal lastPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private Input input;
    private LocalDateTime buyDateTime;

    public HybridStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	super(ticker, barMap, bankrollBar);
	this.buyDateTime = LocalDateTime.of(getStartDate(), SEVEN_FIFTY_FIVE);
	this.coverLong = false;
	this.coverLose = false;
	this.coverShort = false;
	this.initialized = false;
	this.input = HybridInputs2.getInputFromLastYear(ticker.getYear());
	this.lastMom = BigDecimal.ZERO;
	this.lastPos = null;
	this.longPos = null;
	this.marketShort = false;
	this.marketBuy = false;
	this.shortPos = null;
	setCostPerContractBar(COST_PER_CONTRACT_BAR);
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
	    lastPos = bar.getOpen();
	    coverShort = false;
	    marketShort = false;
	}

	if (coverLong) {
	    longPos = bar.getOpen();
	    lastPos = bar.getOpen();
	    coverLong = false;
	    marketBuy = false;
	}

	if (coverLose) {
	    addBankroll(shortPos.subtract(bar.getOpen()));
	    addBankrollBar(lastPos.subtract(bar.getOpen()));

	    shortPos = bar.getOpen();
	    lastPos = bar.getOpen();
	    coverLose = false;
	}

	if (marketBuy) {
	    if (shortPos != null) {
		addBankroll(shortPos.subtract(bar.getOpen()));
		addBankrollBar(lastPos.subtract(bar.getOpen()));

		longPos = bar.getOpen();
		lastPos = bar.getOpen();
		shortPos = null;
	    } else {
		longPos = bar.getOpen();
		lastPos = bar.getOpen();
		shortPos = null;
	    }

	    initialized = true;
	    marketBuy = false;
	}

	if (marketShort) {
	    if (longPos != null) {
		addBankroll(bar.getOpen().subtract(longPos));
		addBankrollBar(bar.getOpen().subtract(lastPos));
		longPos = null;
	    }

	    shortPos = bar.getOpen();
	    lastPos = bar.getOpen();
	    marketShort = false;
	}

	if (isLimit(bar)) {
	    BigDecimal gain = lowPrice;
	    if (lowPrice.compareTo(bar.getOpen()) == 1) {
		gain = bar.getOpen();
	    }

	    addBankroll(shortPos.subtract(gain));
	    addBankrollBar(lastPos.subtract(gain));
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

		if (!marketBuy) {
		    marketBuy = isStopLoss(bar);

		    if (marketBuy && mom.compareTo(input.getMomST()) == -1
			    && accel.compareTo(input.getAccelST()) == -1) {
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
			marketBuy = false;
			coverLose = true;
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
    public BigDecimal getUnrealizedBar(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getLow().subtract(lastPos);
	} else if (shortPos != null) {
	    unrealized = lastPos.subtract(bar.getHigh());
	}

	return unrealized;
    }

    @Override
    public void checkRebalance(Bar bar) {
	if (longPos != null) {
	    BigDecimal realized = bar.getClose().subtract(lastPos);

	    if (isRebalance(realized)) {
		addBankrollBar(realized);
		rebalance();
		lastPos = bar.getClose();
	    }
	} else if (shortPos != null) {
	    BigDecimal realized = lastPos.subtract(bar.getClose());

	    if (isRebalance(realized)) {
		addBankrollBar(realized);
		rebalance();
		lastPos = bar.getClose();
	    }
	}
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), SEVEN_FIFTY_FIVE);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), NINE_THIRTY);
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
