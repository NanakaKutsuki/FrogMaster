package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

public abstract class AbstractStrategy {
    private static final BigDecimal COMMISSION = new BigDecimal("5.38");
    private static final BigDecimal FIFTY = new BigDecimal("50");
    private static final LocalTime EIGHT_AM = LocalTime.of(8, 0);
    private static final BigDecimal SLIPPAGE = new BigDecimal("0.50");

    private BigDecimal bankroll;
    private BigDecimal bankrollBar;
    private BigDecimal numContracts;
    private int count;
    private int index;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime nextRebalance;
    private List<LocalDateTime> keyList;
    private TreeMap<LocalDateTime, Bar> barMap;
    private TreeMap<LocalDateTime, Equity> equityMap;

    public abstract BigDecimal getUnrealized(Bar bar);

    public abstract BigDecimal getRealized(Bar bar);

    public abstract LocalDateTime getStartDateTime();

    public abstract LocalDateTime getEndDateTime();

    public abstract void rebalance();

    public abstract void strategy(Bar bar);

    public AbstractStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	this.bankroll = BigDecimal.ZERO;
	this.bankrollBar = BigDecimal.ZERO;
	this.barMap = barMap;
	this.count = 1;
	this.endDate = calcEndDate(ticker);
	this.equityMap = new TreeMap<LocalDateTime, Equity>();
	this.index = 0;
	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	this.nextRebalance = LocalDateTime.MIN;
	this.numContracts = BigDecimal.ONE;
	this.startDate = calcStartDate(ticker);
	Collections.sort(this.keyList);

	if (ticker.getYear() > 8) {
	    this.bankrollBar = bankrollBar;
	    this.numContracts = bankrollBar.divide(Ticker.COST_PER_CONTRACT, 0, RoundingMode.FLOOR);
	    if (numContracts.compareTo(BigDecimal.ONE) == -1) {
		numContracts = BigDecimal.ONE;
	    }
	}
    }

    public void run() {
	Equity prevEquity = new Equity(null);

	for (LocalDateTime key = barMap.firstKey(); key.isBefore(barMap.lastKey())
		|| key.isEqual(barMap.lastKey()); key = key.plusMinutes(5)) {
	    boolean found = false;
	    Bar bar = barMap.get(key);
	    BigDecimal realized = BigDecimal.ZERO;

	    if (bar != null) {
		if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
		    strategy(bar);

		    Equity equity = new Equity(key);
		    realized = getRealized(bar);
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

	    if (key.isAfter(nextRebalance) && realized.compareTo(BigDecimal.ZERO) == 0) {
		rebalance();
		nextRebalance = LocalDateTime.of(key.toLocalDate().plusDays(1), LocalTime.of(12, 0));
	    }
	}
    }

    public void addBankroll(BigDecimal realized) {
	bankroll = bankroll.add(convertTicks(realized));
	bankroll = bankroll.subtract(COMMISSION.add(SLIPPAGE));

	bankrollBar = bankrollBar.add(convertTicks(realized).multiply(numContracts));
	bankrollBar = bankrollBar.subtract(COMMISSION.add(SLIPPAGE).multiply(numContracts));
    }

    public void addBankrollBar(BigDecimal realized) {
	// add running bankroll
	bankrollBar = bankrollBar.add(convertTicks(realized).multiply(numContracts));

	// pay commission for sale
	bankrollBar = bankrollBar.subtract(COMMISSION.add(SLIPPAGE).multiply(numContracts));

	// recalculate number of contracts
	numContracts = bankrollBar.divide(Ticker.COST_PER_CONTRACT, 0, RoundingMode.FLOOR);
	if (numContracts.compareTo(BigDecimal.ONE) == -1) {
	    numContracts = BigDecimal.ONE;
	}

	// pay commission for rebuy
	bankrollBar = bankrollBar.subtract(COMMISSION.add(SLIPPAGE).multiply(numContracts));
    }

    public BigDecimal convertTicks(BigDecimal ticks) {
	return ticks.multiply(FIFTY);
    }

    public BigDecimal payCommission(BigDecimal realized) {
	// convertTicks first!
	return realized.subtract(COMMISSION.add(SLIPPAGE));
    }

    public boolean rebalancePrecheck(BigDecimal realized) {
	// calculate new projected bankroll for each bar
	BigDecimal projected = bankrollBar.add(convertTicks(realized).multiply(numContracts));
	projected = projected.subtract(COMMISSION.add(SLIPPAGE).multiply(numContracts));

	// calculate new number of contracts
	BigDecimal contracts = projected.divide(Ticker.COST_PER_CONTRACT, 0, RoundingMode.FLOOR);

	// if the projected contracts is not the same as the current number of
	// contracts, rebalance
	return contracts.compareTo(numContracts) != 0;
    }

    public BigDecimal getBankroll() {
	return bankroll;
    }

    public BigDecimal getBankrollBar() {
	return bankrollBar;
    }

    public LocalTime getEightAM() {
	return EIGHT_AM;
    }

    public LocalDate getEndDate() {
	return endDate;
    }

    public TreeMap<LocalDateTime, Equity> getEquityMap() {
	return equityMap;
    }

    public Bar getNextBar() {
	Bar bar = null;

	if (index + 1 < keyList.size()) {
	    bar = barMap.get(keyList.get(index + 1));
	}

	return bar;
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

    public String debug(String name, BigDecimal price) {
	StringBuilder sb = new StringBuilder();
	sb.append(count).append('.').append(' ');
	sb.append(getNextBar().getDateTime()).append(' ');
	sb.append(name).append(' ');
	sb.append(price).append(' ');
	sb.append(bankroll);
	count++;

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
