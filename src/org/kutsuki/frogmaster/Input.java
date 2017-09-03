package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class Input {
	private BigDecimal momST;
	private BigDecimal accelST;
	private BigDecimal upAmount;
	private BigDecimal downAmount;

	public Input(String momST, String accelST, String upAmount, String downAmount) {
		this.momST = new BigDecimal(momST);
		this.accelST = new BigDecimal(accelST);
		this.upAmount = new BigDecimal(upAmount);
		this.downAmount = new BigDecimal(downAmount);
	}

	public BigDecimal getMomST() {
		return momST;
	}

	public BigDecimal getAccelST() {
		return accelST;
	}

	public BigDecimal getUpAmount() {
		return upAmount;
	}

	public BigDecimal getDownAmount() {
		return downAmount;
	}
}
