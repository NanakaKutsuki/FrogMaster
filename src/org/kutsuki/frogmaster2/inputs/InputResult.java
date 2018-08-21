package org.kutsuki.frogmaster2.inputs;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.kutsuki.frogmaster2.TradestationSearch;

public class InputResult {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Input input;
    private int realized;
    private int unrealized;
    private int lowestEquity;

    public InputResult() {
	this.input = null;
	this.realized = Integer.MIN_VALUE;
	this.unrealized = Integer.MIN_VALUE;
	this.lowestEquity = 0;
    }

    public InputResult(Input input, int realized, int unrealized, int equity) {
	this.input = input;
	this.realized = realized;
	this.unrealized = unrealized;
	this.lowestEquity = equity;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Realized $");
	BigDecimal bd = new BigDecimal(realized);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	sb.append(bd).append(' ');

	sb.append("Unrealized $");
	bd = new BigDecimal(unrealized);
	bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	sb.append(bd).append(' ');

	if (!TradestationSearch.AT_ES) {
	    sb.append("LowestEquity $");
	    bd = new BigDecimal(lowestEquity);
	    bd = bd.divide(HUNDRED, 2, RoundingMode.HALF_UP);
	    sb.append(bd).append(' ');
	}

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

    public int getTotal() {
	return realized + unrealized;
    }
}
