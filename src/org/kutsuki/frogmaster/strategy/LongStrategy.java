package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Ticker;

public class LongStrategy extends AbstractStrategy {
    private BigDecimal holding;
    private LocalDateTime buyDateTime;
    private LocalDateTime sellDateTime;

    public LongStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.holding = null;
	this.buyDateTime = LocalDateTime.of(getStartDate(), LocalTime.of(8, 0));
	this.sellDateTime = LocalDateTime.of(getEndDate(), LocalTime.of(8, 0));
    }

    @Override
    public void strategy(Bar bar) {
	if (holding == null && bar.getDateTime().isEqual(buyDateTime)) {
	    holding = bar.getClose();
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (holding != null && !bar.getDateTime().isEqual(sellDateTime)) {
	    unrealized = bar.getClose().subtract(holding);
	}

	return convertTicks(unrealized);
    }

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (holding != null && bar.getDateTime().isEqual(sellDateTime)) {
	    realized = bar.getClose().subtract(holding);
	    realized = payCommission(convertTicks(realized));
	    holding = null;
	}

	return realized;
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
