package org.kutsuki.frogmaster.marketprofile;

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
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.Bar;

public class ESDParser {
    private static final BigDecimal TICK = BigDecimal.valueOf(0.25);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime START_TIME = LocalTime.of(9, 59);
    private static final LocalTime END_TIME = LocalTime.of(16, 31);
    private static final LocalTime TEN_AM = LocalTime.of(10, 00);
    private static final LocalTime FOUR_PM = LocalTime.of(16, 00);
    private static final String PATHNAME = "C:/Users/Scraper/Desktop/ESD.txt";

    private BigDecimal high;
    private BigDecimal low;
    private TreeMap<LocalDateTime, Bar> barMap;

    public ESDParser() {
	this.barMap = new TreeMap<LocalDateTime, Bar>();
    }

    public void run() {
	parse();

	TreeMap<BigDecimal, Integer> tpoMap = new TreeMap<BigDecimal, Integer>();
	for (Entry<LocalDateTime, Bar> entry : barMap.entrySet()) {
	    LocalTime time = entry.getKey().toLocalTime();

	    if (time.isAfter(START_TIME) && time.isBefore(END_TIME)) {
		System.out.println(entry.getKey());
		if (time.equals(TEN_AM)) {
		    high = BigDecimal.ZERO;
		    low = BigDecimal.ZERO;
		}

		tpoMap.clear();

		LocalDateTime key = LocalDateTime.of(entry.getKey().toLocalDate(), TEN_AM);
		while (key.isBefore(entry.getKey()) || key.isEqual(entry.getKey())) {
		    Bar bar = barMap.get(key);

		    if (bar.getHigh().compareTo(high) == 1) {
			high = bar.getHigh();
		    }

		    if (bar.getLow().compareTo(low) == -1) {
			low = bar.getLow();
		    }

		    for (BigDecimal price = bar.getLow(); price.compareTo(bar.getHigh()) <= 0; price = price
			    .add(TICK)) {
			if (tpoMap.containsKey(price)) {
			    tpoMap.put(price, tpoMap.get(price) + 1);
			} else {
			    tpoMap.put(price, 1);
			}
		    }

		    if (key.toLocalTime().equals(FOUR_PM)) {
			key = key.plusMinutes(15);
		    } else {
			key = key.plusMinutes(30);
		    }
		}
	    }

	    if (time.equals(END_TIME)) {
		System.exit(0);
	    }
	}
    }

    private void parse() {
	try (BufferedReader br = new BufferedReader(new FileReader(new File(PATHNAME)))) {
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
    }

    public static void main(String[] args) {
	ESDParser parser = new ESDParser();
	parser.run();
    }
}
