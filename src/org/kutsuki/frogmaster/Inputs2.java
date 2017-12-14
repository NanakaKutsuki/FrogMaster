package org.kutsuki.frogmaster;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Inputs2 {
    private final static Map<Integer, Input> INPUT_MAP = new HashMap<Integer, Input>();
    private final static String TXT = ".txt";

    static {
	INPUT_MAP.put(5, new Input("-5.75", "0", "6.5", "14.25"));
	INPUT_MAP.put(6, new Input("-5.75", "0", "6.5", "14.25"));
	INPUT_MAP.put(7, new Input("-5.75", "-1", "6", "11.75"));
	INPUT_MAP.put(8, new Input("-6.25", "-1.5", "10.75", "10"));
	INPUT_MAP.put(9, new Input("-6.25", "-1.25", "7", "9.75"));
	INPUT_MAP.put(10, new Input("-6.25", "-2", "8.25", "10"));
	INPUT_MAP.put(11, new Input("-6.25", "-1.5", "10.75", "10"));
	INPUT_MAP.put(12, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(13, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(14, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(15, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(16, new Input("-6.25", "-1.5", "8.25", "10"));
	INPUT_MAP.put(17, new Input("-6.25", "-1.5", "8.25", "10"));
    }

    private Inputs2() {
    }

    public static Input getInput(String fileName) {
	int lastYear = Integer.parseInt(getTicker(fileName).substring(3)) - 1;
	return INPUT_MAP.get(lastYear);
    }

    public static String getTicker(String fileName) {
	return StringUtils.substringBefore(fileName, TXT);
    }
}
