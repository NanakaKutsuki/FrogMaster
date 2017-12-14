package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;

public class NoStrategy extends AbstractStrategy {
    public NoStrategy(TreeMap<LocalDateTime, Bar> barMap) {
	super(barMap);
    }

    @Override
    public void strategy(Bar bar) {
	// do nothing
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	return BigDecimal.ZERO;
    }
}
