package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Ticker {
    public static final BigDecimal COST_PER_CONTRACT = new BigDecimal("18500");
    private static final BigDecimal STARTING_BANKROLL = new BigDecimal("100000");
    private static final String ES = "ES";

    private char month;
    private int year;

    private BigDecimal bankrollBar;
    private BigDecimal bankrollQuarterly;
    private BigDecimal equity;
    private BigDecimal numContractsBar;
    private BigDecimal numContractsQuarterly;
    private BigDecimal realized;
    private LocalDateTime equityDateTime;

    public Ticker(char month, int year) {
	this.month = month;
	this.year = year;

	this.equity = null;
	this.numContractsQuarterly = STARTING_BANKROLL.divide(COST_PER_CONTRACT, 0, RoundingMode.FLOOR);
	this.numContractsBar = STARTING_BANKROLL.divide(COST_PER_CONTRACT, 0, RoundingMode.FLOOR);
	this.realized = null;
	this.bankrollQuarterly = STARTING_BANKROLL;
	this.bankrollBar = STARTING_BANKROLL;
	this.equityDateTime = null;
    }

    @Override
    public String toString() {
	return Ticker.getKey(getMonth(), getYear());
    }

    public static String getKey(char month, int year) {
	StringBuilder sb = new StringBuilder();

	sb.append(ES);
	sb.append(month);

	if (year < 10) {
	    sb.append(0);
	}
	sb.append(year);

	return sb.toString();
    }

    public int getFullYear() {
	int fullYear = -1;

	if (getYear() >= 97) {
	    fullYear = 1900 + getYear();
	} else {
	    fullYear = 2000 + getYear();
	}

	return fullYear;
    }

    public char getMonth() {
	return month;
    }

    public int getYear() {
	return year;
    }

    public BigDecimal getEquity() {
	return equity;
    }

    public void setEquity(BigDecimal equity) {
	this.equity = equity;
    }

    public BigDecimal getNumContractsQuarterly() {
	return numContractsQuarterly;
    }

    public void setNumContractsQuarterly(BigDecimal numContractsQuarterly) {
	this.numContractsQuarterly = numContractsQuarterly;
    }

    public BigDecimal getNumContractsBar() {
	return numContractsBar;
    }

    public void setNumContractsBar(BigDecimal numContractsBar) {
	this.numContractsBar = numContractsBar;
    }

    public BigDecimal getBankrollQuarterly() {
	return bankrollQuarterly;
    }

    public void setBankrollQuarterly(BigDecimal bankrollQuarterly) {
	this.bankrollQuarterly = bankrollQuarterly;
    }

    public BigDecimal getRealized() {
	return realized;
    }

    public void setRealized(BigDecimal realized) {
	this.realized = realized;
    }

    public BigDecimal getBankrollBar() {
	return bankrollBar;
    }

    public void setBankrollBar(BigDecimal bankrollBar) {
	this.bankrollBar = bankrollBar;
    }

    public LocalDateTime getEquityDateTime() {
	return equityDateTime;
    }

    public void setEquityDateTime(LocalDateTime equityDateTime) {
	this.equityDateTime = equityDateTime;
    }
}
