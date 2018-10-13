package org.kutsuki.frogmaster2.inputs;

public class Input {
    private int length;
    private int momST;
    private int accelST;
    private int upAmount;
    private int downAmount;

    private int lengthAH;
    private int momAH;
    private int accelAH;
    private int upAmountAH;
    private int downAmountAH;

    public Input(int length, int momST, int accelST, int upAmount, int downAmount) {
	this.length = length;
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;
    }

    public Input(int length, int momST, int accelST, int upAmount, int downAmount, int lengthAH, int momAH, int accelAH,
	    int upAmountAH, int downAmountAH) {
	this.length = length;
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;

	this.lengthAH = lengthAH;
	this.momAH = momAH;
	this.accelAH = accelAH;
	this.upAmountAH = upAmountAH;
	this.downAmountAH = downAmountAH;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");

	if (getDownAmountAH() != 0) {
	    sb.append(getLength()).append(',').append(' ');
	    sb.append(getMomST()).append(',').append(' ');
	    sb.append(getAccelST()).append(',').append(' ');
	    sb.append(getUpAmount()).append(',').append(' ');
	    sb.append(getDownAmount()).append(',').append(' ');
	    sb.append(getLengthAH()).append(',').append(' ');
	    sb.append(getMomAH()).append(',').append(' ');
	    sb.append(getAccelAH()).append(',').append(' ');
	    sb.append(getUpAmountAH()).append(',').append(' ');
	    sb.append(getDownAmountAH());
	} else {
	    sb.append(getLength()).append(',').append(' ');
	    sb.append(getMomST()).append(',').append(' ');
	    sb.append(getAccelST()).append(',').append(' ');
	    sb.append(getUpAmount()).append(',').append(' ');
	    sb.append(getDownAmount());
	}

	sb.append(')');
	return sb.toString();
    }

    public int getLength() {
	return length;
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
