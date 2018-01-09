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
    private static final LocalTime START = LocalTime.of(7, 59);
    private static final LocalTime END = LocalTime.of(15, 45);

    private boolean initialized;
    private boolean sell;
    private BigDecimal holding;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private Input input;
    private LocalDateTime buyDateTime;
    private LocalDateTime sellDateTime;

    public HybridStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.buyDateTime = LocalDateTime.of(getStartDate(), LocalTime.of(8, 0));
	this.holding = null;
	this.initialized = false;
	this.input = Inputs2.getInputFromLastYear(ticker.getYear());
	this.lastMom = null;
	this.sell = false;
	this.sellDateTime = LocalDateTime.of(getEndDate(), LocalTime.of(8, 0));
    }

    @Override
    public void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getDateTime().isEqual(buyDateTime)) {
		holding = bar.getClose();
		initialized = true;
	    }
	} else {
	    if (isDay(bar)) {
		BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());

		if (lastMom != null) {
		    BigDecimal accel = mom.subtract(lastMom);

		    if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
			sell = true;
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
		    }
		}

		lastMom = mom;
	    }
	}
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

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (bar.getDateTime().isEqual(sellDateTime) || sell) {
	    realized = getNextBar().getOpen().subtract(holding);
	    holding = getNextBar().getOpen();
	    sell = false;
	} else if (highPrice != null && lowPrice != null) {
	    if (isDay(bar) && isStopLoss(bar)) {
		realized = holding.subtract(getNextBar().getOpen());
		holding = getNextBar().getOpen();
		highPrice = null;
		lowPrice = null;
	    } else if (isLimit(getNextBar())) {
		BigDecimal gain = lowPrice;
		if (lowPrice.compareTo(getNextBar().getOpen()) == 1) {
		    gain = getNextBar().getOpen();
		}

		realized = holding.subtract(gain);
		holding = getNextBar().getOpen();
		highPrice = null;
		lowPrice = null;
	    }
	}

	return realized;
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), getEightAM());
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), LocalTime.MIDNIGHT);
    }
}
