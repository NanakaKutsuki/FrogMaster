package org.kutsuki.frogmaster;

import org.apache.commons.lang3.StringUtils;

public class Ticker {
    private static final String ES = "ES";

    private char month;
    private int year;

    public Ticker(char month, int year) {
	this.month = month;
	this.year = year;
    }

    public Ticker(String filename) {
	if (!filename.startsWith(ES)) {
	    throw new IllegalArgumentException("Not an ES file! " + filename);
	}

	this.month = filename.charAt(2);
	this.year = Integer.parseInt(StringUtils.substring(filename, 3, 5));
    }

    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(getSymbol());
	sb.append(getMonth());

	if (year < 10) {
	    sb.append(0);
	}
	sb.append(year);

	return sb.toString();
    }

    public String getSymbol() {
	return ES;
    }

    public char getMonth() {
	return month;
    }

    public int getYear() {
	return year;
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
}
