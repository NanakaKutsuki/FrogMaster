package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Equity {
	private LocalDateTime dateTime;
	private BigDecimal realized;
	private BigDecimal unrealized;

	public Equity(LocalDateTime dateTime) {
		this.dateTime = dateTime;
		this.realized = BigDecimal.ZERO;
		this.unrealized = BigDecimal.ZERO;
	}

	public Equity(LocalDateTime dateTime, Equity other) {
		this.dateTime = dateTime;
		this.realized = other.getRealized();
		this.unrealized = other.getUnrealized();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getDateTime()).append(',');
		sb.append(getUnrealized()).append(',');
		sb.append(getRealized());
		return sb.toString();
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public BigDecimal getRealized() {
		return realized;
	}

	public void setRealized(BigDecimal realized) {
		this.realized = realized;
	}

	public BigDecimal getUnrealized() {
		return unrealized;
	}

	public void setUnrealized(BigDecimal unrealized) {
		this.unrealized = unrealized;
	}
}
