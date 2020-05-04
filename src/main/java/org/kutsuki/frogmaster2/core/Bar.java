package org.kutsuki.frogmaster2.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Bar implements Comparable<Bar> {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private LocalDateTime dateTime;
    private int open;
    private int high;
    private int low;
    private int close;
    private int upTicks;
    private int downTicks;
    private BigDecimal median;

    public Bar(LocalDateTime dateTime, int open, int high, int low, int close) {
	this.dateTime = dateTime;
	this.open = open;
	this.high = high;
	this.low = low;
	this.close = close;
	this.median = BigDecimal.valueOf(high).add(BigDecimal.valueOf(low)).divide(TWO, 2, RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(Bar other) {
	return getDateTime().compareTo(other.getDateTime());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getDateTime()).append(',');
	sb.append(getOpen()).append(',');
	sb.append(getHigh()).append(',');
	sb.append(getLow()).append(',');
	sb.append(getClose());
	// sb.append(getClose()).append(',');
	// sb.append(getUpTicks()).append(',');
	// sb.append(getDownTicks());

	return sb.toString();
    }

    public LocalTime getTime() {
	return getDateTime().toLocalTime();
    }

    public LocalDateTime getDateTime() {
	return dateTime;
    }

    public int getOpen() {
	return open;
    }

    public int getHigh() {
	return high;
    }

    public int getLow() {
	return low;
    }

    public int getClose() {
	return close;
    }

    public int getUpTicks() {
	return upTicks;
    }

    public int getDownTicks() {
	return downTicks;
    }

    public void setUpTicks(int upTicks) {
	this.upTicks = upTicks;
    }

    public void setDownTicks(int downTicks) {
	this.downTicks = downTicks;
    }

    public BigDecimal getMedian() {
	return median;
    }
}
