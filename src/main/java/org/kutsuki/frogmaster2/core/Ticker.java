package org.kutsuki.frogmaster2.core;

import java.time.LocalDateTime;

public class Ticker {
    private static final String ES = "ES";

    private char month;
    private int year;

    private int equity;
    private int realized;
    private LocalDateTime equityDateTime;

    public Ticker(char month, int year) {
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

    public LocalDateTime getEquityDateTime() {
	return equityDateTime;
    }

    public void setEquityDateTime(LocalDateTime equityDateTime) {
	this.equityDateTime = equityDateTime;
    }
}
