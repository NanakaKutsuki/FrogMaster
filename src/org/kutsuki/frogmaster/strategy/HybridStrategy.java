package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Inputs2;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategy extends AbstractStrategy {
    private static final LocalTime SEVEN_FIFTY_FIVE = LocalTime.of(7, 55);
    private static final LocalTime START = LocalTime.of(7, 59);
    private static final LocalTime END = LocalTime.of(15, 45);

    private boolean initialized;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private Input input;
    private LocalDateTime buyDateTime;

    public HybridStrategy(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.buyDateTime = LocalDateTime.of(getStartDate(), SEVEN_FIFTY_FIVE);
	this.initialized = false;
	this.input = Inputs2.getInputFromLastYear(ticker.getYear());
	this.lastMom = null;
	this.longPos = null;
	this.shortPos = null;
    }

    @Override
    public void strategy(Bar bar) {
	if (!initialized) {
	    if (bar.getDateTime().isEqual(buyDateTime)) {
		longPos = getNextBar().getOpen();
		initialized = true;
	    }
	} else {
	    if (isDay(bar)) {
		BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());

		if (lastMom != null) {
		    BigDecimal accel = mom.subtract(lastMom);

		    if (shortPos == null && mom.compareTo(input.getMomST()) == -1
			    && accel.compareTo(input.getAccelST()) == -1) {
			shortPos = getNextBar().getOpen();
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
		    }
		}

		lastMom = mom;
	    }
	}
    }

    private boolean isDay(Bar bar) {
	return !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)
		&& !bar.getDateTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)
		&& bar.getDateTime().toLocalTime().isAfter(START) && bar.getDateTime().toLocalTime().isBefore(END);
    }

    private boolean isStopLoss(Bar bar) {
	return bar.getClose().compareTo(highPrice) >= 0;
    }

    private boolean isLimit(Bar bar) {
	return bar.getLow().compareTo(lowPrice) <= 0;
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null && shortPos == null) {
	    unrealized = bar.getClose().subtract(longPos);
	} else if (shortPos != null && longPos == null && !(isDay(bar) && (isStopLoss(bar) || isLimit(bar)))) {
	    unrealized = shortPos.subtract(bar.getClose());
	}

	return unrealized;
    }

    private BigDecimal bankroll = BigDecimal.ZERO;

    @Override
    public BigDecimal getRealized(Bar bar) {
	BigDecimal realized = BigDecimal.ZERO;

	if (longPos != null && shortPos != null) {
	    realized = shortPos.subtract(longPos);
	    longPos = null;

	    bankroll = addBankroll(bankroll, realized);
	    System.out.println(debug(bar, "SellShort", shortPos, realized, bankroll));
	} else if (longPos == null && shortPos != null) {
	    if (isDay(bar) && isStopLoss(bar)) {
		realized = shortPos.subtract(getNextBar().getOpen());
		longPos = getNextBar().getOpen();
		shortPos = null;
	    } else if (isLimit(getNextBar())) {
		BigDecimal gain = lowPrice;
		if (lowPrice.compareTo(getNextBar().getOpen()) == 1) {
		    gain = getNextBar().getOpen();
		}

		realized = shortPos.subtract(gain);
		longPos = getNextBar().getOpen();
		shortPos = null;
	    }
	}

	return realized;
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate(), SEVEN_FIFTY_FIVE);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), LocalTime.MIDNIGHT);
    }
}
