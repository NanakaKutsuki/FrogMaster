package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Input;

public class HybridStrategy extends AbstractStrategy {
    private static final LocalTime START = LocalTime.of(7, 59);
    private static final LocalTime END = LocalTime.of(15, 41);

    private boolean init;
    private BigDecimal longg;
    private BigDecimal shortt;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Input input;

    public HybridStrategy(TreeMap<LocalDateTime, Bar> barMap, Input input) {
	super(barMap);
	this.longg = null;
	this.init = false;
	this.input = input;
	this.shortt = null;
    }

    @Override
    public void strategy(Bar bar) {
	if (isDay(bar)) {
	    if (!init) {
		longg = bar.getClose();
		this.init = true;
	    }

	    Bar bar9 = getPrevBar(9);

	    if (bar9 != null) {
		Bar bar1 = getPrevBar(1);
		Bar bar8 = getPrevBar(8);

		BigDecimal mom = bar.getClose().subtract(bar8.getClose());
		BigDecimal accel = mom.subtract(bar1.getClose().subtract(bar9.getClose()));

		if (shortt == null && mom.compareTo(input.getMomST()) == -1
			&& accel.compareTo(input.getAccelST()) == -1) {
		    shortt = getNextBar().getOpen();
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

	if (longg != null) {
	    unrealized = bar.getClose().subtract(longg);
	}

	if (shortt != null && !(isDay(bar) && (isStopLoss(bar) || isLimit(bar)))) {
	    unrealized = shortt.subtract(bar.getClose());
	}

	return unrealized;
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (isDay(bar)) {
	    if (longg != null && shortt != null) {
		realized = shortt.subtract(longg);
		longg = null;
	    }

	    if (shortt != null && longg == null) {
		if (isStopLoss(bar)) {
		    realized = shortt.subtract(getNextBar().getOpen());
		    shortt = null;
		    longg = getNextBar().getOpen();
		} else if (isLimit(bar)) {
		    realized = shortt.subtract(bar.getLow());
		    shortt = null;
		    longg = bar.getLow();
		}
	    }
	}

	return realized;
    }
}
