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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.strategy.LongStrategy;
import org.kutsuki.frogmaster.strategy.NoStrategy;

public class TradestationParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final File DIR = new File("C:/Users/Matcha Green/Desktop/ES");

    private TreeMap<String, String> realizedMap;
    private TreeMap<String, String> equityResultMap;

    public TradestationParser() {
	this.realizedMap = new TreeMap<String, String>();
	this.equityResultMap = new TreeMap<String, String>();
    }

    public void run(File file) {
	Ticker ticker = new Ticker(file.getName());
	TreeMap<LocalDateTime, Bar> barMap = new TreeMap<LocalDateTime, Bar>();

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

	LongStrategy strategy1 = new LongStrategy(ticker, barMap);
	// ShortStrategy2 strategy1 = new ShortStrategy2(ticker, barMap);
	strategy1.run();
	NoStrategy strategy2 = new NoStrategy(ticker, barMap);
	strategy2.run();

	BigDecimal min = new BigDecimal(10000);
	LocalDateTime minDateTime = null;

	for (LocalDateTime key : strategy1.getEquityMap().keySet()) {
	    Equity e1 = strategy1.getEquityMap().get(key);
	    Equity e2 = strategy2.getEquityMap().get(key);

	    BigDecimal total = e1.getUnrealized().add(e1.getRealized()).add(e2.getUnrealized()).add(e2.getRealized());

	    if (total.compareTo(min) == -1) {
		min = total;
		minDateTime = key;
	    }
	}

	String realized = strategy1.getEquityMap().lastEntry().getValue().getRealized()
		.add(strategy2.getEquityMap().lastEntry().getValue().getRealized()).toString();
	realizedMap.put(ticker.toString(), realized);
	equityResultMap.put(ticker.toString(), minDateTime + StringUtils.SPACE + min);
    }

    public Map<String, String> getEquityResultMap() {
	return equityResultMap;
    }

    public Map<String, String> getRealizedMap() {
	return realizedMap;
    }

    public String getRealized(char month, int year) {
	return getResult(getRealizedMap(), month, year);
    }

    public String getEquityResult(char month, int year) {
	return getResult(getEquityResultMap(), month, year);
    }

    private String getResult(Map<String, String> map, char month, int year) {
	Ticker ticker = new Ticker(month, year);
	String result = map.get(ticker.toString());
	if (StringUtils.isNotBlank(result) && result.contains(StringUtils.SPACE)) {
	    result = StringUtils.substringAfter(result, StringUtils.SPACE);
	}

	return result;
    }

    public static void main(String[] args) {
	TradestationParser parser = new TradestationParser();

	// for (File file : DIR.listFiles()) {
	// parser.run(file);
	// }

	File file = new File(DIR + "/ESH14.txt");
	parser.run(file);

	System.out.println("Realized");
	for (Entry<String, String> entry : parser.getRealizedMap().entrySet()) {
	    System.out.println(entry.getKey() + StringUtils.SPACE + entry.getValue());
	}

	System.out.println("RealizedMap");
	for (int i = 17; i >= 6; i--) {
	    String h = parser.getRealized('H', i);
	    String m = parser.getRealized('M', i);
	    String u = parser.getRealized('U', i);
	    String z = parser.getRealized('Z', i);
	    System.out.println(h + "," + m + "," + u + "," + z);
	}

	System.out.println("--------------------------");
	System.out.println("EquityResult");
	for (Entry<String, String> entry : parser.getEquityResultMap().entrySet()) {
	    System.out.println(entry.getKey() + StringUtils.SPACE + entry.getValue());
	}

	System.out.println("EquityMap");
	for (int i = 17; i >= 6; i--) {
	    String h = parser.getRealized('H', i);
	    String m = parser.getRealized('M', i);
	    String u = parser.getRealized('U', i);
	    String z = parser.getRealized('Z', i);
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }
}
