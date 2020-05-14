package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;

public class MarketProfileParser extends AbstractParser {
    private static final BigDecimal TWO = new BigDecimal("2");
    private static final BigDecimal TWELVE_FIVE = new BigDecimal("12.5");
    private static final LocalTime START = LocalTime.of(9, 30);
    private static final LocalTime END = LocalTime.of(16, 15);
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

    private int low30;
    private int high30;
    private int poc;
    private int vah;
    private int val;
    private int lastPoc;
    private int lastVah;
    private int lastVal;
    private BarMap barMap;
    private TreeMap<Integer, Integer> tpoMap;

    public MarketProfileParser() {
	setTicker(StringUtils.substringAfterLast(FILE_NAME, Character.toString('/')));
	File file = getFile(null);
	if (!file.exists()) {
	    throw new IllegalStateException("at File Missing: " + FILE_NAME);
	}

	this.barMap = load(file);
	this.high30 = 0;
	this.lastPoc = 0;
	this.lastVah = 0;
	this.lastVal = 0;
	this.low30 = Integer.MAX_VALUE;
	this.poc = 0;
	this.tpoMap = new TreeMap<Integer, Integer>();
	this.vah = 0;
	this.val = 0;
    }

    @Override
    public File getFile(Symbol symbol) {
	return new File(FILE_NAME);
    }

    public void run() {
	int open = 0;
	int total = 0;

	for (int i = 0; i < barMap.size(); i++) {
	    Bar bar = barMap.get(i);
	    addTpos(bar);

	    LocalTime time = bar.getTime();
	    if (time.equals(START)) {
		open = bar.getOpen();
	    } else if (time.equals(END) && lastVal != 0 && lastVah != 0) {
		int close = bar.getClose();
		total += open - close;
	    }
	}

	System.out.println(total);
    }

    private void addTpos(Bar bar) {
	LocalTime time = bar.getTime();

	if (time.equals(START)) {
	    lastPoc = poc;
	    lastVah = vah;
	    lastVal = val;

	    high30 = 0;
	    low30 = Integer.MAX_VALUE;
	    tpoMap.clear();
	}

	if (time.isAfter(START) && (time.isBefore(END) || time.equals(END))) {
	    if (bar.getHigh() > high30) {
		high30 = bar.getHigh();
	    }

	    if (bar.getLow() < low30) {
		low30 = bar.getLow();
	    }

	    if (time.getMinute() == 0 || time.getMinute() == 30) {
		for (int i = low30; i <= high30; i += 25) {
		    Integer tpo = tpoMap.get(i);

		    if (tpo == null) {
			tpo = 0;
		    }

		    tpoMap.put(i, tpo + 1);
		}

		high30 = 0;
		low30 = Integer.MAX_VALUE;

		findPoc();
	    }
	}
    }

    private int getMedianPrice() {
	BigDecimal median = new BigDecimal(tpoMap.firstKey() + tpoMap.lastKey());
	median = median.divide(TWO, 2, RoundingMode.HALF_UP);

	if (median.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
	    median = median.add(TWELVE_FIVE);
	}

	return median.intValue();
    }

    private void findPoc() {
	int median = getMedianPrice();
	int longest = 0;
	int total = 0;
	for (Entry<Integer, Integer> e : tpoMap.entrySet()) {
	    if (e.getValue() > longest) {
		longest = e.getValue();
	    }

	    total += e.getValue();
	}

	int moveUp = 25;
	int moveDown = 25;
	this.poc = 0;
	if (tpoMap.get(median) == longest) {
	    poc = median;
	} else {
	    while (poc == 0) {
		if (tpoMap.get(median + moveUp) == longest) {
		    poc = median + moveUp;
		} else if (tpoMap.get(median - moveDown) == longest) {
		    poc = median - moveDown;
		}

		moveUp += 25;
		moveDown += 25;
	    }
	}

	findValueArea(total);
    }

    private void findValueArea(int total) {
	int count = tpoMap.get(poc);
	int goal = (int) (total * .7);
	int moveDown = 25;
	int moveUp = 25;

	while (count < goal) {
	    int upTpos = 0;
	    if (poc + moveUp + 25 <= tpoMap.lastKey()) {
		upTpos = tpoMap.get(poc + moveUp) + tpoMap.get(poc + moveUp + 25);
	    }

	    int downTpos = 0;
	    if (poc - moveDown - 25 >= tpoMap.firstKey()) {
		downTpos = tpoMap.get(poc - moveDown) + tpoMap.get(poc - moveDown - 25);
	    }

	    if (upTpos >= downTpos) {
		count += tpoMap.get(poc + moveUp);
		vah = poc + moveUp;
		moveUp += 25;

		if (count < goal) {
		    count += tpoMap.get(poc + moveUp);
		    vah = poc + moveUp;
		    moveUp += 25;
		}
	    } else {
		count += tpoMap.get(poc - moveDown);
		val = poc - moveDown;
		moveDown += 25;

		if (count < goal) {
		    count += tpoMap.get(poc - moveDown);
		    val = poc - moveDown;
		    moveDown += 25;
		}
	    }
	}
    }

    public static void main(String[] args) {
	MarketProfileParser parser = new MarketProfileParser();
	parser.run();
    }
}
