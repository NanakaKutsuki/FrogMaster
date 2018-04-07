package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class Input {
    private BigDecimal momST;
    private BigDecimal accelST;
    private BigDecimal upAmount;
    private BigDecimal downAmount;
    private BigDecimal momRE;
    private BigDecimal upAmountRE;
    private BigDecimal downAmountRE;
    private BigDecimal longSafety;
    private BigDecimal upAmountT;
    private BigDecimal downAmountT;
    private BigDecimal longSafetyAH;
    private BigDecimal upAmountT2;
    private BigDecimal downAmountT2;
    private BigDecimal momREAH;
    private BigDecimal accelREAH;
    private BigDecimal upAmountREAH;
    private BigDecimal downAmountREAH;
    private BigDecimal momCore;
    private BigDecimal upAmountCore;
    private BigDecimal downAmountCore;
    private BigDecimal accelAH;
    private BigDecimal upAmountAH;
    private BigDecimal downAmountAH;
    private BigDecimal momBeat;
    private BigDecimal accelBeat;
    private BigDecimal longBeating;
    private BigDecimal upAmountB;
    private BigDecimal downAmountB;
    private BigDecimal longBeatingAH;
    private BigDecimal upAmountB2;
    private BigDecimal downAmountB2;

    public Input(String momST, String accelST, String upAmount, String downAmount) {
	this.momST = new BigDecimal(momST);
	this.accelST = new BigDecimal(accelST);
	this.upAmount = new BigDecimal(upAmount);
	this.downAmount = new BigDecimal(downAmount);
    }

    public BigDecimal getMomST() {
	return momST;
    }

    public void setMomST(BigDecimal momST) {
	this.momST = momST;
    }

    public BigDecimal getAccelST() {
	return accelST;
    }

    public void setAccelST(BigDecimal accelST) {
	this.accelST = accelST;
    }

    public BigDecimal getUpAmount() {
	return upAmount;
    }

    public void setUpAmount(BigDecimal upAmount) {
	this.upAmount = upAmount;
    }

    public BigDecimal getDownAmount() {
	return downAmount;
    }

    public void setDownAmount(BigDecimal downAmount) {
	this.downAmount = downAmount;
    }

    public BigDecimal getMomRE() {
	return momRE;
    }

    public void setMomRE(BigDecimal momRE) {
	this.momRE = momRE;
    }

    public BigDecimal getUpAmountRE() {
	return upAmountRE;
    }

    public void setUpAmountRE(BigDecimal upAmountRE) {
	this.upAmountRE = upAmountRE;
    }

    public BigDecimal getDownAmountRE() {
	return downAmountRE;
    }

    public void setDownAmountRE(BigDecimal downAmountRE) {
	this.downAmountRE = downAmountRE;
    }

    public BigDecimal getLongSafety() {
	return longSafety;
    }

    public void setLongSafety(BigDecimal longSafety) {
	this.longSafety = longSafety;
    }

    public BigDecimal getUpAmountT() {
	return upAmountT;
    }

    public void setUpAmountT(BigDecimal upAmountT) {
	this.upAmountT = upAmountT;
    }

    public BigDecimal getDownAmountT() {
	return downAmountT;
    }

    public void setDownAmountT(BigDecimal downAmountT) {
	this.downAmountT = downAmountT;
    }

    public BigDecimal getLongSafetyAH() {
	return longSafetyAH;
    }

    public void setLongSafetyAH(BigDecimal longSafetyAH) {
	this.longSafetyAH = longSafetyAH;
    }

    public BigDecimal getUpAmountT2() {
	return upAmountT2;
    }

    public void setUpAmountT2(BigDecimal upAmountT2) {
	this.upAmountT2 = upAmountT2;
    }

    public BigDecimal getDownAmountT2() {
	return downAmountT2;
    }

    public void setDownAmountT2(BigDecimal downAmountT2) {
	this.downAmountT2 = downAmountT2;
    }

    public BigDecimal getLongBeating() {
	return longBeating;
    }

    public void setLongBeating(BigDecimal longBeating) {
	this.longBeating = longBeating;
    }

    public BigDecimal getUpAmountB() {
	return upAmountB;
    }

    public void setUpAmountB(BigDecimal upAmountB) {
	this.upAmountB = upAmountB;
    }

    public BigDecimal getDownAmountB() {
	return downAmountB;
    }

    public void setDownAmountB(BigDecimal downAmountB) {
	this.downAmountB = downAmountB;
    }

    public BigDecimal getLongBeatingAH() {
	return longBeatingAH;
    }

    public void setLongBeatingAH(BigDecimal longBeatingAH) {
	this.longBeatingAH = longBeatingAH;
    }

    public BigDecimal getUpAmountB2() {
	return upAmountB2;
    }

    public void setUpAmountB2(BigDecimal upAmountB2) {
	this.upAmountB2 = upAmountB2;
    }

    public BigDecimal getDownAmountB2() {
	return downAmountB2;
    }

    public void setDownAmountB2(BigDecimal downAmountB2) {
	this.downAmountB2 = downAmountB2;
    }

    public BigDecimal getMomREAH() {
	return momREAH;
    }

    public void setMomREAH(BigDecimal momREAH) {
	this.momREAH = momREAH;
    }

    public BigDecimal getAccelREAH() {
	return accelREAH;
    }

    public void setAccelREAH(BigDecimal accelREAH) {
	this.accelREAH = accelREAH;
    }

    public BigDecimal getUpAmountREAH() {
	return upAmountREAH;
    }

    public void setUpAmountREAH(BigDecimal upAmountREAH) {
	this.upAmountREAH = upAmountREAH;
    }

    public BigDecimal getDownAmountREAH() {
	return downAmountREAH;
    }

    public void setDownAmountREAH(BigDecimal downAmountREAH) {
	this.downAmountREAH = downAmountREAH;
    }

    public BigDecimal getMomCore() {
	return momCore;
    }

    public void setMomCore(BigDecimal momCore) {
	this.momCore = momCore;
    }

    public BigDecimal getUpAmountCore() {
	return upAmountCore;
    }

    public void setUpAmountCore(BigDecimal upAmountCore) {
	this.upAmountCore = upAmountCore;
    }

    public BigDecimal getDownAmountCore() {
	return downAmountCore;
    }

    public void setDownAmountCore(BigDecimal downAmountCore) {
	this.downAmountCore = downAmountCore;
    }

    public BigDecimal getAccelAH() {
	return accelAH;
    }

    public void setAccelAH(BigDecimal accelAH) {
	this.accelAH = accelAH;
    }

    public BigDecimal getUpAmountAH() {
	return upAmountAH;
    }

    public void setUpAmountAH(BigDecimal upAmountAH) {
	this.upAmountAH = upAmountAH;
    }

    public BigDecimal getDownAmountAH() {
	return downAmountAH;
    }

    public void setDownAmountAH(BigDecimal downAmountAH) {
	this.downAmountAH = downAmountAH;
    }

    public BigDecimal getMomBeat() {
	return momBeat;
    }

    public void setMomBeat(BigDecimal momBeat) {
	this.momBeat = momBeat;
    }

    public BigDecimal getAccelBeat() {
	return accelBeat;
    }

    public void setAccelBeat(BigDecimal accelBeat) {
	this.accelBeat = accelBeat;
    }
}
