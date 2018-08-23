package org.kutsuki.frogmaster2.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;

public class HybridStrategy2 extends AbstractStrategy {
    private static final int COST_PER_CONTRACT = 1500000;
    private static final LocalTime END = LocalTime.of(15, 36);
    private static final LocalTime NINE_THIRTYFIVE = LocalTime.of(9, 35);
    private static final LocalTime NINE_THIRTY = LocalTime.of(9, 30);
    private static final LocalTime NINE_TWENTYFIVE = LocalTime.of(9, 25);
    private static final LocalTime EIGHT_NINETEEN = LocalTime.of(8, 19);
    private static final LocalTime START = LocalTime.of(9, 29);
    //
    // private boolean initialized;
    // private boolean marketShort;
    // private boolean marketBuy;
    // private BigDecimal longPrice;
    // private BigDecimal longPos;
    // private BigDecimal shortPos;
    // private BigDecimal highPrice;
    // private BigDecimal lowPrice;
    // private BigDecimal lastMom;
    // private BigDecimal lastMom2;
    // private BigDecimal lastMom3;
    // private BigDecimal lastMomAH;
    // private BigDecimal lastMomAH2;
    // private Input input;
    // private LocalDateTime buyDateTime;
    //
    // public HybridStrategy2(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap) {
    // super(ticker, barMap);
    // this.buyDateTime = LocalDateTime.of(getStartDate(), NINE_TWENTYFIVE);
    // this.initialized = false;
    // this.input = HybridInputs2.getInput();
    // this.lastMom = null;
    // this.lastMom2 = BigDecimal.ZERO;
    // this.lastMom3 = BigDecimal.ZERO;
    // this.lastMomAH = BigDecimal.ZERO;
    // this.lastMomAH2 = BigDecimal.ZERO;
    // this.longPos = null;
    // this.longPrice = BigDecimal.ZERO;
    // this.marketShort = false;
    // this.marketBuy = false;
    // this.shortPos = null;
    // }
    //

    @Override
    public void init(Ticker ticker, TreeMap<LocalDateTime, Bar> barMap, Input input) {
	setTickerBarMap(ticker, barMap, input);
    }

    @Override
    public int getCostPerContract() {
	return COST_PER_CONTRACT;
    }

    @Override
    public void strategy(Bar bar) {
	// if (!initialized) {
	// marketBuy = bar.getDateTime().isEqual(buyDateTime);
	// longPrice = bar.getClose();
	// lastMom = bar.getClose().subtract(getPrevBar(5).getClose());
	// } else {
	// if (isDay(bar.getDateTime().toLocalTime())) {
	// BigDecimal mom = bar.getClose().subtract(getPrevBar(8).getClose());
	// BigDecimal mom3 = bar.getClose().subtract(getPrevBar(6).getClose());
	//
	// if (bar.getDateTime().toLocalTime().equals(NINE_THIRTY)) {
	// lastMom3 = mom;
	// }
	//
	// BigDecimal accel = mom.subtract(lastMom);
	// BigDecimal accel3 = mom.subtract(lastMom3);
	//
	// lastMom3 = lastMom2;
	// lastMom2 = lastMom;
	// lastMom = mom;
	//
	// if (shortPos == null) {
	// if (mom.compareTo(input.getMomST()) == -1 &&
	// accel.compareTo(input.getAccelST()) == -1) {
	// highPrice = bar.getClose().add(input.getUpAmount());
	// lowPrice = bar.getClose().subtract(input.getDownAmount());
	// marketShort = true;
	// } else if (mom3.compareTo(input.getMomCore()) == 1 &&
	// accel3.compareTo(input.getAccelST()) == -1) {
	// highPrice = bar.getClose().add(input.getUpAmountCore());
	// lowPrice = bar.getClose().subtract(input.getDownAmountCore());
	// marketShort = true;
	// } else if (bar.getClose().compareTo(longPrice.add(input.getLongSafety())) ==
	// 1) {
	// highPrice = bar.getClose().add(input.getUpAmountT());
	// lowPrice = bar.getClose().subtract(input.getDownAmountT());
	// marketShort = true;
	// } else if
	// (longPrice.subtract(bar.getClose()).compareTo(input.getLongBeating()) == 1) {
	// highPrice = bar.getClose().add(input.getUpAmountB());
	// lowPrice = bar.getClose().subtract(input.getDownAmountB());
	// marketShort = true;
	// }
	// } else if (shortPos != null) {
	// if (bar.getLow().compareTo(lowPrice) <= 0) {
	// if (mom.compareTo(input.getMomRE()) == -1 &&
	// accel.compareTo(input.getAccelRE()) == -1) {
	// highPrice = bar.getClose().add(input.getUpAmountRE());
	// lowPrice = bar.getClose().subtract(input.getDownAmountRE());
	// } else {
	// marketBuy = true;
	// longPrice = bar.getClose();
	// }
	// } else if (bar.getClose().compareTo(highPrice) >= 0) {
	// marketBuy = true;
	// longPrice = bar.getClose();
	// }
	// }
	// } else {
	// BigDecimal mom = bar.getClose().subtract(getPrevBar(5).getClose());
	// BigDecimal accel = mom.subtract(lastMom2);
	// lastMom2 = lastMom;
	// lastMom = mom;
	//
	// BigDecimal mom2 = bar.getClose().subtract(getPrevBar(8).getClose());
	// BigDecimal accel2 = mom2.subtract(lastMomAH2);
	//
	// if (bar.getDateTime().toLocalTime().equals(NINE_TWENTYFIVE)) {
	// lastMomAH2 = mom2;
	// lastMomAH = mom2;
	// } else {
	// lastMomAH2 = lastMomAH;
	// lastMomAH = mom2;
	// }
	//
	// if (shortPos == null) {
	// if (accel.compareTo(input.getAccelAH()) == -1) {
	// highPrice = bar.getClose().add(input.getUpAmountAH());
	// lowPrice = bar.getClose().subtract(input.getDownAmountAH());
	// marketShort = true;
	// } else if (bar.getClose().compareTo(longPrice.add(input.getLongSafetyAH()))
	// == 1) {
	// highPrice = bar.getClose().add(input.getUpAmountT2());
	// lowPrice = bar.getClose().subtract(input.getDownAmountT2());
	// marketShort = true;
	// } else if
	// (longPrice.subtract(bar.getClose()).compareTo(input.getLongBeatingAH()) == 1)
	// {
	// highPrice = bar.getClose().add(input.getUpAmountB2());
	// lowPrice = bar.getClose().subtract(input.getDownAmountB2());
	// marketShort = true;
	// }
	// } else if (shortPos != null) {
	// if (mom2.compareTo(input.getMomBeat()) == 1) {
	// marketBuy = true;
	// longPrice = bar.getClose();
	// } else if (accel2.compareTo(input.getAccelBeat()) == 1) {
	// marketBuy = true;
	// longPrice = bar.getClose();
	// } else if (bar.getLow().compareTo(lowPrice) <= 0) {
	// if (mom.compareTo(input.getMomREAH()) == -1 ||
	// accel.compareTo(input.getAccelREAH()) == -1) {
	// highPrice = bar.getClose().add(input.getUpAmountREAH());
	// lowPrice = bar.getClose().subtract(input.getDownAmountREAH());
	// } else {
	// marketBuy = true;
	// longPrice = bar.getClose();
	// }
	// } else if (bar.getClose().compareTo(highPrice) >= 0
	// && bar.getDateTime().toLocalTime().isAfter(EIGHT_NINETEEN)
	// && bar.getDateTime().toLocalTime().isBefore(START)) {
	// marketBuy = true;
	// longPrice = bar.getClose();
	// }
	// }
	// }
	// }
    }

    private boolean isDay(LocalTime time) {
	return time.isAfter(START) && time.isBefore(END);
    }
}
