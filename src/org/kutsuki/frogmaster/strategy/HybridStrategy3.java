package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.HybridInputs3;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategy3 extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("18000");
    private static final BigDecimal COST_PER_CONTRACT_BAR = new BigDecimal("10100");
    private static final BigDecimal FORCE_STOP = new BigDecimal("16");
    private static final LocalTime END_TIME = LocalTime.of(16, 15);
    private static final LocalTime START_TIME = LocalTime.of(20, 30);

    private boolean marketShort;
    private boolean marketBuy;
    private boolean marketSell;
    private boolean marketCover;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal lastPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal stopPrice;
    private BigDecimal longPrice;
    private BigDecimal lastMom;
    private Input input;

    public HybridStrategy3(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
	super(ticker, barMap);
	this.highPrice = BigDecimal.ZERO;
	this.input = HybridInputs3.getInput();
	this.lastMom = BigDecimal.ZERO;
	this.lastPos = null;
	this.longPos = null;
	this.lowPrice = BigDecimal.ZERO;
	this.marketCover = false;
	this.marketSell = false;
	this.marketShort = false;
	this.marketBuy = false;
	this.shortPos = null;
	this.stopPrice = BigDecimal.ZERO;
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

	if (shortPos != null) {
	    margin = margin.add(getMaintenanceMargin());
	}

	return margin;
    }

    private int count = 0;

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (marketBuy) {
	    if (getMarketPosition() == -1) {
		addBankroll(shortPos.subtract(bar.getOpen()));
		addBankrollBar(lastPos.subtract(bar.getOpen()));

		longPos = bar.getOpen();
		lastPos = bar.getOpen();
		shortPos = null;

		System.out.println(count + ". " + bar.getDateTime() + " LONG " + bar.getOpen() + " " + getBankroll());
		count++;
		System.out.println(count + ". " + bar.getDateTime() + " LONG " + bar.getOpen());
	    } else {
		longPos = bar.getOpen();
		lastPos = bar.getOpen();
		shortPos = null;

		count++;
		System.out.println(count + ". " + bar.getDateTime() + " START " + bar.getOpen());
	    }

	    marketBuy = false;
	} else if (marketShort) {
	    if (getMarketPosition() == 1) {
		addBankroll(bar.getOpen().subtract(longPos));
		addBankrollBar(bar.getOpen().subtract(lastPos));
		longPos = null;

		System.out.println(count + ". " + bar.getDateTime() + " SHORT " + bar.getOpen() + " " + getBankroll());
	    }

	    count++;
	    System.out.println(count + ". " + bar.getDateTime() + " SHORT " + bar.getOpen());

	    shortPos = bar.getOpen();
	    lastPos = bar.getOpen();
	    marketShort = false;
	} else if (marketCover) {
	    if (getMarketPosition() == -1) {
		addBankroll(shortPos.subtract(bar.getOpen()));
		addBankrollBar(lastPos.subtract(bar.getOpen()));
		shortPos = null;
		lastPos = null;

		System.out
			.println(count + ". " + bar.getDateTime() + " CoverOut " + bar.getOpen() + " " + getBankroll());
	    }

	    marketCover = false;
	} else if (marketSell) {
	    if (getMarketPosition() == 1) {
		addBankroll(bar.getOpen().subtract(longPos));
		addBankrollBar(bar.getOpen().subtract(lastPos));
		longPos = null;
		lastPos = null;

		System.out
			.println(count + ". " + bar.getDateTime() + " SellOut " + bar.getOpen() + " " + getBankroll());
	    }

	    marketSell = false;
	} else if (getMarketPosition() == 1 && bar.getLow().compareTo(stopPrice) <= 0) {
	    BigDecimal price = stopPrice;
	    if (bar.getOpen().compareTo(stopPrice) == -1) {
		price = bar.getOpen();
	    }

	    addBankroll(price.subtract(longPos));
	    addBankrollBar(price.subtract(lastPos));
	    longPos = null;
	    lastPos = null;

	    System.out.println(count + ". " + bar.getDateTime() + " SellStop " + stopPrice + " " + getBankroll());
	} else if (getMarketPosition() == -1 && bar.getHigh().compareTo(stopPrice) >= 0) {
	    BigDecimal price = stopPrice;
	    if (bar.getOpen().compareTo(stopPrice) == 1) {
		price = bar.getOpen();
	    }

	    addBankroll(shortPos.subtract(price));
	    addBankrollBar(lastPos.subtract(price));
	    shortPos = null;
	    lastPos = null;

	    System.out.println(count + ". " + bar.getDateTime() + " CoverStop " + price + " " + getBankroll());
	}
    }

    @Override
    public void strategy(Bar bar) {
	LocalTime time = bar.getDateTime().toLocalTime();

	BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());
	BigDecimal accel = mom.subtract(lastMom);
	lastMom = mom;

	if ((time.isAfter(START_TIME) && time.isBefore(LocalTime.MAX))
		|| ((time.equals(LocalTime.MIN) || time.isAfter(LocalTime.MIN)) && time.isBefore(END_TIME))) {
	    if (getMarketPosition() == 0) {
		marketBuy = true;
		longPrice = bar.getClose();
		stopPrice = bar.getClose().subtract(FORCE_STOP);
	    }

	    if (getMarketPosition() != -1) {
		if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		    stopPrice = bar.getClose().add(FORCE_STOP);
		    marketShort = true;
		} else if (bar.getClose().compareTo(longPrice.add(input.getLongSafety())) == 1) {
		    highPrice = bar.getClose().add(input.getUpAmount());
		    lowPrice = bar.getClose().subtract(input.getDownAmount());
		    stopPrice = bar.getClose().add(FORCE_STOP);
		    marketShort = true;
		}
	    } else if (getMarketPosition() == -1) {
		if (bar.getLow().compareTo(lowPrice) <= 0) {
		    if (mom.compareTo(input.getMomRE()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
		    } else {
			marketBuy = true;
			longPrice = bar.getClose();
			stopPrice = bar.getClose().subtract(FORCE_STOP);
		    }
		} else if (bar.getClose().compareTo(highPrice) >= 0) {
		    marketBuy = true;
		    longPrice = bar.getClose();
		    stopPrice = bar.getClose().subtract(FORCE_STOP);
		}
	    }
	} else {
	    if (getMarketPosition() == 1) {
		marketSell = true;
	    } else if (getMarketPosition() == -1) {
		marketCover = true;
	    }
	}
    }

    @Override
    public BigDecimal getUnrealized(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getClose().subtract(longPos);
	} else if (shortPos != null) {
	    unrealized = shortPos.subtract(bar.getClose());
	}

	return unrealized;
    }

    @Override
    public BigDecimal getUnrealizedBar(Bar bar) {
	BigDecimal unrealized = BigDecimal.ZERO;

	if (longPos != null) {
	    unrealized = bar.getClose().subtract(lastPos);
	} else if (shortPos != null) {
	    unrealized = lastPos.subtract(bar.getClose());
	}

	return unrealized;
    }

    @Override
    public void checkRebalance(Bar bar) {
	if (longPos != null) {
	    BigDecimal realized = bar.getClose().subtract(lastPos);

	    if (isRebalance(realized)) {
		addBankrollBar(realized);
		rebalance();
		lastPos = bar.getClose();
	    }
	} else if (shortPos != null) {
	    BigDecimal realized = lastPos.subtract(bar.getClose());

	    if (isRebalance(realized)) {
		addBankrollBar(realized);
		rebalance();
		lastPos = bar.getClose();
	    }
	}
    }

    @Override
    public LocalDateTime getStartDateTime() {
	return LocalDateTime.of(getStartDate().minusDays(1), START_TIME.plusMinutes(5));
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), START_TIME.plusMinutes(10));
    }

    private int getMarketPosition() {
	int position = 0;

	if (longPos != null) {
	    position = 1;
	} else if (shortPos != null) {
	    position = -1;
	}

	return position;
    }
}
