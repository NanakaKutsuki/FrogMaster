package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bar implements Comparable<Bar> {
    private LocalDateTime dateTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
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

    public BigDecimal getOpen() {
	return open;
    }

    public void setOpen(BigDecimal open) {
	this.open = open;
    }

    public BigDecimal getHigh() {
	return high;
    }

    public void setHigh(BigDecimal high) {
	this.high = high;
    }

    public BigDecimal getLow() {
	return low;
    }

    public void setLow(BigDecimal low) {
	this.low = low;
    }

    public BigDecimal getClose() {
	return close;
    }

    public void setClose(BigDecimal close) {
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
