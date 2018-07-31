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
import org.kutsuki.frogmaster.Ticker;

public abstract class AbstractStrategy {
    private static final BigDecimal COMMISSION = new BigDecimal("5.38");
    private static final BigDecimal FIFTY = new BigDecimal("50");
    private static final BigDecimal MAINTENANCE_MARGIN = new BigDecimal("5600");
    private static final BigDecimal SLIPPAGE = new BigDecimal("0.50");
    private static final LocalTime EIGHT_AM = LocalTime.of(8, 0);

    private BigDecimal bankroll;
    private BigDecimal lowestEquity;
    private BigDecimal highY;
    private int index;
    private int lastYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime dateTime;
    private LocalDateTime lowestEquityDateTime;
    private List<LocalDateTime> keyList;
    private TreeMap<LocalDateTime, Bar> barMap;

    public abstract BigDecimal getCostPerContract();

    public abstract BigDecimal getStrategyMargin();

    public abstract BigDecimal getUnrealized(Bar bar);

    public abstract LocalDateTime getStartDateTime();

    public abstract LocalDateTime getEndDateTime();

    public abstract void resolveMarketOrders(Bar bar);

    public abstract void strategy(Bar bar);

    public AbstractStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	this.bankroll = BigDecimal.ZERO;
	this.barMap = barMap;
	this.dateTime = null;
	this.endDate = calcEndDate(ticker);
	this.index = 0;
	this.lastYear = 0;
	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	this.lowestEquity = BigDecimal.valueOf(100000);
	this.lowestEquityDateTime = barMap.firstKey();
	this.startDate = calcStartDate(ticker);
	Collections.sort(this.keyList);
    }

    public BigDecimal highY(int year) {
	return highY;
    }

    public void run() {
	for (LocalDateTime key : keyList) {
	    Bar bar = barMap.get(key);
	    dateTime = key;

	    if (dateTime.getYear() > lastYear) {
		highY = bar.getHigh();
		lastYear = dateTime.getYear();
	    }

	    if (bar.getHigh().compareTo(highY) == 1) {
		highY = bar.getHigh();
	    }

	    if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
		// resolve market orders first
		resolveMarketOrders(bar);

		// run strategy
		strategy(bar);

		// calculate unrealized
		BigDecimal unrealized = convertTicks(getUnrealized(bar));

		// calculate equity
		BigDecimal equity = getBankroll().add(unrealized);
		if (equity.compareTo(getLowestEquity()) == -1) {
		    this.lowestEquity = equity;
		    this.lowestEquityDateTime = key;
		}

		// margin check
		maintenanceMarginCheck(unrealized);
	    }

	    index++;
	}
    }

    public void addBankroll(Bar bar, BigDecimal realized) {
	this.bankroll = getBankroll().add(convertTicks(realized));
	this.bankroll = getBankroll().subtract(COMMISSION.add(SLIPPAGE));
    }

    public BigDecimal getBankroll() {
	return bankroll;
    }

    public LocalTime getEightAM() {
	return EIGHT_AM;
    }

    public LocalDate getEndDate() {
	return endDate;
    }

    public BigDecimal getLowestEquity() {
	return lowestEquity;
    }

    public LocalDateTime getLowestEquityDateTime() {
	return lowestEquityDateTime;
    }

    public BigDecimal getMaintenanceMargin() {
	return MAINTENANCE_MARGIN;
    }

    public Bar getPrevBar(int length) {
	Bar bar = null;

	if (index > length) {
	    bar = barMap.get(keyList.get(index - length));
	}

	return bar;
    }

    public LocalDate getStartDate() {
	return startDate;
    }

    private LocalDate calcStartDate(Ticker ticker) {
	LocalDate date = null;

	switch (ticker.getMonth()) {
	case 'A':
	    // hard coded for @ES
	    date = LocalDate.of(2005, 12, 1);
	    break;
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
	    throw new IllegalStateException("Bad Ticker!" + ticker);
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
	case 'A':
	    // hard coded for @ES
	    date = LocalDate.of(2018, 6, 1);
	    break;
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

    private BigDecimal convertTicks(BigDecimal ticks) {
	return ticks.multiply(FIFTY);
    }

    private void maintenanceMarginCheck(BigDecimal unrealized) {
	// Only check starting in 2009
	if (dateTime.getYear() > 2008) {
	    BigDecimal equity = getBankroll().add(unrealized).add(getCostPerContract());

	    if (equity.compareTo(getStrategyMargin()) == -1) {
		throw new IllegalStateException("Maintenance Margin Exceeded! " + dateTime + " " + getBankroll() + " "
			+ unrealized + " " + getStrategyMargin());
	    }
	}

    }
}
