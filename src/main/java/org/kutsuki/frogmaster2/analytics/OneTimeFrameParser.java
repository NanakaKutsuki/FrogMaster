package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;

public class OneTimeFrameParser extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	// TODO convert from 30 min to 5 min bars

	if (file.exists()) {
	    int day = 0;
	    int lastHigh = 0;
	    int lastLow = 0;
	    int otf = 0;
	    int open = 0;
	    LocalTime start = LocalTime.of(9, 30);
	    LocalTime end = LocalTime.of(16, 00);
	    List<Integer> otfList = new ArrayList<Integer>();

	    int upDays = 0;
	    int downDays = 0;
	    boolean once = false;
	    boolean onceDown = false;

	    for (LocalDateTime key : barMap.keySet()) {
		LocalTime time = key.toLocalTime();

		if (time.equals(start)) {
		    Bar bar = barMap.get(key);
		    day = key.getDayOfYear();
		    lastHigh = bar.getHigh();
		    lastLow = bar.getLow();
		    open = bar.getOpen();
		    otf = 0;
		    otfList.clear();
		    once = false;
		    onceDown = false;
		}

		if (time.equals(end) && day == key.getDayOfYear()) {
		    Bar bar = barMap.get(key);

		    for (int i : otfList) {
			if (i == 1) {
			    if (bar.getClose() > open && !once) {
				upDays++;
				once = true;
			    } else if (bar.getClose() < open && !onceDown) {
				downDays++;
				onceDown = true;
			    }
			}
		    }
		}

		if (time.isAfter(start) && time.isBefore(end)) {
		    Bar bar = barMap.get(key);

		    if (bar.getHigh() < lastHigh && bar.getLow() < lastLow) {
			otf++;
		    } else {
			if (otf != 0) {
			    otfList.add(otf);
			}

			otf = 0;
		    }

		    lastHigh = bar.getHigh();
		    lastLow = bar.getLow();
		}
	    }

	    System.out.println(upDays);
	    System.out.println(downDays);
	}
    }

    public static void main(String[] args) {
	OneTimeFrameParser parser = new OneTimeFrameParser();
	parser.run();
    }
}
