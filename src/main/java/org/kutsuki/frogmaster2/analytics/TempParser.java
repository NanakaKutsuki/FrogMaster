package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;

public class TempParser extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

    public TempParser() {
	super(Ticker.ES.getDivisor());
    }

    @Override
    public File getFile(Symbol symbol) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    int bankroll = 0;

	    boolean set = false;
	    LocalTime fourpm = LocalTime.of(15, 45);
	    LocalTime sixpm = LocalTime.of(18, 5);
	    int close = 0;
	    int yesterday = 0;

	    TreeMap<Integer, String> resultMap = new TreeMap<Integer, String>();

	    for (int hour = 12; hour <= 16; hour++) {
		for (int minute = 0; minute <= 55; minute += 5) {
		    for (int hour2 = 18; hour2 <= 23; hour2++) {
			for (int minute2 = 0; minute2 <= 55; minute2 += 5) {
			    fourpm = LocalTime.of(hour, minute);
			    sixpm = LocalTime.of(hour2, minute2);

			    for (Bar bar : barMap.values()) {
				if (bar.getTime().equals(fourpm)) {
				    close = bar.getClose();
				    set = true;
				}

				if (set && bar.getTime().equals(sixpm)) {
				    bankroll += bar.getClose() - close;
				    set = false;
				}

				if (bar.getDateTime().getDayOfMonth() != yesterday) {
				    set = false;
				}

				yesterday = bar.getDateTime().getDayOfMonth();
			    }
			    // System.out.println(bankroll);
			    resultMap.put(bankroll, fourpm + " - " + sixpm);
			    bankroll = 0;
			    set = false;
			}
		    }
		}
	    }

	    for (Entry<Integer, String> e : resultMap.entrySet()) {
		System.out.println(e.toString());
	    }
	}
    }

    public static void main(String[] args) {
	TempParser parser = new TempParser();
	parser.run();
    }
}
