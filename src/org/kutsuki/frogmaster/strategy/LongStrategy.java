package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;

public class LongStrategy extends AbstractStrategy {
    private LocalTime START = LocalTime.of(8, 0);

    private BigDecimal holding = null;

    public LongStrategy(TreeMap<LocalDateTime, Bar> barMap) {
	super(barMap);
    }

    @Override
    public void strategy(Bar bar) {
	if (holding == null && !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)
		&& !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)
		&& bar.getDateTime().toLocalTime().equals(START)) {
	    holding = bar.getClose();
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (holding != null && !isSell(bar)) {
	    unrealized = bar.getClose().subtract(holding);
	}

	return unrealized;
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (holding != null && isSell(bar)) {
	    realized = bar.getClose().subtract(holding);
	    holding = null;
	}

	return realized;
    }

    private boolean isSell(Bar bar) {
	return bar.getDateTime().isEqual(getLastBar());
    }
}
