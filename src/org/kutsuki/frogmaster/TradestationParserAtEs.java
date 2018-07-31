package org.kutsuki.frogmaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.strategy.HybridStrategyCore;

public class TradestationParserAtEs {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String FILE_NAME = "C:/Users/Scraper/Desktop/atEs.txt";

    private Ticker ticker;

    public TradestationParserAtEs() {
	this.ticker = new Ticker('A', 6);
    }

    public void run() {
	TreeMap<LocalDateTime, Bar> barMap = new TreeMap<LocalDateTime, Bar>();
	File file = new File(FILE_NAME);

	if (file.exists()) {
	    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		// skip first line
		br.readLine();

		String line = null;
		while ((line = br.readLine()) != null) {
		    String[] split = StringUtils.split(line, ',');

		    try {
			LocalDate date = LocalDate.parse(split[0], DATE_FORMAT);
			LocalTime time = LocalTime.parse(split[1], TIME_FORMAT);
			BigDecimal open = new BigDecimal(split[2]);
			BigDecimal high = new BigDecimal(split[3]);
			BigDecimal low = new BigDecimal(split[4]);
			BigDecimal close = new BigDecimal(split[5]);
			int up = Integer.parseInt(split[6]);
			int down = Integer.parseInt(split[7]);

			Bar bar = new Bar();
			bar.setDateTime(LocalDateTime.of(date, time));
			bar.setOpen(open);
			bar.setHigh(high);
			bar.setLow(low);
			bar.setClose(close);
			bar.setUpTicks(up);
			bar.setDownTicks(down);

			barMap.put(bar.getDateTime(), bar);
		    } catch (DateTimeParseException | NumberFormatException e) {
			e.printStackTrace();
		    }
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    // LongStrategy2 strategy = new LongStrategy2(ticker, barMap);
	    // HybridStrategyOG strategy = new HybridStrategyOG(ticker, barMap);
	    // TheLyon strategy = new TheLyon(ticker, barMap);
	    HybridStrategyCore strategy = new HybridStrategyCore(ticker, barMap);
	    strategy.run();

	    // set ticker data
	    ticker.setRealized(strategy.getBankroll());
	}
    }

    public void printSummary() {
	System.out.println("Realized: " + ticker.getRealized());
    }

    public static void main(String[] args) {
	TradestationParserAtEs parser = new TradestationParserAtEs();
	parser.run();
	parser.printSummary();
    }
}
