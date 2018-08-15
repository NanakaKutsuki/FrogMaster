package org.kutsuki.frogmaster2.core;

import java.time.LocalDateTime;

public class Bar implements Comparable<Bar> {
    private LocalDateTime dateTime;
    private int open;
    private int high;
    private int low;
    private int close;
    private int upTicks;
    private int downTicks;

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
	sb.append(getClose()).append(',');
	sb.append(getUpTicks()).append(',');
	sb.append(getDownTicks());

	return sb.toString();
    }

    public LocalDateTime getDateTime() {
	return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
	this.dateTime = dateTime;
    }

    public int getOpen() {
	return open;
    }

    public void setOpen(int open) {
	this.open = open;
    }

    public int getHigh() {
	return high;
    }

    public void setHigh(int high) {
	this.high = high;
    }

    public int getLow() {
	return low;
    }

    public void setLow(int low) {
	this.low = low;
    }

    public int getClose() {
	return close;
    }

    public void setClose(int close) {
	this.close = close;
    }

    public int getUpTicks() {
	return upTicks;
    }

    public void setUpTicks(int upTicks) {
	this.upTicks = upTicks;
    }

    public int getDownTicks() {
	return downTicks;
    }

    public void setDownTicks(int downTicks) {
	this.downTicks = downTicks;
    }
}
