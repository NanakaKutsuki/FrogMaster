package org.kutsuki.frogmaster2.inputs;

import java.util.HashMap;
import java.util.Map;

public class HybridInputsOG {
    private final static Input INPUT;
    private final static Map<Integer, Input> INPUT_MAP = new HashMap<Integer, Input>();

    static {
	INPUT = new Input(-625, -150, 1025, 1050);

	// INPUT_MAP.put(18, new Input(-625, -150, 1025, 1050));
	INPUT_MAP.put(17, new Input(-625, -150, 1075, 925));
	INPUT_MAP.put(16, new Input(-625, -150, 1075, 925));
	INPUT_MAP.put(15, new Input(-625, -150, 1075, 925));
	INPUT_MAP.put(14, new Input(-625, -150, 1075, 925));
	INPUT_MAP.put(13, new Input(-625, -150, 1125, 925));
	INPUT_MAP.put(12, new Input(-625, -150, 1125, 925));
	INPUT_MAP.put(11, new Input(-625, -150, 1125, 925));
	INPUT_MAP.put(10, new Input(-625, -150, 1475, 800));
	INPUT_MAP.put(9, new Input(-625, -150, 1475, 800));
	INPUT_MAP.put(8, new Input(-625, -150, 1075, 925));
	INPUT_MAP.put(7, new Input(-625, -150, 1250, 800));
	INPUT_MAP.put(6, new Input(-625, -150, 1250, 800));
	INPUT_MAP.put(5, new Input(-625, -150, 1250, 800));
    }

    private HybridInputsOG() {
	// private constructor
    }

    public static Input getInput() {
	return INPUT;
    }

    public static Input getInputFromLastYear(int thisYear) {
	return INPUT_MAP.get(thisYear - 1);
    }
}
