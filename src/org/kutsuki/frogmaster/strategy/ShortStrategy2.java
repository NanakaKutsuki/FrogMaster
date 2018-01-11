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

    private BigDecimal holding;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private BigDecimal lastPos;
    private Input input;

    public ShortStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.holding = null;
	this.input = Inputs2.getInputFromLastYear(ticker.getYear());
	this.lastMom = null;
    }

    @Override
    public void strategy(Bar bar) {
	if (isDay(bar)) {
	    BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());

	    if (lastMom != null) {
		BigDecimal accel = mom.subtract(lastMom);

		if (holding == null && mom.compareTo(input.getMomST()) == -1
			&& accel.compareTo(input.getAccelST()) == -1) {
		    holding = getNextBar().getOpen();
		    lastPos = getNextBar().getOpen();
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
	BigDecimal unrealized = BigDecimal.ZERO;

	if (holding != null && !(isDay(bar) && (isStopLoss(bar) || isLimit(bar)))) {
	    unrealized = holding.subtract(bar.getClose());
	}

	return convertTicks(unrealized);
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (holding != null) {
	    if (isDay(bar) && isStopLoss(bar)) {
		realized = holding.subtract(getNextBar().getOpen());
		addBankroll(realized);
		realized = payCommission(convertTicks(realized));

		holding = null;
		lastPos = null;
	    }

	    if (isLimit(getNextBar())) {
		BigDecimal gain = lowPrice;
		if (lowPrice.compareTo(getNextBar().getOpen()) == 1) {
		    gain = getNextBar().getOpen();
		}

		realized = holding.subtract(gain);
		addBankroll(realized);
		realized = payCommission(convertTicks(realized));

		holding = null;
		lastPos = null;
	    }
	}

	return realized;
    }

    @Override
    public void rebalance() {
	if (holding != null) {
	    BigDecimal realized = lastPos.subtract(getNextBar().getOpen());
	    addBankrollBar(realized);
	    lastPos = getNextBar().getOpen();
	}
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
