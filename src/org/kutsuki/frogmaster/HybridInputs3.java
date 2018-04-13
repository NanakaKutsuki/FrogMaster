package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class HybridInputs3 {
	private final static Input INPUT;

	static {
		INPUT = new Input("-6.25", "-1.5", "11", "10.25");
		INPUT.setMomRE(new BigDecimal("-20.5"));
		INPUT.setLongSafety(new BigDecimal("26.25"));
	}

	private HybridInputs3() {
		// private constructor
	}

	public static Input getInput() {
		return INPUT;
	}
}
