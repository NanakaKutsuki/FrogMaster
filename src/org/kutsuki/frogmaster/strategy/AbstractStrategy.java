package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Equity;

public abstract class AbstractStrategy {
    private static final BigDecimal FIFTY = new BigDecimal("50");
    private static final BigDecimal COMMISSION = new BigDecimal("5.38");

    private int index;
    private List<LocalDateTime> keyList;
    private TreeMap<LocalDateTime, Bar> barMap;
    private TreeMap<LocalDateTime, Equity> equityMap;

    public abstract void strategy(Bar bar);

    public abstract BigDecimal getUnrealized(Bar bar);

    public abstract BigDecimal getRealized(Bar bar);

    public AbstractStrategy(TreeMap<LocalDateTime, Bar> barMap) {
	this.barMap = barMap;
	this.equityMap = new TreeMap<LocalDateTime, Equity>();
	this.index = 0;
	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	Collections.sort(this.keyList);
    }

    public void run() {
	Equity prevEquity = new Equity(null);

	for (LocalDateTime key = barMap.firstKey(); key.isBefore(barMap.lastKey())
		|| key.isEqual(barMap.lastKey()); key = key.plusMinutes(5)) {
	    Bar bar = barMap.get(key);

	    if (bar != null) {
		strategy(bar);

		Equity equity = new Equity(key);

		BigDecimal realized = convertTicks(getRealized(bar));
		if (realized.compareTo(BigDecimal.ZERO) != 0) {
		    equity.setRealized(prevEquity.getRealized().add(realized).subtract(COMMISSION));
		} else {
		    equity.setRealized(prevEquity.getRealized());
		}

		equity.setUnrealized(convertTicks(getUnrealized(bar)));

		equityMap.put(key, equity);
		prevEquity = equity;
		// System.out.println(equity);
		index++;
	    } else {
		equityMap.put(key, new Equity(key, prevEquity));
	    }
	}
    }

    public LocalDateTime getLastBar() {
	return barMap.lastKey();
    }

    public Bar getPrevBar(int length) {
	Bar bar = null;

	if (index > length) {
	    bar = barMap.get(keyList.get(index - length));
	}

	return bar;
    }

    public Bar getNextBar() {
	Bar bar = null;

	if (index + 1 < keyList.size()) {
	    bar = barMap.get(keyList.get(index + 1));
	}

	return bar;
    }

    public TreeMap<LocalDateTime, Equity> getEquityMap() {
	return equityMap;
    }

    private BigDecimal convertTicks(BigDecimal ticks) {
	return ticks.multiply(FIFTY);
    }
}
