package org.kutsuki.frogmaster2.inputs;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InputResult implements Comparable<InputResult> {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Input input;
    private int total;
    private int lowestEquity;

    public InputResult() {
	this.input = null;
	this.total = Integer.MIN_VALUE;
	this.lowestEquity = 0;
    }

    public InputResult(Input input, int total, int equity) {
	this.input = input;
	this.total = total;
	this.lowestEquity = equity;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append("Total $");
	BigDecimal bd = new BigDecimal(total);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	sb.append(bd).append(' ');

	sb.append("LowestEquity -$");
	bd = new BigDecimal(lowestEquity);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP).negate();
	sb.append(bd).append(' ');

	sb.append("ROI ");
	bd = getROI();
	sb.append(bd).append('x').append(' ');

	if (input != null) {
	    sb.append(input);
	}

	return sb.toString();
    }

    @Override
    public int compareTo(InputResult rhs) {
	return rhs.getROI().compareTo(getROI());
    }

    public BigDecimal getROI() {
	BigDecimal cost = BigDecimal.valueOf(-lowestEquity + 580000);
	return BigDecimal.valueOf(total).divide(cost, 4, RoundingMode.HALF_UP);
    }
}
