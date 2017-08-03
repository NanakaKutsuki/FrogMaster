package org.kutsuki.frogmaster.candlestick;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class CandlestickFilled {
    private TreeMap<LocalDateTime, Candlestick> candlestickMap;

    public CandlestickFilled() {
	this.candlestickMap = new TreeMap<>();
    }

    public void parse(File file) {
	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    // parse data
	    String line = null;
	    while ((line = br.readLine()) != null) {
		String[] split = StringUtils.split(line, ',');

		if (split.length == 5) {
		    // parse date and time
		    LocalDateTime key = null;
		    try {
			key = LocalDateTime.parse(split[0]);
		    } catch (DateTimeParseException e) {
			e.printStackTrace();
		    }

		    // parse price
		    BigDecimal open = null;
		    BigDecimal close = null;
		    BigDecimal high = null;
		    BigDecimal low = null;

		    try {
			open = new BigDecimal(split[1]);
			close = new BigDecimal(split[2]);
			high = new BigDecimal(split[3]);
			low = new BigDecimal(split[4]);
		    } catch (NumberFormatException e) {
			e.printStackTrace();
		    }

		    // add data
		    if (key != null && open != null && close != null && high != null && low != null) {
			Candlestick candlestick = new Candlestick(key, open, close, high, low);
			candlestickMap.put(key, candlestick);
		    }

		} else {
		    System.err.println("Bad Line: " + line);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void fillIn(String symbol) {
	BigDecimal four = new BigDecimal(4);
	BigDecimal five = new BigDecimal(300000);
	BigDecimal twentyFive = new BigDecimal("0.25");
	ZoneId utc = ZoneId.of("UTC");

	List<LocalDateTime> keyList = new ArrayList<>(candlestickMap.keySet());
	Collections.sort(keyList);
	for (int i = 1; i < keyList.size(); i++) {
	    Candlestick stick = candlestickMap.get(keyList.get(i));
	    Candlestick prevStick = candlestickMap.get(keyList.get(i - 1));

	    long gap = stick.getKey().atZone(utc).toInstant().toEpochMilli()
		    - prevStick.getKey().atZone(utc).toInstant().toEpochMilli();

	    if (gap > five.longValue()) {
		BigDecimal missing = BigDecimal.valueOf(gap).divide(five, 0, RoundingMode.HALF_UP);
		BigDecimal diff = stick.getOpen().subtract(prevStick.getClose());

		BigDecimal tick = diff.divide(missing.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP).multiply(four)
			.setScale(0, RoundingMode.HALF_UP).divide(four, 2, RoundingMode.HALF_UP);

		for (int j = 0; j < missing.intValue() - 1; j++) {
		    LocalDateTime key = prevStick.getKey().plusMinutes(5 * (j + 1));

		    BigDecimal price = null;
		    if (tick.compareTo(twentyFive) == -1) {
			price = prevStick.getClose();
		    } else {
			price = prevStick.getClose().add(tick.multiply(BigDecimal.valueOf(j).add(BigDecimal.ONE)));
		    }

		    Candlestick missingStick = new Candlestick(key, price, price, price, price);
		    candlestickMap.put(key, missingStick);
		}
	    }
	}

	try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(symbol + "_candlestickFilled.csv")))) {
	    // List<LocalDateTime> keyList2 = new ArrayList<>(candlestickMap.keySet());
	    // Collections.sort(keyList2);
	    for (Candlestick stick : candlestickMap.values()) {
		bw.write(stick.toString());
		bw.newLine();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	String path = "C:/Users/Matcha Green/Desktop/candlestick";

	File dir = new File(path);
	for (File file : dir.listFiles()) {
	    String symbol = StringUtils.substringBefore(file.getName(), Character.toString('_'));

	    // if (symbol.equals("ESZ16")) {
	    CandlestickFilled parser = new CandlestickFilled();
	    parser.parse(file);
	    parser.fillIn(symbol);
	    // }
	}
    }
}
