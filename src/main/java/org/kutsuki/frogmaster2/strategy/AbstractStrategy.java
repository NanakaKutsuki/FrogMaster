package org.kutsuki.frogmaster2.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;

public abstract class AbstractStrategy {
    private static final boolean PRINT_TRADES = false;
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);
    private static final LocalTime FIVE_PM = LocalTime.of(17, 0);

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
    private List<Bar> barList;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime lowestEquityDateTime;
    private Ticker ticker;

    public abstract void setup(Symbol symbol, List<LocalDateTime> keyList, List<Bar> barList, AbstractInput input);

    protected abstract void strategy(Bar bar);

    protected void setTickerBarMap(Symbol symbol, List<LocalDateTime> keyList, List<Bar> barList) {
	this.bankroll = 0;
	this.bankrollEquity = 0;
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
	this.ticker = symbol.getTicker();
	this.unrealized = 0;

	this.barList = barList;

	if (!barList.isEmpty()) {
	    this.endDateTime = calcEndDateTime(keyList, symbol);
	    this.startDateTime = calcStartDateTime(keyList, symbol);
	    this.lowestEquityDateTime = keyList.get(0);
	}
    }

    public void run() {
	for (Bar bar : barList) {
	    LocalDateTime key = bar.getDateTime();
	    if (!key.isBefore(getStartDateTime()) && !key.isAfter(getEndDateTime())) {
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
	    }

	    index++;
	}
    }

    public LocalDateTime calcEndDateTime(List<LocalDateTime> keyList, char month, int fullYear) {
	LocalDate date = null;

	switch (month) {
	case 'A':
	    // hard coded for @ES
	    date = barList.get(barList.size() - 1).getDateTime().toLocalDate();
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
	while (!keyList.contains(dateTime)) {
	    dateTime = dateTime.minusDays(1);
	}

	return dateTime;
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
	    bar = barList.get(index - length);
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

    protected boolean skip(LocalTime time) {
	int hour = time.getHour();
	int min = time.getMinute();

	return (hour == 16 && min == 10) || (hour == 16 && min == 15) || (hour == 16 && min == 20)
		|| (hour == 16 && min == 25) || (hour == 16 && min == 30) || (hour == 16 && min == 55) || hour == 17
		|| (hour == 18 && min == 0);
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
	this.bankroll -= ticker.getCommission() + ticker.getMinimumTick() + ticker.getCommission()
		+ ticker.getMinimumTick();

	this.bankrollEquity += convertTicks(realized);
	this.bankrollEquity -= ticker.getCommission() + ticker.getMinimumTick() + ticker.getCommission()
		+ ticker.getMinimumTick();

	if (bankrollEquity > 0) {
	    this.bankrollEquity = 0;
	}
    }

    protected int averageFC(int length) {
	int result = 0;

	if (index > length) {
	    int sum = 0;

	    for (int i = 0; i < length; i++) {
		sum += getPrevBar(i).getClose();
	    }

	    result = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(length), 0, RoundingMode.HALF_UP).intValue();
	}

	return result;
    }

    protected int priceOscillator(int fastLength, int slowLength) {
	BigDecimal fastAvg = BigDecimal.ZERO;
	BigDecimal slowAvg = BigDecimal.ZERO;

	if (index > fastLength) {
	    BigDecimal sum = BigDecimal.ZERO;

	    for (int i = 0; i < fastLength; i++) {
		sum = sum.add(getPrevBar(i).getMedian());
	    }

	    fastAvg = sum.divide(BigDecimal.valueOf(fastLength), 2, RoundingMode.HALF_UP);
	}

	if (index > slowLength) {
	    BigDecimal sum = BigDecimal.ZERO;

	    for (int i = 0; i < slowLength; i++) {
		sum = sum.add(getPrevBar(i).getMedian());
	    }

	    slowAvg = sum.divide(BigDecimal.valueOf(slowLength), 2, RoundingMode.HALF_UP);
	}

	// if (getPrevBar(0).getDateTime().isEqual(LocalDateTime.of(2006, 2, 27, 14,
	// 15))) {
	// System.out.println(fastAvg + " " + slowAvg);
	// }

	return fastAvg.subtract(slowAvg).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private LocalDateTime calcStartDateTime(List<LocalDateTime> keyList, Symbol symbol) {
	LocalDate date = null;

	switch (symbol.getMonth()) {
	case 'A':
	    // hard coded for @ES
	    date = LocalDate.of(2005, 12, 1);
	    break;
	case 'H':
	    date = LocalDate.of(symbol.getFullYear() - 1, 12, 1);
	    break;
	case 'M':
	    date = LocalDate.of(symbol.getFullYear(), 3, 1);
	    break;
	case 'U':
	    date = LocalDate.of(symbol.getFullYear(), 6, 1);
	    break;
	case 'Z':
	    date = LocalDate.of(symbol.getFullYear(), 9, 1);
	    break;
	default:
	    throw new IllegalStateException("Bad Ticker!" + symbol);
	}

	LocalDateTime dateTime = LocalDateTime.of(calcThirdDayOfWeek(date), NINE_TWENTYFIVE);
	while (!keyList.contains(dateTime)) {
	    dateTime = dateTime.plusDays(1);
	}

	return dateTime;
    }

    private LocalDateTime calcEndDateTime(List<LocalDateTime> keyList, Symbol symbol) {
	return calcEndDateTime(keyList, symbol.getMonth(), symbol.getFullYear());
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
	    unrealized -= ticker.getCommission() + ticker.getMinimumTick();
	} else if (getMarketPosition() == -1) {
	    unrealized = convertTicks(positionPrice - bar.getClose());
	    unrealized -= ticker.getCommission() + ticker.getMinimumTick();
	} else {
	    unrealized = 0;
	}
    }

    private int convertTicks(int ticks) {
	return ticks * ticker.getDollarValue();
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
