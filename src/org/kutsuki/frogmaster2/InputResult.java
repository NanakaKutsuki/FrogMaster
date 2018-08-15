package org.kutsuki.frogmaster2;

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
	sb.append(input.getMomST()).append(',');
	sb.append(input.getAccelST()).append(',');
	sb.append(input.getUpAmount()).append(',');
	sb.append(input.getDownAmount()).append(' ').append('$');

	BigDecimal bd = new BigDecimal(realized);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	sb.append(realized);

	return sb.toString();
    }

    public Input getInput() {
	return input;
    }

    public int getRealized() {
	return realized;
    }
}
