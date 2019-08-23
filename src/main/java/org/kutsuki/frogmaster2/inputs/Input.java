package org.kutsuki.frogmaster2.inputs;

public class Input extends AbstractInput {
    private int length;
    private int momST;
    private int accelST;
    private int upAmount;
    private int downAmount;

    private int lengthRE;
    private int momRE;
    private int accelRE;
    private int upAmountRE;
    private int downAmountRE;

    public Input(int length, int momST, int accelST, int upAmount, int downAmount) {
	this.length = length;
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;
	this.lengthRE = 0;
    }

    public Input(int length, int momST, int accelST, int upAmount, int downAmount, int lengthRE, int momRE, int accelRE,
	    int upAmountRE, int downAmountRE) {
	this.length = length;
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;

	this.lengthRE = lengthRE;
	this.momRE = momRE;
	this.accelRE = accelRE;
	this.upAmountRE = upAmountRE;
	this.downAmountRE = downAmountRE;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");

	if (getLengthRE() != 0) {
	    sb.append(getLength()).append(',').append(' ');
	    sb.append(getMomST()).append(',').append(' ');
	    sb.append(getAccelST()).append(',').append(' ');
	    sb.append(getUpAmount()).append(',').append(' ');
	    sb.append(getDownAmount()).append(',').append(' ');
	    sb.append(getLengthRE()).append(',').append(' ');
	    sb.append(getMomRE()).append(',').append(' ');
	    sb.append(getAccelRE()).append(',').append(' ');
	    sb.append(getUpAmountRE()).append(',').append(' ');
	    sb.append(getDownAmountRE());
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

    public int getLengthRE() {
	return lengthRE;
    }

    public int getMomRE() {
	return momRE;
    }

    public int getAccelRE() {
	return accelRE;
    }

    public int getUpAmountRE() {
	return upAmountRE;
    }

    public int getDownAmountRE() {
	return downAmountRE;
    }
}
