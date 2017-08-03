package org.kutsuki.frogmaster.candlestick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class CandlestickParser {
    private static final BigDecimal TICK_SIZE = new BigDecimal("0.25");
    private static final BigDecimal TICK_VALUE = new BigDecimal("12.5");
    private static final BigDecimal SCALE = BigDecimal.ONE.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    private static final int INTERVAL = 5;

    private TreeMap<LocalDateTime, Candlestick> candlestickMap;

    public CandlestickParser() {
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
			candlestickMap.put(candlestick.getKey(), candlestick);
		    }

		} else {
		    System.err.println("Bad Line: " + line);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void analyze(String symbol) {
	BigDecimal bankroll = BigDecimal.ZERO;
	BigDecimal lastBuyPrice = BigDecimal.ZERO;
	boolean purchased = false;
	int trades = 0;

	List<LocalDateTime> keyList = getKeyList();
	for (int i = 13; i < keyList.size() - 1; i++) {
	    LocalDateTime key = keyList.get(i);

	    Candlestick stick = getCandlestick(key, 0);
	    Candlestick stick12 = getCandlestick(key, 12);

	    Candlestick stick1 = getCandlestick(key, 1);
	    Candlestick stick13 = getCandlestick(key, 13);

	    Candlestick stickFuture = getCandlestick(key, -1);

	    BigDecimal delta = stick.getClose().subtract(stick12.getClose());
	    BigDecimal delta1 = stick1.getClose().subtract(stick13.getClose());
	    BigDecimal acceleration = delta.subtract(delta1);

	    BigDecimal high = stick.getHigh().add(SCALE);
	    BigDecimal low = stick.getLow().add(SCALE);
	    if (!purchased && delta.compareTo(BigDecimal.ZERO) == 1 && acceleration.compareTo(BigDecimal.ZERO) == 1
		    && stickFuture.getHigh().compareTo(high) >= 0) {
		lastBuyPrice = stickFuture.getOpen().max(low);
		bankroll = bankroll.subtract(tickValue(lastBuyPrice));
		purchased = true;
		trades++;
	    } else if (purchased && delta.compareTo(BigDecimal.ZERO) == 1
		    && acceleration.compareTo(BigDecimal.ZERO) == 1 && stickFuture.getLow().compareTo(low) <= 0) {
		bankroll = bankroll.add(tickValue(stickFuture.getOpen().max(high)));
		purchased = false;
		trades++;
	    }
	}

	// refund last purchase
	if (purchased) {
	    bankroll = bankroll.add(tickValue(lastBuyPrice));
	    trades--;
	}

	System.out.println(symbol + " Bankroll: $" + bankroll + " Trades: " + trades);
    }

    public void analyze2(String symbol) {
	BigDecimal bankroll = BigDecimal.ZERO;
	BigDecimal lastBuyPrice = BigDecimal.ZERO;
	boolean purchased = false;
	int trades = 0;

	List<LocalDateTime> keyList = getKeyList();
	for (int i = 14; i < keyList.size() - 1; i++) {
	    LocalDateTime key = keyList.get(i);

	    Candlestick stick1 = getCandlestick(key, 1);
	    Candlestick stick13 = getCandlestick(key, 13);

	    Candlestick stick2 = getCandlestick(key, 2);
	    Candlestick stick14 = getCandlestick(key, 14);

	    Candlestick stick = getCandlestick(key, 0);
	    Candlestick futureStick = getCandlestick(key, -1);

	    BigDecimal delta1 = stick1.getClose().subtract(stick13.getClose());
	    BigDecimal delta2 = stick2.getClose().subtract(stick14.getClose());
	    BigDecimal acceleration = delta1.subtract(delta2);

	    BigDecimal high = stick1.getHigh().add(SCALE);
	    BigDecimal low = stick1.getLow().add(SCALE);
	    if (!purchased && delta1.compareTo(BigDecimal.ZERO) == 1 && acceleration.compareTo(BigDecimal.ZERO) == 1
		    && stick.getHigh().compareTo(high) >= 0) {
		lastBuyPrice = futureStick.getOpen();
		bankroll = bankroll.subtract(tickValue(lastBuyPrice));
		purchased = true;
		trades++;
	    } else if (purchased && delta1.compareTo(BigDecimal.ZERO) == 1
		    && acceleration.compareTo(BigDecimal.ZERO) == 1 && stick.getLow().compareTo(low) <= 0) {
		bankroll = bankroll.add(tickValue(futureStick.getOpen()));
		purchased = false;
		trades++;
	    }
	}

	// refund last purchase
	if (purchased) {
	    bankroll = bankroll.add(tickValue(lastBuyPrice));
	    trades--;
	}

	System.out.println(symbol + " Bankroll: $" + bankroll + " Trades: " + trades);
    }

    public BigDecimal analyze3(String symbol) {
	// BigDecimal deltaLG = new BigDecimal("0.7");
	// BigDecimal deltaST = BigDecimal.ONE;
	// BigDecimal accelerationLG = new BigDecimal("0.9");
	// BigDecimal accelerationST = BigDecimal.ZERO;

	BigDecimal deltaLG = new BigDecimal("1.8");
	BigDecimal deltaST = new BigDecimal("3.3");
	BigDecimal accelerationLG = new BigDecimal("0.8");
	BigDecimal accelerationST = BigDecimal.ZERO;

	BigDecimal bankroll = BigDecimal.ZERO;
	BigDecimal lastBuyPrice = BigDecimal.ZERO;
	boolean purchased = false;
	int trades = 0;

	List<LocalDateTime> keyList = getKeyList();
	for (int i = 4; i < keyList.size() - 1; i++) {
	    LocalDateTime key = keyList.get(i);

	    Candlestick stick = getCandlestick(key, 0);
	    Candlestick stick3 = getCandlestick(key, 3);

	    Candlestick stick1 = getCandlestick(key, 1);
	    Candlestick stick4 = getCandlestick(key, 4);

	    Candlestick futureStick = getCandlestick(key, -1);

	    BigDecimal delta = stick.getClose().subtract(stick3.getClose());
	    BigDecimal delta1 = stick1.getClose().subtract(stick4.getClose());

	    BigDecimal acceleration = delta.subtract(delta1);
	    BigDecimal high = stick1.getHigh().add(SCALE);
	    BigDecimal low = stick1.getLow().add(SCALE);

	    if (!purchased && delta.abs().compareTo(deltaLG) == 1 && acceleration.abs().compareTo(accelerationLG) == 1
		    && stick.getHigh().compareTo(high) >= 0) {
		lastBuyPrice = futureStick.getOpen();
		bankroll = bankroll.subtract(tickValue(lastBuyPrice));
		purchased = true;
		trades++;
	    } else if (purchased && delta.abs().compareTo(deltaST) == 1
		    && acceleration.abs().compareTo(accelerationST) == 1 && stick.getLow().compareTo(low) <= 0) {
		bankroll = bankroll.add(tickValue(futureStick.getOpen()));
		purchased = false;
		trades++;
	    }
	}

	// refund
	if (purchased) {
	    bankroll = bankroll.add(tickValue(lastBuyPrice));
	    trades--;
	}

	System.out.println(symbol + ", Bankroll: $" + bankroll + ", Trades: " + trades);
	return bankroll;
    }

    private Candlestick getCandlestick(LocalDateTime ldt, int sticks) {
	LocalDateTime key = null;
	if (sticks < 0) {
	    key = ldt.plusMinutes(INTERVAL * -sticks);
	} else {
	    key = ldt.minusMinutes(INTERVAL * sticks);
	}

	if (!candlestickMap.containsKey(key)) {
	    System.out.println(ldt + " missing: " + key);

	    throw new NullPointerException();
	}

	return candlestickMap.get(key);
    }

    private List<LocalDateTime> getKeyList() {
	List<LocalDateTime> keyList = new ArrayList<>(candlestickMap.keySet());
	Collections.sort(keyList);
	return keyList;
    }

    private BigDecimal tickValue(BigDecimal price) {
	return price.divide(TICK_SIZE, 0, RoundingMode.HALF_UP).multiply(TICK_VALUE);
    }

    public static void main(String[] args) {
	String path = "C:/Users/Matcha Green/Desktop/candlestickFilled";

	File dir = new File(path);
	for (File file : dir.listFiles()) {
	    String symbol = StringUtils.substringBefore(file.getName(), Character.toString('_'));

	    // if (symbol.equals("ESZ16")) {
	    CandlestickParser parser = new CandlestickParser();
	    parser.parse(file);
	    parser.analyze2(symbol);
	    // }
	}
    }
}
