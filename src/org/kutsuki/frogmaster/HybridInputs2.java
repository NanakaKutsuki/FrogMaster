package org.kutsuki.frogmaster;

import java.util.HashMap;
import java.util.Map;

public class HybridInputs2 {
    private final static Map<Integer, Input> INPUT_MAP = new HashMap<Integer, Input>();

    static {
	INPUT_MAP.put(17, new Input("-6.25", "-1.5", "8.25", "10", "-12", "-11", "26", "37"));
    }

    private HybridInputs2() {
	// private constructor
    }

    public static Input getInputFromLastYear(int thisYear) {
	// return INPUT_MAP.get(thisYear - 1);
	return INPUT_MAP.get(17);
    }
}
