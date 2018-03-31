package org.kutsuki.frogmaster.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster.Bar;
import org.kutsuki.frogmaster.HybridInputs2;
import org.kutsuki.frogmaster.Input;
import org.kutsuki.frogmaster.Ticker;

public class HybridStrategy2 extends AbstractStrategy {
    private static final BigDecimal COST_PER_CONTRACT = new BigDecimal("17500");
    private static final BigDecimal COST_PER_CONTRACT_BAR = new BigDecimal("12000");
    private static final LocalTime END = LocalTime.of(15, 44);
    private static final LocalTime NINE_THIRTYFIVE = LocalTime.of(9, 35);
    private static final LocalTime NINE_THIRTY = LocalTime.of(9, 30);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);
    private static final LocalTime SEVEN_FIFTYNINE = LocalTime.of(7, 59);
    private static final LocalTime START = LocalTime.of(9, 29);

    private boolean initialized;
    private boolean marketShort;
    private boolean marketBuy;
    private BigDecimal longPrice;
    private BigDecimal longPos;
    private BigDecimal shortPos;
    private BigDecimal lastPos;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal lastMom;
    private BigDecimal lastMom2;
    private BigDecimal lastMom3;
    private BigDecimal lastMomAH;
    private BigDecimal lastMomAH2;
    private Input input;
    private LocalDateTime buyDateTime;

    public HybridStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, BigDecimal bankrollBar) {
	super(ticker, barMap, bankrollBar);
	this.buyDateTime = LocalDateTime.of(getStartDate(), NINE_TWENTYFIVE);
	this.initialized = false;
	this.input = HybridInputs2.getInput();
	this.lastMom = null;
	this.lastMom2 = BigDecimal.ZERO;
	this.lastMom3 = BigDecimal.ZERO;
	this.lastMomAH = BigDecimal.ZERO;
	this.lastMomAH2 = BigDecimal.ZERO;
	this.lastPos = null;
	this.longPos = null;
	this.longPrice = BigDecimal.ZERO;
	this.marketShort = false;
	this.marketBuy = false;
	this.shortPos = null;
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

    @Override
    public void resolveMarketOrders(Bar bar) {
	if (marketBuy) {
	    if (shortPos != null) {
		addBankroll(shortPos.subtract(bar.getOpen()));
		addBankrollBar(lastPos.subtract(bar.getOpen()));

		longPos = bar.getOpen();
		lastPos = bar.getOpen();
		shortPos = null;
	    } else {
		longPos = bar.getOpen();
		lastPos = bar.getOpen();
		shortPos = null;
	    }

	    initialized = true;
	    marketBuy = false;
	} else if (marketShort) {
	    if (longPos != null) {
		addBankroll(bar.getOpen().subtract(longPos));
		addBankrollBar(bar.getOpen().subtract(lastPos));
		longPos = null;
	    }

	    shortPos = bar.getOpen();
	    lastPos = bar.getOpen();
	    marketShort = false;
	}
    }

    @Override
    public void strategy(Bar bar) {
	if (!initialized) {
	    marketBuy = bar.getDateTime().isEqual(buyDateTime);
	    longPrice = bar.getClose();
	    lastMom = bar.getClose().subtract(getPrevBar(8).getClose());
	} else {
	    if (isDay(bar.getDateTime().toLocalTime())) {
		BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());
		BigDecimal mom3 = bar.getClose().subtract(getPrevBar(6).getClose());

		if (bar.getDateTime().toLocalTime().equals(NINE_THIRTY)) {
		    lastMom3 = mom;
		}

		BigDecimal accel = mom.subtract(lastMom);
		BigDecimal accel3 = mom.subtract(lastMom3);

		lastMom3 = lastMom2;
		lastMom2 = lastMom;
		lastMom = mom;

		if (shortPos == null) {
		    if (mom.compareTo(input.getMomST()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
			highPrice = bar.getClose().add(input.getUpAmount());
			lowPrice = bar.getClose().subtract(input.getDownAmount());
			marketShort = true;
		    } else if (mom3.compareTo(input.getMomCore()) == 1 && accel3.compareTo(input.getAccelST()) == -1) {
			highPrice = bar.getClose().add(input.getUpAmountCore());
			lowPrice = bar.getClose().subtract(input.getDownAmountCore());
			marketShort = true;
		    } else if (bar.getClose().compareTo(longPrice.add(input.getLongSafety())) == 1) {
			highPrice = bar.getClose().add(input.getUpAmountT());
			lowPrice = bar.getClose().subtract(input.getDownAmountT());
			marketShort = true;

			/// NEWWWW

		    } else if (longPrice.subtract(bar.getClose()).compareTo(BigDecimal.valueOf(20)) == 1) {
			highPrice = bar.getClose().add(BigDecimal.valueOf(6));
			lowPrice = bar.getClose().subtract(BigDecimal.valueOf(10));
			marketShort = true;
		    }
		} else if (shortPos != null) {
		    if (bar.getLow().compareTo(lowPrice) <= 0) {
			if (mom.compareTo(input.getMomRE()) == -1 && accel.compareTo(input.getAccelST()) == -1) {
			    highPrice = bar.getClose().add(input.getUpAmountRE());
			    lowPrice = bar.getClose().subtract(input.getDownAmountRE());
			} else {
			    marketBuy = true;
			    longPrice = bar.getClose();
			}
		    } else if (bar.getClose().compareTo(highPrice) >= 0) {
			marketBuy = true;
			longPrice = bar.getClose();
		    }
		}
	    } else {
		BigDecimal mom = bar.getClose().subtract(getPrevBar(5).getClose());
		BigDecimal accel = mom.subtract(lastMom2);
		lastMom2 = lastMom;
		lastMom = mom;

		BigDecimal mom2 = bar.getClose().subtract(getPrevBar(8).getClose());
		BigDecimal accel2 = mom2.subtract(lastMomAH2);

		if (bar.getDateTime().toLocalTime().equals(NINE_TWENTYFIVE)) {
		    lastMomAH2 = mom2;
		    lastMomAH = mom2;
		} else {
		    lastMomAH2 = lastMomAH;
		    lastMomAH = mom2;
		}

		if (shortPos == null) {
		    if (accel.compareTo(input.getAccelAH()) == -1) {
			highPrice = bar.getClose().add(input.getUpAmountAH());
			lowPrice = bar.getClose().subtract(input.getDownAmountAH());
			marketShort = true;
		    } else if (bar.getClose().compareTo(longPrice.add(input.getLongSafetyAH())) == 1) {
			highPrice = bar.getClose().add(input.getUpAmountT2());
			lowPrice = bar.getClose().subtract(input.getDownAmountT2());
			marketShort = true;
		    }
		} else if (shortPos != null) {
		    if (mom2.compareTo(input.getMomBeat()) == 1) {
			marketBuy = true;
			longPrice = bar.getClose();
		    } else if (accel2.compareTo(input.getAccelBeat()) == 1) {
			marketBuy = true;
			longPrice = bar.getClose();
		    } else if (bar.getLow().compareTo(lowPrice) <= 0) {
			if (mom.compareTo(input.getMomREAH()) == -1 || accel.compareTo(input.getAccelREAH()) == -1) {
			    highPrice = bar.getClose().add(input.getUpAmountREAH());
			    lowPrice = bar.getClose().subtract(input.getDownAmountREAH());
			} else {
			    marketBuy = true;
			    longPrice = bar.getClose();
			}
		    } else if (bar.getClose().compareTo(highPrice) >= 0
			    && bar.getDateTime().toLocalTime().isAfter(SEVEN_FIFTYNINE)
			    && bar.getDateTime().toLocalTime().isBefore(START)) {
			marketBuy = true;
			longPrice = bar.getClose();
		    }
		}
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
	return LocalDateTime.of(getStartDate(), NINE_TWENTYFIVE);
    }

    @Override
    public LocalDateTime getEndDateTime() {
	return LocalDateTime.of(getEndDate(), NINE_THIRTYFIVE);
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }
}
