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
    private static final LocalTime END = LocalTime.of(15, 41);

    private BigDecimal holding;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Input input;

    public ShortStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.holding = null;
	this.input = Inputs2.getInputFromLastYear(ticker.getYear());
    }

    @Override
    public void strategy(Bar bar) {
	if (isDay(bar)) {
	    Bar bar9 = getPrevBar(9);

	    if (bar9 != null) {
		Bar bar1 = getPrevBar(1);
		Bar bar8 = getPrevBar(8);

		BigDecimal mom = bar.getClose().subtract(bar8.getClose());
		BigDecimal accel = mom.subtract(bar1.getClose().subtract(bar9.getClose()));

		if (holding == null && mom.compareTo(input.getMomST()) == -1
			&& accel.compareTo(input.getAccelST()) == -1) {
		    holding = getNextBar().getOpen();
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		}
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
	BigDecimal unrealized = BigDecimal.ZERO;

	if (holding != null && !(isDay(bar) && (isStopLoss(bar) || isLimit(bar)))) {
	    unrealized = holding.subtract(bar.getClose());
	}

	return unrealized;
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (isDay(bar) && holding != null) {
	    if (isStopLoss(bar)) {
		realized = holding.subtract(getNextBar().getOpen());
		holding = null;
	    } else if (isLimit(bar)) {
		realized = holding.subtract(bar.getLow());
		holding = null;
	    }
	}

	return realized;
    }
}
