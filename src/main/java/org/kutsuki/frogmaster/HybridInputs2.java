package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class HybridInputs2 {
    private final static Input INPUT;

    static {
	INPUT = new Input("-6.25", "-1", "8", "10.25");
	INPUT.setMomRE(new BigDecimal("-9.75"));
	INPUT.setAccelRE(new BigDecimal("-9.25"));
	INPUT.setUpAmountRE(new BigDecimal("23"));
	INPUT.setDownAmountRE(new BigDecimal("9"));
	INPUT.setMomREAH(new BigDecimal("-10"));
	INPUT.setAccelREAH(new BigDecimal("-13.75"));
	INPUT.setUpAmountREAH(new BigDecimal("36"));
	INPUT.setDownAmountREAH(new BigDecimal("26"));
	INPUT.setMomCore(new BigDecimal("14"));
	INPUT.setUpAmountCore(new BigDecimal("35"));
	INPUT.setDownAmountCore(new BigDecimal("9.25"));
	INPUT.setLongSafety(new BigDecimal("33.25"));
	INPUT.setUpAmountT(new BigDecimal("8"));
	INPUT.setDownAmountT(new BigDecimal("12"));
	INPUT.setLongSafetyAH(new BigDecimal("22"));
	INPUT.setUpAmountT2(new BigDecimal("12"));
	INPUT.setDownAmountT2(new BigDecimal("8"));
	INPUT.setAccelAH(new BigDecimal("-14.75"));
	INPUT.setUpAmountAH(new BigDecimal("1"));
	INPUT.setDownAmountAH(new BigDecimal("21.25"));
	INPUT.setMomBeat(new BigDecimal("15.75"));
	INPUT.setAccelBeat(new BigDecimal("21.5"));
	INPUT.setLongBeating(new BigDecimal("19.75"));
	INPUT.setUpAmountB(new BigDecimal("8.25"));
	INPUT.setDownAmountB(new BigDecimal("33.75"));
	INPUT.setLongBeatingAH(new BigDecimal("30"));
	INPUT.setUpAmountB2(new BigDecimal("1.25"));
	INPUT.setDownAmountB2(new BigDecimal("7.75"));
    }

    private HybridInputs2() {
	// private constructor
    }

    public static Input getInput() {
	return INPUT;
    }
}
