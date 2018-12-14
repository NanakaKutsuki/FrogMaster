package org.kutsuki.frogmaster2.inputs;

import java.util.HashMap;
import java.util.Map;

public class InputLimit {
    private static final Map<Integer, Input> INPUT_MAP = new HashMap<Integer, Input>();

    static {
	INPUT_MAP.put(5, new Input(9, -275, -100, 1450, 200)); // uncrunched
	INPUT_MAP.put(6, new Input(9, -275, -100, 1450, 200));
	INPUT_MAP.put(7, new Input(11, -50, -300, 1350, 1650));
	// INPUT_MAP.put(7, new Input(9, -250, -150, 1400, 225));
	INPUT_MAP.put(8, new Input(6, -325, -50, 1500, 650));
	INPUT_MAP.put(9, new Input(8, -625, -125, 1650, 800));
	INPUT_MAP.put(10, new Input(8, -625, -75, 1650, 875));
	INPUT_MAP.put(11, new Input(5, -550, -325, 1325, 1050));
	INPUT_MAP.put(12, new Input(6, -550, -150, 1600, 925));
	// INPUT_MAP.put(12, new Input(5, -475, -75, 1300, 1025));
	INPUT_MAP.put(13, new Input(8, -625, 0, 1300, 900));
	INPUT_MAP.put(14, new Input(8, -625, 0, 1325, 875));
	INPUT_MAP.put(15, new Input(8, -625, -200, 1075, 875));
	INPUT_MAP.put(16, new Input(8, -625, -200, 1075, 875));
	INPUT_MAP.put(17, new Input(8, -625, -200, 1025, 875));
    }

    public static Input getInput(int year) {
	return INPUT_MAP.get(year - 1);
    }
}
