package org.kutsuki.frogmaster2.inputs;

public class Input {
    private int length;
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

    public Input(int length, int momST, int accelST, int upAmount, int downAmount) {
	this.length = length;
	this.momST = momST;
	this.accelST = accelST;
	this.upAmount = upAmount;
	this.downAmount = downAmount;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");
	sb.append(getLength()).append(',').append(' ');
	sb.append(getMomST()).append(',').append(' ');
	sb.append(getAccelST()).append(',').append(' ');
	sb.append(getUpAmount()).append(',').append(' ');
	sb.append(getDownAmount());
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

    public int getLength() {
	return length;
    }
}
