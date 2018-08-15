package org.kutsuki.frogmaster2.strategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.Bar;
import org.kutsuki.frogmaster2.Ticker;

public abstract class AbstractStrategy {
    private static final boolean PRINT_TRADES = false;
    private static final int COMMISSION = 538;
    private static final int FIFTY = 50;
    private static final int MAINTENANCE_MARGIN = 560000;
    private static final int SLIPPAGE = 50;
    private static final LocalTime EIGHT_AM = LocalTime.of(8, 0);

    private boolean marketBuy;
    private boolean marketSellShort;
    private int bankroll;
    private int count = 0;
    private int lowestEquity;
    private int index;
    private int marketPosition;
    private int positionPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime dateTime;
    private LocalDateTime lowestEquityDateTime;
    private List<LocalDateTime> keyList;
    private TreeMap<LocalDateTime, Bar> barMap;

    public abstract int getCostPerContract();

    public abstract LocalDateTime getStartDateTime();

    public abstract LocalDateTime getEndDateTime();

    public abstract void strategy(Bar bar);

    public AbstractStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	this.bankroll = 0;
	this.barMap = barMap;
	this.count = 0;
	this.dateTime = null;
	this.index = 0;
	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	this.lowestEquity = Integer.MAX_VALUE;
	this.marketPosition = 0;
	Collections.sort(this.keyList);

	if (!barMap.isEmpty()) {
	    this.endDate = calcEndDate(ticker);
	    this.startDate = calcStartDate(ticker);
	    this.lowestEquityDateTime = barMap.firstKey();
	}
    }

    public void run() {
	for (LocalDateTime key : keyList) {
	    Bar bar = barMap.get(key);
	    dateTime = key;

	    if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
		// resolve market orders first
		resolveMarketOrders(bar);

		// run strategy
		strategy(bar);

		// calculate unrealized
		int unrealized = getUnrealized(bar);

		// calculate equity
		int equity = getBankroll() + unrealized;
		if (equity < getLowestEquity()) {
		    lowestEquity = equity;
		    lowestEquityDateTime = key;
		}

		// margin check
		maintenanceMarginCheck(unrealized);
	    }

	    index++;
	}
    }

    public int getBankroll() {
	return bankroll;
    }

    public LocalTime getEightAM() {
	return EIGHT_AM;
    }

    public LocalDate getEndDate() {
	return endDate;
    }

    public int getLowestEquity() {
	return lowestEquity;
    }

    public LocalDateTime getLowestEquityDateTime() {
	return lowestEquityDateTime;
    }

    public int getMaintenanceMargin() {
	return MAINTENANCE_MARGIN;
    }

    public int getMarketPosition() {
	return marketPosition;
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

    public void marketBuy() {
	marketBuy = true;
    }

    public void marketSellShort() {
	marketSellShort = true;
    }

    private void addBankroll(int realized) {
	this.bankroll += convertTicks(realized);
	this.bankroll -= COMMISSION;
	this.bankroll -= SLIPPAGE;
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

    private int convertTicks(int ticks) {
	return ticks * FIFTY;
    }

    private int getStrategyMargin() {
	int margin = 0;

	if (getMarketPosition() != 0) {
	    margin = getMaintenanceMargin();
	}

	return margin;
    }

    private int getUnrealized(Bar bar) {
	int unrealized = 0;

	if (getMarketPosition() == 1) {
	    unrealized = bar.getClose() - positionPrice;
	} else if (getMarketPosition() == -1) {
	    unrealized = positionPrice - bar.getClose();
	}

	return convertTicks(unrealized);
    }

    private void maintenanceMarginCheck(int unrealized) {
	// Only check starting in 2009
	if (dateTime.getYear() > 2008) {
	    int equity = getBankroll() + unrealized + getCostPerContract();

	    if (equity < getStrategyMargin()) {
		throw new IllegalStateException("Maintenance Margin Exceeded! " + dateTime + " " + getBankroll() + " "
			+ unrealized + " " + getStrategyMargin());
	    }
	}

    }

    private void resolveMarketOrders(Bar bar) {
	if (marketBuy) {
	    if (marketPosition == -1) {
		addBankroll(positionPrice - bar.getOpen());

		if (PRINT_TRADES) {
		    System.out
			    .println(count + " " + bar.getDateTime() + " Long " + bar.getOpen() + " " + getBankroll());
		    count++;
		    System.out.println(count + " " + bar.getDateTime() + " Long " + bar.getOpen());
		}
	    } else {
		if (PRINT_TRADES) {
		    count++;
		    System.out.println(count + " " + bar.getDateTime() + " Long " + bar.getOpen());
		}
	    }

	    positionPrice = bar.getOpen();
	    marketPosition = 1;
	    marketBuy = false;
	} else if (marketSellShort) {
	    if (marketPosition == 1) {
		addBankroll(bar.getOpen() - positionPrice);

		if (PRINT_TRADES) {
		    System.out
			    .println(count + " " + bar.getDateTime() + " Short " + bar.getOpen() + " " + getBankroll());
		}
	    }

	    if (PRINT_TRADES) {
		count++;
		System.out.println(count + " " + bar.getDateTime() + " Short " + bar.getOpen());
	    }

	    positionPrice = bar.getOpen();
	    marketPosition = -1;
	    marketSellShort = false;
	}
    }
}
