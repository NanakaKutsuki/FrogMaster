package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Inputs2;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategy extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("12500");
    private static final BigDecimal MAINTENANCE_MARGIN = new BigDecimal("4500");
    private static final LocalTime SEVEN_FIFTY_FIVE = LocalTime.of(7, 55);
    private static final LocalTime START = LocalTime.of(7, 59);
    private static final LocalTime END = LocalTime.of(15, 45);

    private boolean initialized;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal lastPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private Input input;
    private LocalDateTime buyDateTime;

    public HybridStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	super(ticker, barMap, bankrollBar);
	this.buyDateTime = LocalDateTime.of(getStartDate(), SEVEN_FIFTY_FIVE);
	this.initialized = false;
	this.input = Inputs2.getInputFromLastYear(ticker.getYear());
	this.lastMom = null;
	this.lastPos = null;
	this.longPos = null;
	this.shortPos = null;
    }

    @Override
    public BigDecimal getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public BigDecimal getMaintenanceMargin() {
	return MAINTENANCE_MARGIN;
    }

    @Override
    public void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getDateTime().isEqual(buyDateTime)) {
		longPos = getNextBar().getOpen();
		lastPos = getNextBar().getOpen();
		initialized = true;
	    }
	} else {
	    if (isDay(bar)) {
		BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());

		if (lastMom != null) {
		    BigDecimal accel = mom.subtract(lastMom);

		    if (shortPos == null && mom.compareTo(input.getMomST()) == -1
			    && accel.compareTo(input.getAccelST()) == -1) {
			shortPos = getNextBar().getOpen();
			lastPos = getNextBar().getOpen();
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
		    }
		}

		lastMom = mom;
	    }
	}
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal total = BigDecimal.ZERO;

	if (longPos != null && shortPos != null) {
	    BigDecimal realized = shortPos.subtract(longPos);
	    addBankroll(realized);
	    total = total.add(convertTicks(realized));
	    total = payCommission(total);
	    lastPos = shortPos;
	    longPos = null;
	}

	if (longPos == null && shortPos != null) {
	    if (isDay(bar) && isStopLoss(bar)) {
		BigDecimal realized = shortPos.subtract(getNextBar().getOpen());
		addBankroll(realized);
		total = total.add(convertTicks(realized));
		total = payCommission(total);
		longPos = getNextBar().getOpen();
		lastPos = getNextBar().getOpen();
		shortPos = null;
	    } else if (isLimit(getNextBar())) {
		BigDecimal gain = lowPrice;
		if (lowPrice.compareTo(getNextBar().getOpen()) == 1) {
		    gain = getNextBar().getOpen();
		}

		BigDecimal realized = shortPos.subtract(gain);
		addBankroll(realized);
		total = total.add(convertTicks(realized));
		total = payCommission(total);
		longPos = gain;
		lastPos = gain;
		shortPos = null;
	    }
	}

	return total;
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getLow().subtract(longPos);
	} else if (shortPos != null) {
	    unrealized = shortPos.subtract(bar.getHigh());
	}

	return convertTicks(unrealized);
    }

    @Override
    public void rebalance(Bar bar) {
	if (longPos != null) {
	    BigDecimal realized = bar.getClose().subtract(lastPos);

	    if (rebalancePrecheck(realized)) {
		addAndRebalance(realized);
		lastPos = bar.getClose();
	    }
	} else if (shortPos != null) {
	    BigDecimal realized = lastPos.subtract(bar.getClose());

	    if (rebalancePrecheck(realized)) {
		addAndRebalance(realized);
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
	return LocalDateTime.of(getEndDate(), LocalTime.of(9, 25));
    }

    private boolean isDay(Bar bar) {
	return !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)
		&& !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)
		&& bar.getDateTime().toLocalTime().isAfter(START) && bar.getDateTime().toLocalTime().isBefore(END);
    }

    private boolean isStopLoss(Bar bar) {
	return bar.getClose().compareTo(highPrice) >= 0;
    }

    private boolean isLimit(Bar bar) {
	return bar.getLow().compareTo(lowPrice) <= 0;
    }
}
