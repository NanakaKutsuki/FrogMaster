package org.kutsuki.frogmaster2.core;

import java.time.LocalDateTime;

public class Ticker implements Comparable<Ticker> {
    private String ticker;
    private char month;
    private int year;

    private int bankrollRE;
    private int equity;
    private int realized;
    private int unrealized;
    private LocalDateTime equityDateTime;

    public Ticker(char month, int year) {
	this.month = month;
	this.year = year;

	StringBuilder sb = new StringBuilder();
	sb.append("ES");
	sb.append(month);

	if (year < 10) {
	    sb.append(0);
	}
	sb.append(year);

	this.ticker = sb.toString();
    }

    @Override
    public int compareTo(Ticker rhs) {
	int result = Integer.compare(getFullYear(), rhs.getFullYear());

	if (result == 0) {
	    result = Character.compare(getMonth(), rhs.getMonth());
	}

	return result;
    }

    @Override
    public String toString() {
	return ticker;
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

    public int getBankrollRE() {
	return bankrollRE;
    }

    public void setBankrollRE(int bankrollRE) {
	this.bankrollRE = bankrollRE;
    }

    public int getEquity() {
	return equity;
    }

    public void setEquity(int equity) {
	this.equity = equity;
    }

    public int getRealized() {
	return realized;
    }

    public void setRealized(int realized) {
	this.realized = realized;
    }

    public int getUnrealized() {
	return unrealized;
    }

    public void setUnrealized(int unrealized) {
	this.unrealized = unrealized;
    }

    public LocalDateTime getEquityDateTime() {
	return equityDateTime;
    }

    public void setEquityDateTime(LocalDateTime equityDateTime) {
	this.equityDateTime = equityDateTime;
    }
}
