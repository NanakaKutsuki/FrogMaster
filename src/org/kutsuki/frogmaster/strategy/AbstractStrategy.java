package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Equity;
import org.kutsuki.frogmaster.Ticker;
import org.kutsuki.frogmaster.Tradestation;

public abstract class AbstractStrategy {
    private static final BigDecimal FIFTY = new BigDecimal("50");
    private static final LocalTime EIGHT_AM = LocalTime.of(8, 0);

    private BigDecimal bankroll;
    private int count;
    private int index;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<LocalDateTime> keyList;
    private TreeMap<LocalDateTime, Bar> barMap;
    private TreeMap<LocalDateTime, Equity> equityMap;

    public abstract void strategy(Bar bar);

    public abstract BigDecimal getUnrealized(Bar bar);

    public abstract BigDecimal getRealized(Bar bar);

    public abstract LocalDateTime getStartDateTime();

    public abstract LocalDateTime getEndDateTime();

    public AbstractStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	this.bankroll = BigDecimal.ZERO;
	this.barMap = barMap;
	this.count = 1;
	this.endDate = calcEndDate(ticker);
	this.equityMap = new TreeMap<LocalDateTime, Equity>();
	this.index = 0;
	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	this.startDate = calcStartDate(ticker);
	Collections.sort(this.keyList);
    }

    public void run() {
	Equity prevEquity = new Equity(null);

	for (LocalDateTime key = barMap.firstKey(); key.isBefore(barMap.lastKey())
		|| key.isEqual(barMap.lastKey()); key = key.plusMinutes(5)) {
	    boolean found = false;
	    Bar bar = barMap.get(key);

	    if (bar != null) {
		if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
		    strategy(bar);

		    Equity equity = new Equity(key);
		    BigDecimal realized = getRealized(bar);
		    equity.setRealized(prevEquity.getRealized().add(realized));
		    equity.setUnrealized(getUnrealized(bar));

		    equityMap.put(key, equity);
		    prevEquity = equity;
		    found = true;
		}

		index++;
	    }

	    if (!found) {
		equityMap.put(key, new Equity(key, prevEquity));
	    }
	}
    }

    public BigDecimal convertTicks(BigDecimal ticks) {
	return ticks.multiply(FIFTY);
    }

    public BigDecimal payCommission(BigDecimal realized) {
	// convertTicks first!
	return realized.subtract(Tradestation.COMMISSION).subtract(Tradestation.SLIPPAGE);
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

    public LocalDate getEndDate() {
	return endDate;
    }

    public LocalDate getStartDate() {
	return startDate;
    }

    public LocalTime getEightAM() {
	return EIGHT_AM;
    }

    public TreeMap<LocalDateTime, Equity> getEquityMap() {
	return equityMap;
    }

    public String debug(String name, BigDecimal price, BigDecimal realized) {
	StringBuilder sb = new StringBuilder();

	if (realized != null) {

	    for (int i = 0; i < Integer.toString(count).length() + 2; i++) {
		sb.append(' ');
	    }
	} else {
	    sb.append(count).append('.').append(' ');
	    count++;
	}

	sb.append(getNextBar().getDateTime()).append(' ');
	sb.append(name).append(' ');
	sb.append(price).append(' ');

	if (realized != null) {
	    sb.append(convertTicks(realized)).append(' ');
	    bankroll = bankroll.add(convertTicks(realized));
	    bankroll = bankroll.subtract(Tradestation.COMMISSION).subtract(Tradestation.SLIPPAGE);
	    sb.append(bankroll);
	}

	return sb.toString();
    }

    private LocalDate calcStartDate(Ticker ticker) {
	LocalDate date = null;

	switch (ticker.getMonth()) {
	case 'H':
	    date = LocalDate.of(ticker.getFullYear() - 1, 12, 1);
	    break;
	case 'M':
	    date = LocalDate.of(ticker.getFullYear(), 3, 1);
	    break;
	case 'U':
	    date = LocalDate.of(ticker.getFullYear(), 6, 1);
	    break;
	case 'Z':
	    date = LocalDate.of(ticker.getFullYear(), 9, 1);
	    break;
	default:
	    throw new IllegalArgumentException("Bad Ticker!" + ticker);
	}

	LocalDateTime dateTime = LocalDateTime.of(calcThirdDayOfWeek(date), EIGHT_AM);
	while (!barMap.containsKey(dateTime)) {
	    dateTime = dateTime.plusDays(1);
	}

	return dateTime.toLocalDate();
    }

    private LocalDate calcEndDate(Ticker ticker) {
	LocalDate date = null;

	switch (ticker.getMonth()) {
	case 'H':
	    date = LocalDate.of(ticker.getFullYear(), 3, 1);
	    break;
	case 'M':
	    date = LocalDate.of(ticker.getFullYear(), 6, 1);
	    break;
	case 'U':
	    date = LocalDate.of(ticker.getFullYear(), 9, 1);
	    break;
	case 'Z':
	    date = LocalDate.of(ticker.getFullYear(), 12, 1);
	    break;
	default:
	    throw new IllegalArgumentException("Bad Ticker!" + ticker);
	}

	LocalDateTime dateTime = LocalDateTime.of(calcThirdDayOfWeek(date), EIGHT_AM);
	while (!barMap.containsKey(dateTime)) {
	    dateTime = dateTime.minusDays(1);
	}

	return dateTime.toLocalDate();
    }

    private LocalDate calcThirdDayOfWeek(LocalDate start) {
	int week = 0;
	LocalDate i = start;
	LocalDate thirdDayOfWeek = null;

	while (thirdDayOfWeek == null) {
	    if (i.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
		week++;
	    }

	    if (week == 3) {
		thirdDayOfWeek = i;
	    }

	    i = i.plusDays(1);
	}

	return thirdDayOfWeek;
    }
}
