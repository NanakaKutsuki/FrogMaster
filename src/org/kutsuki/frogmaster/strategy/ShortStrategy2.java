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

public class ShortStrategy2 extends AbstractStrategy {
    private static final LocalTime START = LocalTime.of(7, 59);
    private static final LocalTime END = LocalTime.of(15, 45);

    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastLongPos;
    private BigDecimal lastMom;
    private BigDecimal lastShortPos;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private Input input;
    private LocalDateTime buyDateTime;
    private LocalDateTime sellDateTime;
    private LocalDateTime shortEndDateTime;

    public ShortStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	super(ticker, barMap, bankrollBar);
	this.buyDateTime = LocalDateTime.of(getStartDate(), getEightAM());
	this.highPrice = null;
	this.input = Inputs2.getInputFromLastYear(ticker.getYear());
	this.lastLongPos = null;
	this.lastMom = null;
	this.lastShortPos = null;
	this.longPos = null;
	this.lowPrice = null;
	this.shortPos = null;
	this.sellDateTime = getEndDateTime();
	this.shortEndDateTime = LocalDateTime.of(getEndDate(), LocalTime.MIDNIGHT);
    }

    @Override
    public void strategy(Bar bar) {
	if (longPos == null && bar.getDateTime().isEqual(buyDateTime)) {
	    longPos = bar.getClose();
	    lastLongPos = bar.getClose();
	}

	if (isDay(bar)) {
	    BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());

	    if (lastMom != null) {
		BigDecimal accel = mom.subtract(lastMom);

		if (shortPos == null && mom.compareTo(input.getMomST()) == -1
			&& accel.compareTo(input.getAccelST()) == -1) {
		    shortPos = getNextBar().getOpen();
		    lastShortPos = getNextBar().getOpen();
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		}
	    }

	    lastMom = mom;
	}
    }

    private boolean isDay(Bar bar) {
	return !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)
		&& !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)
		&& bar.getDateTime().isBefore(shortEndDateTime) && bar.getDateTime().toLocalTime().isAfter(START)
		&& bar.getDateTime().toLocalTime().isBefore(END);
    }

    private boolean isStopLoss(Bar bar) {
	return bar.getClose().compareTo(highPrice) >= 0;
    }

    private boolean isLimit(Bar bar) {
	return bar.getLow().compareTo(lowPrice) <= 0;
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null && !bar.getDateTime().isEqual(sellDateTime)) {
	    unrealized = unrealized.add(bar.getClose().subtract(longPos));
	}

	if (shortPos != null && !(isDay(bar) && (isStopLoss(bar) || isLimit(bar)))) {
	    unrealized = unrealized.add(shortPos.subtract(bar.getClose()));
	}

	return convertTicks(unrealized);
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal total = BigDecimal.ZERO;

	if (longPos != null && bar.getDateTime().isEqual(sellDateTime)) {
	    BigDecimal realized = bar.getClose().subtract(longPos);
	    addBankroll(realized);
	    total = total.add(convertTicks(realized));
	    total = payCommission(total);

	    longPos = null;
	    lastLongPos = null;
	}

	if (shortPos != null) {
	    if (isDay(bar) && isStopLoss(bar)) {
		BigDecimal realized = shortPos.subtract(getNextBar().getOpen());
		addBankroll(realized);
		total = total.add(convertTicks(realized));
		total = payCommission(total);

		shortPos = null;
		lastShortPos = null;

		System.out.println(debug("BuyLose", getNextBar().getOpen()));
	    }

	    if (isLimit(getNextBar())) {
		BigDecimal gain = lowPrice;
		if (lowPrice.compareTo(getNextBar().getOpen()) == 1) {
		    gain = getNextBar().getOpen();
		}

		BigDecimal realized = shortPos.subtract(gain);
		addBankroll(realized);
		total = total.add(convertTicks(realized));
		total = payCommission(total);

		shortPos = null;
		lastShortPos = null;
	    }
	}

	return total;
    }

    @Override
    public void rebalance() {
	BigDecimal realized = BigDecimal.ZERO;

	if (longPos != null) {
	    realized = realized.add(getNextBar().getOpen().subtract(lastLongPos));
	}

	if (shortPos != null) {
	    realized = realized.add(lastShortPos.subtract(getNextBar().getOpen()));
	}

	if (rebalancePrecheck(realized)) {
	    if (longPos != null) {
		lastLongPos = getNextBar().getOpen();
	    }

	    if (shortPos != null) {
		lastShortPos = getNextBar().getOpen();
	    }

	    addBankrollBar(realized);
	}
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
