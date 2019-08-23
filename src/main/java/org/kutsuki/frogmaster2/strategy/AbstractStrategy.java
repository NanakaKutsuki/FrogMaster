package org.kutsuki.frogmaster2.strategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;

public abstract class AbstractStrategy {
    private static final boolean PRINT_TRADES = false;
    private static final int COMMISSION = 269;
    private static final int FIFTY = 50;
    private static final int MAINTENANCE_MARGIN = 630000;
    private static final int SLIPPAGE = 25;
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);
    private static final LocalTime FIVE_PM = LocalTime.of(17, 0);

    private boolean marginCheck;
    private boolean marketBuy;
    private boolean marketBuyToCover;
    private boolean marketSell;
    private boolean marketSellShort;
    private int bankroll;
    private int bankrollEquity;
    private int count;
    private int index;
    private int limitCover;
    private int lowestEquity;
    private int marketPosition;
    private int positionPrice;
    private int stopBuy;
    private int stopCover;
    private int stopSell;
    private int unrealized;
    private List<LocalDateTime> keyList;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime lowestEquityDateTime;
    private TreeMap<LocalDateTime, Bar> barMap;

    public abstract void setup(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, AbstractInput input);

    protected abstract int getCostPerContract();

    protected abstract int getCostPerContractRE();

    protected abstract void strategy(Bar bar);

    public AbstractStrategy() {
	this.marginCheck = true;
    }

    protected void setTickerBarMap(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	this.bankroll = 0;
	this.bankrollEquity = 0;
	this.barMap = barMap;
	this.count = 0;
	this.index = 0;
	this.limitCover = 0;
	this.lowestEquity = Integer.MAX_VALUE;
	this.marketPosition = 0;
	this.marketBuy = false;
	this.marketBuyToCover = false;
	this.marketSell = false;
	this.marketSellShort = false;
	this.positionPrice = 0;
	this.stopBuy = 0;
	this.stopCover = 0;
	this.stopSell = 0;
	this.unrealized = 0;

	this.keyList = new ArrayList<LocalDateTime>(barMap.keySet());
	Collections.sort(this.keyList);

	if (!barMap.isEmpty()) {
	    this.endDateTime = calcEndDateTime(ticker);
	    this.startDateTime = calcStartDateTime(ticker);
	    this.lowestEquityDateTime = barMap.firstKey();
	}
    }

    public void run() {
	for (LocalDateTime key : keyList) {
	    if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
		Bar bar = barMap.get(key);

		// resolve orders first
		resolveOrders(bar);

		// run strategy
		strategy(bar);

		// calculate unrealized
		calcUnrealized(bar);

		// calculate equity
		int equity = bankrollEquity + unrealized;
		if (equity < getLowestEquity()) {
		    lowestEquity = equity;
		    lowestEquityDateTime = key;
		}

		// margin check
		maintenanceMarginCheck(key);
	    }

	    index++;
	}
    }

    public LocalDateTime calcEndDateTime(char month, int fullYear) {
	LocalDate date = null;

	switch (month) {
	case 'A':
	    // hard coded for @ES
	    date = barMap.lastKey().toLocalDate();
	    break;
	case 'H':
	    date = LocalDate.of(fullYear, 3, 1);
	    break;
	case 'M':
	    date = LocalDate.of(fullYear, 6, 1);
	    break;
	case 'U':
	    date = LocalDate.of(fullYear, 9, 1);
	    break;
	case 'Z':
	    date = LocalDate.of(fullYear, 12, 1);
	    break;
	default:
	    throw new IllegalArgumentException("Bad Month!" + month);
	}

	LocalDateTime dateTime = LocalDateTime.of(calcThirdDayOfWeek(date), FIVE_PM);
	while (!barMap.containsKey(dateTime)) {
	    dateTime = dateTime.minusDays(1);
	}

	return dateTime;
    }

    public void disableMarginCheck() {
	this.marginCheck = false;
    }

    public int getBankroll() {
	return bankroll;
    }

    public void setStartDate(LocalDate startDate) {
	this.startDateTime = LocalDateTime.of(startDate, NINE_TWENTYFIVE);
    }

    public void setEndDate(LocalDate endDate) {
	this.endDateTime = LocalDateTime.of(endDate, FIVE_PM);
    }

    public int getLowestEquity() {
	return lowestEquity;
    }

    public LocalDateTime getLowestEquityDateTime() {
	return lowestEquityDateTime;
    }

    public int getUnrealized() {
	return unrealized;
    }

    protected LocalDateTime getEndDateTime() {
	return endDateTime;
    }

    protected int getMarketPosition() {
	return marketPosition;
    }

    protected Bar getPrevBar(int length) {
	Bar bar = null;

	if (index > length) {
	    bar = barMap.get(keyList.get(index - length));
	}

	return bar;
    }

    protected LocalDateTime getStartDateTime() {
	return startDateTime;
    }

    protected void limitCover(int limit) {
	limitCover = limit;
    }

    protected void marketBuy() {
	marketBuy = true;
    }

    protected void marketBuyToCover() {
	marketBuyToCover = true;
    }

    protected void marketSell() {
	marketSell = true;
    }

    protected void marketSellShort() {
	marketSellShort = true;
    }

    protected void stopBuy(int stop) {
	stopBuy = stop;
    }

    protected void stopCover(int stop) {
	stopCover = stop;
    }

    protected void stopSell(int stop) {
	stopSell = stop;
    }

    private void addBankroll(int realized) {
	this.bankroll += convertTicks(realized);
	this.bankroll -= COMMISSION + SLIPPAGE + COMMISSION + SLIPPAGE;

	this.bankrollEquity += convertTicks(realized);
	this.bankrollEquity -= COMMISSION + SLIPPAGE + COMMISSION + SLIPPAGE;

	if (bankrollEquity > 0) {
	    this.bankrollEquity = 0;
	}
    }

    private LocalDateTime calcStartDateTime(Ticker ticker) {
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

	LocalDateTime dateTime = LocalDateTime.of(calcThirdDayOfWeek(date), NINE_TWENTYFIVE);
	while (!barMap.containsKey(dateTime)) {
	    dateTime = dateTime.plusDays(1);
	}

	return dateTime;
    }

    private LocalDateTime calcEndDateTime(Ticker ticker) {
	return calcEndDateTime(ticker.getMonth(), ticker.getFullYear());
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

    private void calcUnrealized(Bar bar) {
	if (getMarketPosition() == 1) {
	    unrealized = convertTicks(bar.getClose() - positionPrice);
	    unrealized -= COMMISSION + SLIPPAGE;
	} else if (getMarketPosition() == -1) {
	    unrealized = convertTicks(positionPrice - bar.getClose());
	    unrealized -= COMMISSION + SLIPPAGE;
	} else {
	    unrealized = 0;
	}
    }

    private int convertTicks(int ticks) {
	return ticks * FIFTY;
    }

    private int getStrategyMargin() {
	int margin = 0;

	if (getMarketPosition() != 0) {
	    margin = MAINTENANCE_MARGIN;
	}

	return margin;
    }

    private void maintenanceMarginCheck(LocalDateTime dateTime) {
	if (marginCheck) {
	    int equity = getBankroll() + unrealized + getCostPerContract();

	    if (equity < getStrategyMargin()) {
		throw new IllegalStateException("Maintenance Margin Exceeded! " + dateTime + " " + getBankroll() + " "
			+ unrealized + " " + getStrategyMargin());
	    }
	}
    }

    private void resolveOrders(Bar bar) {
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
	} else if (marketBuyToCover) {
	    if (marketPosition == -1) {
		addBankroll(positionPrice - bar.getOpen());

		if (PRINT_TRADES) {
		    System.out
			    .println(count + " " + bar.getDateTime() + " Cover " + bar.getOpen() + " " + getBankroll());
		    count++;
		    System.out.println(count + " " + bar.getDateTime() + " Cover " + bar.getOpen());
		}
	    }

	    positionPrice = 0;
	    marketPosition = 0;
	    marketBuyToCover = false;
	} else if (marketSell) {
	    if (marketPosition == 1) {
		addBankroll(bar.getOpen() - positionPrice);

		if (PRINT_TRADES) {
		    System.out
			    .println(count + " " + bar.getDateTime() + " Sell " + bar.getOpen() + " " + getBankroll());
		}
	    }

	    positionPrice = 0;
	    marketPosition = 0;
	    marketSell = false;
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

	if (marketPosition == 1 && stopSell > 0 && bar.getLow() <= stopSell) {
	    if (bar.getOpen() < stopSell) {
		stopSell = bar.getOpen();
	    }

	    addBankroll(stopSell - positionPrice);

	    if (PRINT_TRADES) {
		System.out.println(count + " " + bar.getDateTime() + " StopSell " + stopSell + " " + getBankroll());
	    }

	    positionPrice = 0;
	    marketPosition = 0;
	}

	if (marketPosition == -1 && stopCover > 0 && bar.getHigh() >= stopCover) {
	    if (bar.getOpen() > stopCover) {
		stopCover = bar.getOpen();
	    }

	    addBankroll(positionPrice - stopCover);

	    if (PRINT_TRADES) {
		System.out.println(count + " " + bar.getDateTime() + " StopCover " + stopCover + " " + getBankroll());
	    }

	    positionPrice = 0;
	    marketPosition = 0;
	}

	if (marketPosition == -1 && stopBuy > 0 && bar.getHigh() >= stopBuy) {
	    if (bar.getOpen() > stopBuy) {
		stopBuy = bar.getOpen();
	    }

	    addBankroll(positionPrice - stopBuy);

	    if (PRINT_TRADES) {
		System.out.println(count + " " + bar.getDateTime() + " StopBuy " + stopBuy + " " + getBankroll());
		count++;
		System.out.println(count + " " + bar.getDateTime() + " Long " + stopBuy);
	    }

	    positionPrice = stopBuy;
	    marketPosition = 1;
	}

	if (marketPosition == -1 && limitCover > 0 && bar.getLow() <= limitCover) {
	    if (bar.getOpen() < limitCover) {
		limitCover = bar.getOpen();
	    }

	    addBankroll(positionPrice - limitCover);

	    if (PRINT_TRADES) {
		System.out.println(count + " " + bar.getDateTime() + " LongLimit " + limitCover + " " + getBankroll());
	    }

	    positionPrice = 0;
	    marketPosition = 0;
	}

	limitCover = 0;
	stopBuy = 0;
	stopCover = 0;
	stopSell = 0;
    }
}
