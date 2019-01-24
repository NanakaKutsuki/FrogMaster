package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;

public class TempParser extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    int green = 0;
	    int buy = 0;
	    boolean gap = false;
	    Bar prev = barMap.firstEntry().getValue();

	    for (Entry<LocalDateTime, Bar> e : barMap.entrySet()) {
		Bar bar = e.getValue();

		if (bar.getTime().equals(LocalTime.of(18, 5))) {
		    buy = 0;
		}

		if (bar.getClose() - bar.getOpen() > 1000) {
		    buy = bar.getClose();
		}

		if (bar.getTime().equals(LocalTime.of(16, 0)) && buy > 0) {
		    green += buy - bar.getClose();
		    System.out.println(green + " " + (buy - bar.getClose()));
		}
	    }
	}
    }

    public static void main(String[] args) {
	TempParser parser = new TempParser();
	parser.run();
    }
}
