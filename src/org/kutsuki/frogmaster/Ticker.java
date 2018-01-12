package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ticker {
    private static final BigDecimal STARTING_BANKROLL = new BigDecimal("100000");
    private static final String ES = "ES";

    private char month;
    private int year;

    private BigDecimal bankroll;
    private BigDecimal bankrollBar;
    private BigDecimal equity;
    private BigDecimal numContracts;
    private BigDecimal numContractsBar;
    private BigDecimal realized;
    private LocalDateTime equityDateTime;

    public Ticker(char month, int year) {
	this.bankroll = STARTING_BANKROLL;
	this.bankrollBar = STARTING_BANKROLL;
	this.month = month;
	this.year = year;
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

    public BigDecimal getNumContracts() {
	return numContracts;
    }

    public void setNumContracts(BigDecimal numContracts) {
	this.numContracts = numContracts;
    }

    public BigDecimal getNumContractsBar() {
	return numContractsBar;
    }

    public void setNumContractsBar(BigDecimal numContractsBar) {
	this.numContractsBar = numContractsBar;
    }

    public BigDecimal getBankroll() {
	return bankroll;
    }

    public void setBankroll(BigDecimal bankroll) {
	this.bankroll = bankroll;
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
