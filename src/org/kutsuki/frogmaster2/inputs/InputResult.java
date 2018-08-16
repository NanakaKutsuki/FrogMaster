package org.kutsuki.frogmaster2.inputs;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InputResult {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Input input;
    private int realized;
    private int lowestEquity;

    public InputResult() {
	this.input = null;
	this.realized = Integer.MIN_VALUE;
	this.lowestEquity = 0;
    }

    public InputResult(Input input, int realized, int equity) {
	this.input = input;
	this.realized = realized;
	this.lowestEquity = equity;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	BigDecimal bd = new BigDecimal(realized);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	sb.append('$').append(bd).append(' ');

	bd = new BigDecimal(lowestEquity);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	sb.append('$').append(bd).append(' ');

	if (input != null) {
	    sb.append("Inputs: (");
	    sb.append(input.getMomST()).append(',').append(' ');
	    sb.append(input.getAccelST()).append(',').append(' ');
	    sb.append(input.getUpAmount()).append(',').append(' ');
	    sb.append(input.getDownAmount());
	    sb.append(')');
	}

	return sb.toString();
    }

    public Input getInput() {
	return input;
    }

    public int getRealized() {
	return realized;
    }

    public int getLowestEquity() {
	return lowestEquity;
    }
}
