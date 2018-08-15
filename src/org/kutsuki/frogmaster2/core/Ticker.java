package org.kutsuki.frogmaster2.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Ticker {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
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

    public String getEquity() {
	BigDecimal bd = new BigDecimal(equity);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	return bd.toString();
    }

    public void setEquity(int equity) {
	this.equity = equity;
    }

    public String getRealized() {
	BigDecimal bd = new BigDecimal(realized);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	return bd.toString();
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
