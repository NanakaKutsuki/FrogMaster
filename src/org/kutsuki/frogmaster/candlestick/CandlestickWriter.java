package org.kutsuki.frogmaster.candlestick;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class CandlestickWriter {
    private static final int INTERVAL = 5;

    private TreeMap<LocalDateTime, Candlestick> candlestickMap;

    public CandlestickWriter() {
	this.candlestickMap = new TreeMap<>();
    }

    public void parse(File file, String symbol) {
	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    // parse data
	    String line = null;
	    while ((line = br.readLine()) != null) {
		String[] split = StringUtils.split(line, ',');

		if (split.length == 4) {
		    // parse time first
		    LocalTime time = null;
		    try {
			time = LocalTime.parse(split[1]);
		    } catch (DateTimeParseException e) {
			e.printStackTrace();
		    }

		    // parse date
		    LocalDate date = null;
		    try {
			date = LocalDate.parse(split[0]);
		    } catch (DateTimeParseException e) {
			e.printStackTrace();
		    }

		    // parse price
		    BigDecimal price = null;
		    try {
			price = new BigDecimal(split[2]);
		    } catch (NumberFormatException e) {
			e.printStackTrace();
		    }

		    if (date != null && time != null && price != null) {
			addData(date, time, price);
		    }
		} else {
		    System.err.println("Bad Line: " + line);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(symbol + "_candlestick.csv")))) {
	    for (Entry<LocalDateTime, Candlestick> entry : candlestickMap.entrySet()) {
		bw.write(entry.getValue().toString());
		bw.newLine();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void addData(LocalDate date, LocalTime time, BigDecimal price) {
	int minute = time.getMinute();
	LocalDateTime key = LocalDateTime.of(date, time).withSecond(0).withNano(0);

	int i = INTERVAL;
	boolean found = false;
	while (!found && i <= 60) {
	    if (minute < i) {
		key = key.withMinute(i - INTERVAL);
		found = true;
	    }

	    i += INTERVAL;
	}

	Candlestick candle = candlestickMap.get(key);

	if (candle == null) {
	    candle = new Candlestick(key);
	    candlestickMap.put(key, candle);
	}

	candle.add(time, price);
    }

    public static void main(String[] args) {
	String path = "C:/Users/Matcha Green/Desktop/futures/";

	CandlestickWriter parser = new CandlestickWriter();
	for (File file : new File(path).listFiles()) {
	    String symbol = StringUtils.substringBefore(file.getName(), Character.toString('.'));
	    System.out.println(symbol);
	    parser.parse(file, symbol);
	}
    }
}
