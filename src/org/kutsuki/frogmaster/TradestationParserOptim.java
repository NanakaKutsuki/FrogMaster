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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.strategy.HybridStrategy;

public class TradestationParserOptim {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int YEAR = 17;
    private static final String DIR = "C:/Users/Scraper/Desktop/ES/";
    private static final String TXT = ".txt";

    private Map<String, Ticker> tickerMap;

    public TradestationParserOptim() {
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

    public void run(char month, int year, int hour, BigDecimal costPerContractBar) {
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

	    // ShortStrategy2 strategy = new ShortStrategy2(ticker, barMap, bankrollBar);
	    HybridStrategy strategy = new HybridStrategy(ticker, barMap, bankrollBar);
	    strategy.setHour(hour);
	    strategy.setCostPerContractBar(costPerContractBar);
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
	TradestationParserOptim parser = new TradestationParserOptim();
	List<BigDecimal> costPerContractBarList = new ArrayList<BigDecimal>();

	for (int hour = 0; hour < 24; hour++) {
	    if (hour != 18) {
		BigDecimal costPerContractBar = new BigDecimal("10000");
		boolean found = false;
		while (!found) {
		    try {
			for (int year = 9; year <= YEAR; year++) {
			    parser.run('H', year, hour, costPerContractBar);
			    parser.run('M', year, hour, costPerContractBar);
			    parser.run('U', year, hour, costPerContractBar);
			    parser.run('Z', year, hour, costPerContractBar);
			}

			found = true;
		    } catch (IllegalStateException e) {
			costPerContractBar = costPerContractBar.add(BigDecimal.valueOf(100));
		    }
		}

		System.out.println(parser.getTicker('Z', 17).getBankrollBar());
		costPerContractBarList.add(costPerContractBar);
	    } else {
		System.out.println('X');
		costPerContractBarList.add(BigDecimal.ZERO);
	    }
	}

	System.out.println("done!");

	for (BigDecimal bd : costPerContractBarList) {
	    System.out.println(bd);
	}
    }
}
