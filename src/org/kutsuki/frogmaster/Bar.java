package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bar {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private LocalDateTime dateTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private int up;
    private int down;
    private String target;

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getDateTime().format(FORMAT)).append(',');
	sb.append(getOpen()).append(',');
	sb.append(getHigh()).append(',');
	sb.append(getLow()).append(',');
	sb.append(getClose()).append(',');
	sb.append(getUp()).append(',');
	sb.append(getDown()).append(',');
	sb.append(getTarget());

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

    public int getUp() {
	return up;
    }

    public void setUp(int up) {
	this.up = up;
    }

    public int getDown() {
	return down;
    }

    public void setDown(int down) {
	this.down = down;
    }

    public String getTarget() {
	return target;
    }

    public void setTarget(String target) {
	this.target = target;
    }
}
