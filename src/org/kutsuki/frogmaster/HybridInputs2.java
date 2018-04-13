package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class HybridInputs2 {
	private final static Input INPUT;

	static {
		INPUT = new Input("-6.25", "-1.5", "8.25", "10");
		INPUT.setMomRE(new BigDecimal("-5"));
		INPUT.setUpAmountRE(new BigDecimal("12.75"));
		INPUT.setDownAmountRE(new BigDecimal("10.75"));
		INPUT.setMomREAH(new BigDecimal("-14.25"));
		INPUT.setAccelREAH(new BigDecimal("-5.5"));
		INPUT.setUpAmountREAH(new BigDecimal("9.5"));
		INPUT.setDownAmountREAH(new BigDecimal("14"));
		INPUT.setMomCore(new BigDecimal("14.25"));
		INPUT.setUpAmountCore(new BigDecimal("26"));
		INPUT.setDownAmountCore(new BigDecimal("15.5"));
		INPUT.setLongSafety(new BigDecimal("35"));
		INPUT.setUpAmountT(new BigDecimal("9.75"));
		INPUT.setDownAmountT(new BigDecimal("9.5"));
		INPUT.setLongSafetyAH(new BigDecimal("26.75"));
		INPUT.setUpAmountT2(new BigDecimal("8.5"));
		INPUT.setDownAmountT2(new BigDecimal("13"));
		INPUT.setAccelAH(new BigDecimal("-13"));
		INPUT.setUpAmountAH(new BigDecimal("15.75"));
		INPUT.setDownAmountAH(new BigDecimal("10"));
		INPUT.setMomBeat(new BigDecimal("19"));
		INPUT.setAccelBeat(new BigDecimal("18.75"));
		INPUT.setLongBeating(new BigDecimal("18.75"));
		INPUT.setUpAmountB(new BigDecimal("34.5"));
		INPUT.setDownAmountB(new BigDecimal("10.25"));
		INPUT.setLongBeatingAH(new BigDecimal("25.75"));
		INPUT.setUpAmountB2(new BigDecimal("13.5"));
		INPUT.setDownAmountB2(new BigDecimal("24.5"));
	}

	private HybridInputs2() {
		// private constructor
	}

	public static Input getInput() {
		return INPUT;
	}
}
