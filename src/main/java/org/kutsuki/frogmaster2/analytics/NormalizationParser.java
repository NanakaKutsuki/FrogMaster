package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;

public class NormalizationParser extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

    private int year;
    private int low;
    private int high;

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	// TODO convert from 30 min to 5 min bars

	if (file.exists()) {
	    high = 0;
	    low = Integer.MAX_VALUE;
	    year = 0;

	    for (LocalDateTime key : barMap.keySet()) {
		Bar bar = barMap.get(key);

		if (bar.getLow() < low) {
		    low = bar.getLow();
		}

		if (bar.getHigh() > high) {
		    high = bar.getHigh();
		}

		if (key.getYear() != year) {
		    System.out.println(bar.getDateTime().toLocalDate() + " " + normalize(bar.getClose()) + " "
			    + Math.log(bar.getClose()) + " " + bar.getClose() + " " + high + " " + low);
		    year = key.getYear();
		}
	    }
	}
    }

    private double normalize(int price) {
	return (double) (price - low) / (double) (high - low);
    }

    public static void main(String[] args) {
	NormalizationParser parser = new NormalizationParser();
	parser.run();
    }
}
