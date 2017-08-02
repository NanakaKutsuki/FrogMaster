package org.kutsuki.frogmaster.candlestick;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Candlestick implements Comparable<Candlestick> {
    private LocalDateTime key;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private LocalTime openTime;
    private LocalTime closeTime;

    public Candlestick(LocalDateTime key) {
	this.key = key;
	this.open = BigDecimal.ZERO;
	this.close = BigDecimal.ZERO;
	this.openTime = LocalTime.MAX;
	this.closeTime = LocalTime.MIN;
	this.high = BigDecimal.ZERO;
	this.low = BigDecimal.valueOf(1000000);
    }

    public Candlestick(LocalDateTime key, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
	this.key = key;
	this.open = open;
	this.close = close;
	this.high = high;
	this.low = low;
    }

    @Override
    public int compareTo(Candlestick rhs) {
	return getKey().compareTo(rhs.getKey());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getKey()).append(',');
	sb.append(getOpen()).append(',');
	sb.append(getClose()).append(',');
	sb.append(getHigh()).append(',');
	sb.append(getLow());
	return sb.toString();
    }

    public void add(LocalTime localTime, BigDecimal price) {
	if (price.compareTo(high) == 1) {
	    this.high = price;
	}

	if (price.compareTo(low) == -1) {
	    this.low = price;
	}

	if (localTime.isBefore(openTime)) {
	    this.openTime = localTime;
	    this.open = price;
	}

	if (localTime.isAfter(closeTime)) {
	    this.closeTime = localTime;
	    this.close = price;
	}
    }

    public LocalDateTime getKey() {
	return key;
    }

    public BigDecimal getOpen() {
	return open;
    }

    public BigDecimal getClose() {
	return close;
    }

    public BigDecimal getHigh() {
	return high;
    }

    public BigDecimal getLow() {
	return low;
    }
}
