package org.kutsuki.frogmaster;

import java.io.BufferedReader;
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
import org.kutsuki.frogmaster.strategy.ShortStrategy2;

public class TradestationParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DIR = "C:/Users/Matcha Green/Desktop/ES/";
    private static final String TXT = ".txt";

    private Map<String, Ticker> tickerMap;

    public TradestationParser() {
	this.tickerMap = new HashMap<String, Ticker>();

	for (int year = 6; year < 18; year++) {
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

	try (BufferedReader br = new BufferedReader(new FileReader(sb.toString()))) {
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
		    bar.setUp(up);
		    bar.setDown(down);

		    barMap.put(bar.getDateTime(), bar);
		} catch (DateTimeParseException | NumberFormatException e) {
		    e.printStackTrace();
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	BigDecimal bankroll = getPrevTicker(ticker).getBankrollQuarterly();
	BigDecimal bankrollBar = getPrevTicker(ticker).getBankrollBar();
	BigDecimal numContractsQuarterly = getPrevTicker(ticker).getNumContractsQuarterly();
	BigDecimal lowestEquity = new BigDecimal(10000);
	LocalDateTime equityDateTime = null;

	ShortStrategy2 strategy = new ShortStrategy2(ticker, barMap, bankrollBar);
	// HybridStrategy strategy = new HybridStrategy(ticker, barMap, bankrollBar);
	strategy.run();

	for (LocalDateTime key : strategy.getEquityMap().keySet()) {
	    Equity e1 = strategy.getEquityMap().get(key);
	    BigDecimal equity = e1.getRealized().add(e1.getUnrealized());

	    // compare lowest equity found
	    if (equity.compareTo(lowestEquity) == -1) {
		lowestEquity = equity;
		equityDateTime = key;
	    }
	}

	// calculate quarterly bankroll
	BigDecimal realized = strategy.getBankroll();
	if (ticker.getYear() > 8) {
	    bankroll = bankroll.add(realized.multiply(numContractsQuarterly));
	    numContractsQuarterly = bankroll.divide(Ticker.COST_PER_CONTRACT, 0, RoundingMode.FLOOR);
	}

	// set ticker data
	ticker.setEquityDateTime(equityDateTime);
	ticker.setEquity(lowestEquity);
	ticker.setRealized(realized);
	ticker.setBankrollQuarterly(bankroll);
	ticker.setNumContractsQuarterly(numContractsQuarterly);
	ticker.setBankrollBar(strategy.getBankrollBar());
	tickerMap.put(ticker.toString(), ticker);
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
	TradestationParser parser = new TradestationParser();
	// parser.run('H', 9);

	for (int year = 6; year < 18; year++) {
	    parser.run('H', year);
	    parser.run('M', year);
	    parser.run('U', year);
	    parser.run('Z', year);
	}

	System.out.println("Realized");
	for (int year = 17; year >= 6; year--) {
	    BigDecimal h = parser.getTicker('H', year).getRealized();
	    BigDecimal m = parser.getTicker('M', year).getRealized();
	    BigDecimal u = parser.getTicker('U', year).getRealized();
	    BigDecimal z = parser.getTicker('Z', year).getRealized();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}

	System.out.println("--------------------------");
	System.out.println("Lowest Equity");
	for (int year = 17; year >= 6; year--) {
	    BigDecimal h = parser.getTicker('H', year).getEquity();
	    BigDecimal m = parser.getTicker('M', year).getEquity();
	    BigDecimal u = parser.getTicker('U', year).getEquity();
	    BigDecimal z = parser.getTicker('Z', year).getEquity();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}

	System.out.println("--------------------------");
	System.out.println("Bankroll - Rebalance each Quarter");
	for (int year = 17; year >= 9; year--) {
	    BigDecimal h = parser.getTicker('H', year).getBankrollQuarterly();
	    BigDecimal m = parser.getTicker('M', year).getBankrollQuarterly();
	    BigDecimal u = parser.getTicker('U', year).getBankrollQuarterly();
	    BigDecimal z = parser.getTicker('Z', year).getBankrollQuarterly();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}

	System.out.println("--------------------------");
	System.out.println("Bankroll - Rebalance each Bar");
	for (int year = 17; year >= 9; year--) {
	    BigDecimal h = parser.getTicker('H', year).getBankrollBar();
	    BigDecimal m = parser.getTicker('M', year).getBankrollBar();
	    BigDecimal u = parser.getTicker('U', year).getBankrollBar();
	    BigDecimal z = parser.getTicker('Z', year).getBankrollBar();
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }
}
