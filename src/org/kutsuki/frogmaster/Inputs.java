package org.kutsuki.frogmaster;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Inputs {
    private final static Map<Integer, Input> INPUT_MAP = new HashMap<Integer, Input>();
    private final static String TXT = ".txt";

    static {
	INPUT_MAP.put(5, new Input("-5", "0", "2", "13.25"));
	INPUT_MAP.put(6, new Input("-5", "0", "2", "13.25"));
	INPUT_MAP.put(7, new Input("-5.75", "0", "6", "10.75"));
	INPUT_MAP.put(8, new Input("-6", "-0.25", "6", "10.75"));
	INPUT_MAP.put(9, new Input("-6", "-0.25", "6", "10.75"));
	INPUT_MAP.put(10, new Input("-5.5", "-2", "6.75", "10.50"));
	INPUT_MAP.put(11, new Input("-5.5", "-2.25", "6.75", "9.75"));
	INPUT_MAP.put(12, new Input("-5.5", "-2", "6.25", "10.5"));
	INPUT_MAP.put(13, new Input("-2.5", "-4", "6.25", "10"));
	INPUT_MAP.put(14, new Input("-5.25", "-2.75", "6.25", "10.5"));
	INPUT_MAP.put(15, new Input("-5.25", "-2.75", "6.25", "10.5"));
	INPUT_MAP.put(16, new Input("-5.25", "-2.75", "6.25", "10.5"));
	INPUT_MAP.put(17, new Input("-5.25", "-2.75", "6.25", "10"));
    }

    private Inputs() {
    }

    public static Input getInput(String fileName) {
	int lastYear = Integer.parseInt(getTicker(fileName).substring(3)) - 1;
	return INPUT_MAP.get(lastYear);
    }

    public static String getTicker(String fileName) {
	return StringUtils.substringBefore(fileName, TXT);
    }
}
