package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
	    int high = 0;
	    int low = 0;

	    List<Bar> barList = new ArrayList<Bar>();

	    for (Bar bar : barMap.values()) {
		if (bar.getHigh() > high) {
		    Bar bar2 = new Bar();
		    bar2.setDateTime(bar.getDateTime());
		    bar2.setHigh(high);
		    bar2.setLow(low);
		    barList.add(bar2);

		    high = bar.getHigh();
		    low = Integer.MAX_VALUE;
		}

		if (bar.getLow() < low) {
		    low = bar.getLow();
		}
	    }

	    for (Bar bar : barList) {
		System.out.println(bar);
	    }
	}
    }

    public static void main(String[] args) {
	TempParser parser = new TempParser();
	parser.run();
    }
}
