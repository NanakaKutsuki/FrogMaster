package org.kutsuki.frogmaster2.inputs;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.kutsuki.frogmaster2.TradestationSearch;

public class InputResult implements Comparable<InputResult> {
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

	    sb.append("ROI ");
	    bd = getROI();
	    sb.append(bd).append('x').append(' ');
	}

	if (input != null) {
	    sb.append(getInput());
	}

	return sb.toString();
    }

    @Override
    public int compareTo(InputResult rhs) {
	int result = 0;

	if (TradestationSearch.AT_ES) {
	    result = Integer.compare(rhs.getTotal(), getTotal());
	} else {
	    result = rhs.getROI().compareTo(getROI());
	}

	return result;
    }

    public Input getInput() {
	return input;
    }

    public int getTotal() {
	return realized + unrealized;
    }

    public BigDecimal getROI() {
	BigDecimal cost = BigDecimal.valueOf(-lowestEquity + 580000);
	return BigDecimal.valueOf(getTotal()).divide(cost, 4, RoundingMode.HALF_UP);
    }
}
