package org.kutsuki.frogmaster2.inputs;

public class Input {
    private int momST;
    private int accelST;
    private int upAmount;
    private int downAmount;

    private int lengthAH;
    private int lengthAH2;
    private int momAH;
    private int accelAH;
    private int upAmountAH;
    private int downAmountAH;

    public Input(int momST, int accelST, int upAmount, int downAmount) {
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;
    }

    public Input(int momST, int accelST, int upAmount, int downAmount, int lengthAH, int lengthAH2, int momAH,
	    int accelAH, int upAmountAH, int downAmountAH) {
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;

	this.lengthAH = lengthAH;
	this.lengthAH2 = lengthAH2;
	this.momAH = momAH;
	this.accelAH = accelAH;
	this.upAmountAH = upAmountAH;
	this.downAmountAH = downAmountAH;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");

	sb.append(getMomST()).append(',').append(' ');
	sb.append(getAccelST()).append(',').append(' ');
	sb.append(getUpAmount()).append(',').append(' ');
	sb.append(getDownAmount()).append(',').append(' ');
	sb.append(getLengthAH()).append(',').append(' ');
	sb.append(getLengthAH2()).append(',').append(' ');
	sb.append(getMomAH()).append(',').append(' ');
	sb.append(getAccelAH()).append(',').append(' ');
	sb.append(getUpAmountAH()).append(',').append(' ');
	sb.append(getDownAmountAH());
	sb.append(')');
	return sb.toString();
    }

    public int getMomST() {
	return momST;
    }

    public int getAccelST() {
	return accelST;
    }

    public int getUpAmount() {
	return upAmount;
    }

    public int getDownAmount() {
	return downAmount;
    }

    public int getLengthAH() {
	return lengthAH;
    }

    public int getLengthAH2() {
	return lengthAH2;
    }

    public int getMomAH() {
	return momAH;
    }

    public int getAccelAH() {
	return accelAH;
    }

    public int getUpAmountAH() {
	return upAmountAH;
    }

    public int getDownAmountAH() {
	return downAmountAH;
    }
}
