package org.kutsuki.frogmaster;

import java.util.HashMap;
import java.util.Map;

public class HybridInputs {
    private final static Map<Integer, Input> INPUT_MAP = new HashMap<Integer, Input>();

    private final static Input INPUT;

    static {
	INPUT_MAP.put(5, new Input("-5.75", "0", "6.5", "14"));
	INPUT_MAP.put(6, new Input("-5.75", "-1", "6", "12.25"));
	INPUT_MAP.put(7, new Input("-5.75", "-1", "6", "12"));
	INPUT_MAP.put(8, new Input("-6.25", "-2", "10.75", "10"));
	INPUT_MAP.put(9, new Input("-6.25", "-1.25", "9", "10"));
	INPUT_MAP.put(10, new Input("-6.25", "-2", "8.25", "10"));
	INPUT_MAP.put(11, new Input("-6.25", "-1.5", "8.5", "10"));
	INPUT_MAP.put(12, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(13, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(14, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(15, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(16, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(17, new Input("-6.25", "-1.5", "8.25", "10"));

	INPUT = new Input("-6.55", "-0.75", "14.75", "8.75");
    }

    private HybridInputs() {
	// private constructor
    }

    public static Input getInputFromLastYear(int thisYear) {
	return INPUT;
	// return INPUT_MAP.get(thisYear - 1);
    }
}
