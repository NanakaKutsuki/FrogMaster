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
import org.kutsuki.frogmaster.strategy.HybridStrategy;
import org.kutsuki.frogmaster.strategy.NoStrategy;

public class TradestationParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final File DIR = new File("C:/Users/Matcha Green/Desktop/ES");

    private TreeMap<String, String> resultMap;

    public TradestationParser() {
	this.resultMap = new TreeMap<String, String>();
    }

    public void run(File file, Input input) {
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

	// LongStrategy strategy1 = new LongStrategy(barMap);
	// strategy1.run();
	//
	// ShortStrategy2 strategy2 = new ShortStrategy2(barMap, input);
	// strategy2.run();

	HybridStrategy strategy1 = new HybridStrategy(barMap, input);
	strategy1.run();
	NoStrategy strategy2 = new NoStrategy(barMap);
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

	resultMap.put(Inputs.getTicker(file.getName()), minDateTime + StringUtils.SPACE + min);
    }

    public Map<String, String> getResultMap() {
	return resultMap;
    }

    public String getResult(int year, char quarter) {
	StringBuilder ticker = new StringBuilder("ES");

	ticker.append(quarter);

	if (year < 10) {
	    ticker.append(0);
	}
	ticker.append(year);

	return StringUtils.substringAfter(getResultMap().get(ticker.toString()), StringUtils.SPACE);
    }

    public static void main(String[] args) {
	TradestationParser parser = new TradestationParser();

	for (File file : DIR.listFiles()) {
	    Input input = Inputs2.getInput(file.getName());
	    parser.run(file, input);
	}

	// File file = new File(DIR + "/ESU15.txt");
	// Input input = Inputs.getInput(file.getName());
	// parser.run(file, input);

	for (Entry<String, String> entry : parser.getResultMap().entrySet()) {
	    System.out.println(entry.getKey() + StringUtils.SPACE + entry.getValue());
	}

	for (int i = 17; i >= 6; i--) {
	    String h = parser.getResult(i, 'H');
	    String m = parser.getResult(i, 'M');
	    String u = parser.getResult(i, 'U');
	    String z = parser.getResult(i, 'Z');
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }
}
