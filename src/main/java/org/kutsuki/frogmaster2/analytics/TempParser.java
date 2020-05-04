package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;

public class TempParser extends AbstractParser {
    private static final BigDecimal HUNDRED = new BigDecimal(100);
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atESD.txt";

    @Override
    public File getFile(Symbol symbol) {
	return new File(FILE_NAME);
    }

    public void run() {
	setTicker(StringUtils.substringAfterLast(FILE_NAME, Character.toString('/')));
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    TreeMap<LocalDate, Bar> resultMap = new TreeMap<LocalDate, Bar>();

	    boolean first = true;
	    Bar prevBar = null;
	    int totalGapUp = 0;
	    int totalGapDown = 0;
	    int prevRangeUp = 0;
	    int prevRangeDown = 0;
	    int trendUp = 0;
	    int trendDown = 0;

	    int bankroll = 0;
	    int gain = 0;
	    int gainCount = 0;
	    int loss = 0;
	    int lossCount = 0;
	    int midpointUp = 0;
	    int midpointDown = 0;
	    int lowUp = 0;
	    int lowDown = 0;

	    for (Bar bar : barMap.values()) {
		if (!first) {
		    if (bar.getOpen() > prevBar.getHigh() && bar.getOpen() - prevBar.getHigh() >= 5000) {
			if (isPreviousDayRange(bar, prevBar, true)) {
			    int midpoint = (prevBar.getHigh() + prevBar.getLow()) / 2;
			    if (bar.getLow() <= midpoint) {
				midpointUp++;
			    }

			    if (bar.getLow() <= prevBar.getLow()) {
				lowUp++;
			    }

			    prevRangeUp++;
			}

			if (isTrend(bar, true)) {
			    trendUp++;
			}

			int net = bar.getClose() - bar.getOpen();

			if (net > 0) {
			    gain += net;
			    gainCount++;
			} else if (net < 0) {
			    loss += net;
			    lossCount++;
			}

			bankroll += net;

			totalGapUp++;
		    } else if (bar.getOpen() < prevBar.getLow() && prevBar.getLow() - bar.getOpen() >= 5000) {
			if (isPreviousDayRange(bar, prevBar, false)) {
			    int midpoint = (prevBar.getHigh() + prevBar.getLow()) / 2;
			    if (bar.getHigh() >= midpoint) {
				midpointDown++;
			    }

			    if (bar.getHigh() >= prevBar.getHigh()) {
				lowDown++;
			    }

			    prevRangeDown++;
			}

			if (isTrend(bar, false)) {
			    trendDown++;
			}

			int net = bar.getOpen() - bar.getClose();

			if (net > 0) {
			    gain += net;
			    gainCount++;
			} else if (net < 0) {
			    loss += net;
			    lossCount++;
			}

			bankroll += net;

			totalGapDown++;
		    }
		}

		prevBar = bar;
		first = false;
	    }

	    // System.out.println("TotalGapUp: " + totalGapUp);

	    // System.out.println("TrendUp: " + trendUp);
	    // System.out.println("TotalGapDown: " + totalGapDown);
	    // System.out.println("PrevRangeDown: " + prevRangeDown);
	    // System.out.println("TrendDown: " + trendDown);

	    System.out.println("bankroll: " + bankroll / 100);
	    // System.out.println("gain: " + gain / 100);
	    // System.out.println("gainCount: " + gainCount);
	    System.out.println("gainAvg: " + gain / 100 / gainCount);
	    // System.out.println("loss: " + loss / 100);
	    // System.out.println("lossCount: " + lossCount);
	    System.out.println("lossAvg: " + loss / 100 / lossCount);

	    System.out.println("PrevRangeUp: " + prevRangeUp);
	    System.out.println("PrevRangeDown: " + prevRangeDown);
	    System.out.println("MidpointUp: " + midpointUp);
	    System.out.println("MidpointDown: " + midpointDown);
	    System.out.println("lowUp: " + lowUp);
	    System.out.println("lowDown: " + lowDown);

	}
    }

    private boolean isPreviousDayRange(Bar bar, Bar prevBar, boolean gapUp) {
	boolean result = false;

	if (gapUp) {
	    result = prevBar.getHigh() >= bar.getLow();
	} else {
	    result = prevBar.getLow() <= bar.getHigh();
	}

	return result;
    }

    private boolean isTrend(Bar bar, boolean gapUp) {
	boolean result = false;

	if (gapUp) {
	    result = bar.getClose() - bar.getOpen() >= 1000;
	} else {
	    result = bar.getOpen() - bar.getClose() >= 1000;
	}

	return result;
    }

    public static void main(String[] args) {
	TempParser parser = new TempParser();
	parser.run();
    }
}
