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
import org.kutsuki.frogmaster.Ticker;

public abstract class AbstractStrategy {
    private static final BigDecimal COMMISSION = new BigDecimal("5.38");
    private static final BigDecimal FIFTY = new BigDecimal("50");
    private static final BigDecimal MAINTENANCE_MARGIN = new BigDecimal("4500");
    private static final BigDecimal SLIPPAGE = new BigDecimal("0.50");
    private static final LocalTime EIGHT_AM = LocalTime.of(8, 0);

    private BigDecimal bankroll;
    private BigDecimal bankrollBar;
    private BigDecimal numContractsBar;
    private BigDecimal lowestEquity;
    private int count;
    private int index;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime lowestEquityDateTime;
    private List<LocalDateTime> keyList;
    private TreeMap<LocalDateTime, Bar> barMap;

    public abstract BigDecimal getCostPerContract();

    public abstract BigDecimal getStrategyMargin();

    public abstract BigDecimal getUnrealized(Bar bar);

    public abstract LocalDateTime getStartDateTime();

    public abstract LocalDateTime getEndDateTime();

    public abstract void rebalance(Bar bar);

    public abstract void resolveMarketOrders(Bar bar);

    public abstract void strategy(Bar bar);

    public AbstractStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	this.bankroll = BigDecimal.ZERO;
	this.bankrollBar = bankrollBar;
	this.barMap = barMap;
	this.count = 1;
	this.endDate = calcEndDate(ticker);
	this.index = 0;
	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	this.lowestEquity = BigDecimal.valueOf(100000);
	this.lowestEquityDateTime = barMap.firstKey();
	this.numContractsBar = BigDecimal.ONE;
	this.startDate = calcStartDate(ticker);
	Collections.sort(this.keyList);

	// calculate number onf contracts
	if (getEndDate().getYear() > 2008) {
	    this.numContractsBar = bankrollBar.divide(getCostPerContract(), 0, RoundingMode.FLOOR);
	    if (numContractsBar.compareTo(BigDecimal.ONE) == -1) {
		numContractsBar = BigDecimal.ONE;
	    }
	}
    }

    public void run() {
	for (LocalDateTime key : keyList) {
	    Bar bar = barMap.get(key);

	    if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
		// resolve market orders first
		resolveMarketOrders(bar);

		// run strategy
		strategy(bar);

		// calculate unrealized
		BigDecimal unrealized = getUnrealized(bar);

		// calculate equity
		BigDecimal equity = getBankroll().add(unrealized);
		if (equity.compareTo(getLowestEquity()) == -1) {
		    this.lowestEquity = equity;
		    this.lowestEquityDateTime = key;
		}

		// rebalance
		if (getEndDate().getYear() > 2008) {
		    rebalance(bar);
		}

		// margin check
		maintenanceMarginCheck(key, unrealized);
	    }

	    index++;
	}
    }

    public void addBankroll(BigDecimal realized) {
	this.bankroll = getBankroll().add(convertTicks(realized));
	this.bankroll = getBankroll().subtract(COMMISSION.add(SLIPPAGE));

	if (getEndDate().getYear() > 2008) {
	    this.bankrollBar = getBankrollBar().add(convertTicks(realized).multiply(numContractsBar));
	    this.bankrollBar = getBankrollBar().subtract(COMMISSION.add(SLIPPAGE).multiply(numContractsBar));
	}
    }

    public void addAndRebalance(BigDecimal realized) {
	// add running bankroll
	this.bankrollBar = getBankrollBar().add(convertTicks(realized).multiply(numContractsBar));

	// pay commission for sale
	this.bankrollBar = getBankrollBar().subtract(COMMISSION.add(SLIPPAGE).multiply(numContractsBar));

	// recalculate number of contracts
	numContractsBar = getBankrollBar().divide(getCostPerContract(), 0, RoundingMode.FLOOR);
	if (numContractsBar.compareTo(BigDecimal.ONE) == -1) {
	    numContractsBar = BigDecimal.ONE;
	}

	// pay commission for rebuy
	this.bankrollBar = getBankrollBar().subtract(COMMISSION.add(SLIPPAGE).multiply(numContractsBar));
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
	BigDecimal projected = getBankrollBar().add(convertTicks(realized).multiply(numContractsBar));
	projected = projected.subtract(COMMISSION.add(SLIPPAGE).multiply(numContractsBar));

	// calculate new number of contracts
	BigDecimal contracts = projected.divide(getCostPerContract(), 0, RoundingMode.FLOOR);

	// if the projected contracts is not the same as the current number of
	// contracts, rebalance

	// TODO CURRENTLY ONLY REBALANCES UP
	return contracts.compareTo(numContractsBar) == 1;
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

    public String debug(Bar bar, String name, BigDecimal price) {
	StringBuilder sb = new StringBuilder();
	sb.append(count).append('.').append(' ');
	sb.append(bar.getDateTime()).append(' ');
	sb.append(name).append(' ');
	sb.append(price).append(' ');
	sb.append(getBankroll());
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

    private void maintenanceMarginCheck(LocalDateTime date, BigDecimal unrealized) {
	// Only check starting in 2009
	if (date.getYear() > 2008) {
	    BigDecimal equity = getBankroll().add(unrealized).add(getCostPerContract());
	    if (equity.compareTo(getStrategyMargin()) == -1) {
		throw new IllegalStateException("Maintenance Margin Exceeded! " + date + " " + getBankroll() + " "
			+ unrealized + " " + getStrategyMargin());
	    }

	    BigDecimal equityBar = getBankrollBar().add(unrealized).add(getCostPerContract()).multiply(numContractsBar);
	    if (equityBar.compareTo(getStrategyMargin().multiply(numContractsBar)) == -1) {
		throw new IllegalStateException("Maintenance Margin Exceeded! " + date + " " + getBankrollBar() + " "
			+ getStrategyMargin() + " " + numContractsBar);
	    }
	}

    }
}
