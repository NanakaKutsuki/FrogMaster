package org.kutsuki.frogmaster2.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.kutsuki.frogmaster2.inputs.Input;

public class InputResult {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Input input;
    private int realized;

    public InputResult(Input input, int realized) {
	this.input = input;
	this.realized = realized;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	BigDecimal bd = new BigDecimal(realized);
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
}
