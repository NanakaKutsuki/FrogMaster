package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Ticker;

public class LongStrategy extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("20000");
    private static final BigDecimal COST_PER_CONTRACT_BAR = new BigDecimal("21000");

    private BigDecimal lastLongPos;
    private BigDecimal longPos;
    private LocalDateTime buyDateTime;
    private LocalDateTime sellDateTime;

    public LongStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	super(ticker, barMap, bankrollBar);
	this.buyDateTime = LocalDateTime.of(getStartDate(), getEightAM());
	this.lastLongPos = null;
	this.longPos = null;
	this.sellDateTime = getEndDateTime();
	setCostPerContractBar(COST_PER_CONTRACT_BAR);
    }

    @Override
    public BigDecimal getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public BigDecimal getStrategyMargin() {
	BigDecimal margin = BigDecimal.ZERO;

	if (longPos != null) {
	    margin = margin.add(getMaintenanceMargin());
	}

	return margin;
    }

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (longPos != null && bar.getDateTime().isEqual(sellDateTime)) {
	    addBankroll(bar.getClose().subtract(longPos));
	    addBankrollBar(bar.getClose().subtract(lastLongPos));
	    longPos = null;
	    lastLongPos = null;
	}
    }

    @Override
    public void strategy(Bar bar) {
	if (longPos == null && bar.getDateTime().isEqual(buyDateTime)) {
	    longPos = bar.getClose();
	    lastLongPos = bar.getClose();
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getClose().subtract(longPos);
	}

	return unrealized;
    }

    @Override
    public BigDecimal getUnrealizedBar(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getClose().subtract(lastLongPos);
	}

	return unrealized;
    }

    @Override
    public void checkRebalance(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (longPos != null) {
	    realized = realized.add(bar.getClose().subtract(lastLongPos));
	}

	if (isRebalance(realized)) {
	    if (longPos != null) {
		lastLongPos = bar.getClose();
	    }

	    addBankrollBar(realized);
	    rebalance();
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
