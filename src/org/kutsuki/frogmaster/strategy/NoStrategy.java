package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Ticker;

public class NoStrategy extends AbstractStrategy {
    public NoStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
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
