package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

public class FrogData {
    private LocalTime start;
    private BigDecimal price;
    private int count;
    private long duration;

    public FrogData(BigDecimal price, LocalTime start) {
	this.price = price;
	this.start = start;
	this.count = 1;
	this.duration = 0;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(price).append(',');
	sb.append(duration).append(',');
	sb.append(count);

	return sb.toString();
    }

    public void addTime(LocalTime end) {
	long seconds = Duration.between(start, end).getSeconds();

	if (seconds == 0) {
	    seconds = 1;
	}

	duration += seconds;
	count++;
    }

    public BigDecimal getPrice() {
	return price;
    }

    public int getCount() {
	return count;
    }

    public long getDuration() {
	return duration;
    }

}
