package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class Input {
    private BigDecimal momST;
    private BigDecimal accelST;
    private BigDecimal upAmount;
    private BigDecimal downAmount;
    private BigDecimal momRE;
    private BigDecimal accelAH;
    private BigDecimal upAmountAH;
    private BigDecimal downAmountAH;

    public Input(String momST, String accelST, String upAmount, String downAmount) {
	this.momST = new BigDecimal(momST);
	this.accelST = new BigDecimal(accelST);
	this.upAmount = new BigDecimal(upAmount);
	this.downAmount = new BigDecimal(downAmount);
    }

    public Input(String momST, String accelST, String upAmount, String downAmount, String momRE, String accelAH,
	    String upAmountAH, String downAmountAH) {
	this.momST = new BigDecimal(momST);
	this.accelST = new BigDecimal(accelST);
	this.upAmount = new BigDecimal(upAmount);
	this.downAmount = new BigDecimal(downAmount);
	this.momRE = new BigDecimal(momRE);
	this.accelAH = new BigDecimal(accelAH);
	this.upAmountAH = new BigDecimal(upAmountAH);
	this.downAmountAH = new BigDecimal(downAmountAH);
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

    public BigDecimal getMomRE() {
	return momRE;
    }

    public BigDecimal getAccelAH() {
	return accelAH;
    }

    public BigDecimal getUpAmountAH() {
	return upAmountAH;
    }

    public BigDecimal getDownAmountAH() {
	return downAmountAH;
    }
}
