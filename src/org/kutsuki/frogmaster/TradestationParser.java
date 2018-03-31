package org.kutsuki.frogmaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.strategy.HybridStrategy2;

public class TradestationParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int YEAR = LocalDate.now().getYear() - 2000;
    private static final String DIR = "C:/Users/Scraper/Desktop/ES/";
    private static final String TXT = ".txt";

    private Map<String, Ticker> tickerMap;

    public TradestationParser() {
	this.tickerMap = new HashMap<String, Ticker>();

	for (int year = 6; year <= YEAR; year++) {
	    Ticker h = new Ticker('H', year);
	    Ticker m = new Ticker('M', year);
	    Ticker u = new Ticker('U', year);
	    Ticker z = new Ticker('Z', year);

	    tickerMap.put(h.toString(), h);
	    tickerMap.put(m.toString(), m);
	    tickerMap.put(u.toString(), u);
	    tickerMap.put(z.toString(), z);
	}
    }

    public void run(char month, int year) {
	Ticker ticker = getTicker(month, year);
	TreeMap<LocalDateTime, Bar> barMap = new TreeMap<LocalDateTime, Bar>();

	StringBuilder sb = new StringBuilder();
	sb.append(DIR);
	sb.append(ticker);
	sb.append(TXT);
	File file = new File(sb.toString());

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

	    Ticker prevTicker = getPrevTicker(ticker);
	    BigDecimal bankroll = prevTicker.getBankroll();
	    BigDecimal bankrollBar = prevTicker.getBankrollBar();
	    BigDecimal numContracts = prevTicker.getNumContracts();

	    // LongStrategy strategy = new LongStrategy(ticker, barMap, bankrollBar);
	    HybridStrategy2 strategy = new HybridStrategy2(ticker, barMap, bankrollBar);
	    strategy.run();

	    // calculate quarterly bankroll
	    if (ticker.getYear() > 8) {
		if (numContracts == null) {
		    numContracts = prevTicker.getBankroll().divide(strategy.getCostPerContract(), 0,
			    RoundingMode.FLOOR);
		}

		bankroll = bankroll.add(strategy.getBankroll().multiply(numContracts));
		numContracts = bankroll.divide(strategy.getCostPerContract(), 0, RoundingMode.FLOOR);
	    }

	    // set ticker data
	    ticker.setEquityDateTime(strategy.getLowestEquityDateTime());
	    ticker.setEquity(strategy.getLowestEquity());
	    ticker.setRealized(strategy.getBankroll());
	    ticker.setBankroll(bankroll);
	    ticker.setNumContracts(numContracts);
	    ticker.setBankrollBar(strategy.getBankrollBar());
	    tickerMap.put(ticker.toString(), ticker);
	}
    }

    public Ticker getTicker(char month, int year) {
	return tickerMap.get(Ticker.getKey(month, year));
    }

    public void printEquityDateTime() {
	System.out.println("Lowest Equity Dates");
	for (int year = YEAR; year >= 6; year--) {
	    Ticker h = getTicker('H', year);
	    Ticker m = getTicker('M', year);
	    Ticker u = getTicker('U', year);
	    Ticker z = getTicker('Z', year);

	    LocalDateTime hDate = h.getEquityDateTime();
	    LocalDateTime uDate = u.getEquityDateTime();
	    LocalDateTime mDate = m.getEquityDateTime();
	    LocalDateTime zDate = z.getEquityDateTime();

	    if (hDate != null) {
		System.out.println(h + "," + hDate.toLocalDate() + "," + hDate.toLocalTime());
	    }

	    if (mDate != null) {
		System.out.println(m + "," + mDate.toLocalDate() + "," + mDate.toLocalTime());
	    }

	    if (uDate != null) {
		System.out.println(u + "," + uDate.toLocalDate() + "," + uDate.toLocalTime());
	    }

	    if (zDate != null) {
		System.out.println(z + "," + zDate.toLocalDate() + "," + zDate.toLocalTime());
	    }
	}
    }

    public void printRealized() {
	System.out.println("--------------------------");
	System.out.println("Realized");
	for (int year = YEAR; year >= 6; year--) {
	    BigDecimal h = getTicker('H', year).getRealized();
	    BigDecimal m = getTicker('M', year).getRealized();
	    BigDecimal u = getTicker('U', year).getRealized();
	    BigDecimal z = getTicker('Z', year).getRealized();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    public void printEquity() {
	System.out.println("--------------------------");
	System.out.println("Lowest Equity");
	for (int year = YEAR; year >= 6; year--) {
	    BigDecimal h = getTicker('H', year).getEquity();
	    BigDecimal m = getTicker('M', year).getEquity();
	    BigDecimal u = getTicker('U', year).getEquity();
	    BigDecimal z = getTicker('Z', year).getEquity();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    public void printRebalanceQuarterly() {
	System.out.println("--------------------------");
	System.out.println("Bankroll - Rebalance each Quarter");
	for (int year = YEAR; year >= 9; year--) {
	    BigDecimal h = getTicker('H', year).getBankroll();
	    BigDecimal m = getTicker('M', year).getBankroll();
	    BigDecimal u = getTicker('U', year).getBankroll();
	    BigDecimal z = getTicker('Z', year).getBankroll();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    public void printRebalanceBar() {
	System.out.println("--------------------------");
	System.out.println("Bankroll - Rebalance each Bar");
	for (int year = YEAR; year >= 9; year--) {
	    BigDecimal h = getTicker('H', year).getBankrollBar();
	    BigDecimal m = getTicker('M', year).getBankrollBar();
	    BigDecimal u = getTicker('U', year).getBankrollBar();
	    BigDecimal z = getTicker('Z', year).getBankrollBar();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    private Ticker getPrevTicker(Ticker ticker) {
	Ticker prevTicker = null;

	switch (ticker.getMonth()) {
	case 'H':
	    prevTicker = getTicker('Z', ticker.getYear() - 1);
	    break;
	case 'M':
	    prevTicker = getTicker('H', ticker.getYear());
	    break;
	case 'U':
	    prevTicker = getTicker('M', ticker.getYear());
	    break;
	case 'Z':
	    prevTicker = getTicker('U', ticker.getYear());
	    break;
	default:
	    throw new IllegalStateException("Previous Ticker not found: " + ticker);
	}

	if (prevTicker == null) {
	    prevTicker = new Ticker('X', -1);
	}

	return prevTicker;
    }

    public static void main(String[] args) {
	TradestationParser parser = new TradestationParser();
	// parser.run('U', 7);

	for (int year = 6; year <= YEAR; year++) {
	    parser.run('H', year);
	    parser.run('M', year);
	    parser.run('U', year);
	    parser.run('Z', year);
	}

	// parser.printEquityDateTime();
	parser.printRealized();
	parser.printEquity();
	// parser.printRebalanceQuarterly();
	// parser.printRebalanceBar();
    }
}
