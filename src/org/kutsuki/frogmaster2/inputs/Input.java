package org.kutsuki.frogmaster2.inputs;

public class Input {
    private int momST;
    private int accelST;
    private int upAmount;
    private int downAmount;

    public Input(int momST, int accelST, int upAmount, int downAmount) {
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;
    }

    public int getMomST() {
	return momST;
    }

    public void setMomST(int momST) {
	this.momST = momST;
    }

    public int getAccelST() {
	return accelST;
    }

    public void setAccelST(int accelST) {
	this.accelST = accelST;
    }

    public int getUpAmount() {
	return upAmount;
    }

    public void setUpAmount(int upAmount) {
	this.upAmount = upAmount;
    }

    public int getDownAmount() {
	return downAmount;
    }

    public void setDownAmount(int downAmount) {
	this.downAmount = downAmount;
    }
}
