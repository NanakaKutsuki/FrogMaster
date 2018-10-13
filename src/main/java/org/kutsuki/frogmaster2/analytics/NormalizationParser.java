package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;

public class NormalizationParser extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

    private int start;

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	// TODO convert from 30 min to 5 min bars
	boolean four = false;
	boolean nine = false;
	int ll = 0;
	int ss = 0;
	int lltotal = 0;
	int sstotal = 0;
	if (file.exists()) {

	    for (LocalDateTime key : barMap.keySet()) {
		Bar bar = barMap.get(key);

		if (bar.getTime().equals(LocalTime.of(16, 0))) {
		    start = bar.getClose();
		    four = true;
		    nine = false;
		} else if (bar.getTime().equals(LocalTime.of(9, 25))) {
		    if (!nine && four) {
			if (start <= bar.getClose()) {
			    ll++;
			    lltotal += bar.getClose() - start;
			} else {
			    ss++;
			    sstotal += bar.getClose() - start;
			}

			nine = true;
			four = false;
		    }
		}
	    }

	    System.out.println(ll + " " + ss + " " + lltotal + " " + sstotal);
	}
    }

    public static void main(String[] args) {
	NormalizationParser parser = new NormalizationParser();
	parser.run();
    }
}
