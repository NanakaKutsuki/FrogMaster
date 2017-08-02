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

import org.apache.commons.lang3.StringUtils;

public class CandlestickParser {
    private static final BigDecimal COMMISSION = new BigDecimal("1.5");
    private static final BigDecimal TICK_SIZE = new BigDecimal("0.25");
    private static final BigDecimal TICK_VALUE = new BigDecimal("12.5");
    private static final BigDecimal SCALE = BigDecimal.ONE.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    private List<Candlestick> candlestickList;

    public CandlestickParser() {
	this.candlestickList = new ArrayList<>();
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
			candlestickList.add(candlestick);
		    }

		} else {
		    System.err.println("Bad Line: " + line);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public BigDecimal analyze() {
	BigDecimal bankroll = BigDecimal.ZERO;
	BigDecimal lastBuyPrice = BigDecimal.ZERO;

	boolean purchased = false;
	Collections.sort(candlestickList);

	for (int i = 13; i < candlestickList.size() - 1; i++) {
	    Candlestick stick = candlestickList.get(i);
	    Candlestick stick12 = candlestickList.get(i - 12);

	    Candlestick stick1 = candlestickList.get(i - 1);
	    Candlestick stick13 = candlestickList.get(i - 12 - 1);

	    Candlestick stickFuture = candlestickList.get(i + 1);

	    BigDecimal delta = stick.getClose().subtract(stick12.getClose());
	    BigDecimal delta1 = stick1.getClose().subtract(stick13.getClose());
	    BigDecimal acceleration = delta.subtract(delta1);

	    BigDecimal high = stick.getHigh().add(SCALE);
	    BigDecimal low = stick.getLow().add(SCALE);
	    if (!purchased && delta.compareTo(BigDecimal.ZERO) == 1 && acceleration.compareTo(BigDecimal.ZERO) == 1
		    && stickFuture.getHigh().compareTo(high) >= 0) {
		lastBuyPrice = stickFuture.getOpen().max(low);
		bankroll = bankroll
			.subtract(lastBuyPrice.divide(TICK_SIZE, 2, RoundingMode.HALF_UP).multiply(TICK_VALUE));
		bankroll = bankroll.subtract(COMMISSION);
		purchased = true;
		// System.out.println("Buy: " + stick.getKey() + " " +
		// stickFuture.getOpen().max(low) + " " + bankroll);
	    } else if (purchased && delta.compareTo(BigDecimal.ZERO) == 1
		    && acceleration.compareTo(BigDecimal.ZERO) == 1 && stickFuture.getLow().compareTo(low) <= 0) {
		bankroll = bankroll.add(stickFuture.getOpen().max(high).divide(TICK_SIZE, 2, RoundingMode.HALF_UP)
			.multiply(TICK_VALUE));
		bankroll = bankroll.subtract(COMMISSION);
		purchased = false;
		// System.out.println("Sell: " + stick.getKey() + " " +
		// stickFuture.getOpen().max(high) + " " + bankroll);
	    }
	}

	// refund last purchase
	if (purchased) {
	    bankroll = bankroll.add(lastBuyPrice.divide(TICK_SIZE, 2, RoundingMode.HALF_UP).multiply(TICK_VALUE));
	    bankroll = bankroll.add(COMMISSION);
	}

	return bankroll;
    }

    public BigDecimal analyze2() {
	BigDecimal bankroll = BigDecimal.ZERO;
	BigDecimal lastBuyPrice = BigDecimal.ZERO;
	boolean purchased = false;
	Collections.sort(candlestickList);

	for (int i = 14; i < candlestickList.size(); i++) {
	    Candlestick stick1 = candlestickList.get(i - 1);
	    Candlestick stick13 = candlestickList.get(i - 12 - 1);

	    Candlestick stick2 = candlestickList.get(i - 2);
	    Candlestick stick14 = candlestickList.get(i - 12 - 2);

	    Candlestick stick = candlestickList.get(i);

	    BigDecimal delta1 = stick1.getClose().subtract(stick13.getClose());
	    BigDecimal delta2 = stick2.getClose().subtract(stick14.getClose());
	    BigDecimal acceleration = delta1.subtract(delta2);

	    BigDecimal high = stick1.getHigh().add(SCALE);
	    BigDecimal low = stick1.getLow().add(SCALE);
	    if (!purchased && delta1.compareTo(BigDecimal.ZERO) == 1 && acceleration.compareTo(BigDecimal.ZERO) == 1
		    && stick.getHigh().compareTo(high) >= 0) {
		lastBuyPrice = stick.getOpen();
		bankroll = bankroll.subtract(tickValue(lastBuyPrice));
		purchased = true;
	    } else if (purchased && delta1.compareTo(BigDecimal.ZERO) == 1
		    && acceleration.compareTo(BigDecimal.ZERO) == 1 && stick.getLow().compareTo(low) <= 0) {
		bankroll = bankroll.add(tickValue(stick.getOpen()));
		purchased = false;
	    }
	}

	// refund last purchase
	if (purchased) {
	    bankroll = bankroll.add(tickValue(lastBuyPrice));
	}

	return bankroll;
    }

    public BigDecimal analyze3() {
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
	Collections.sort(candlestickList);

	for (int i = 4; i < candlestickList.size() - 1; i++) {
	    Candlestick stick = candlestickList.get(i);
	    Candlestick stick3 = candlestickList.get(i - 3);

	    Candlestick stick1 = candlestickList.get(i - 1);
	    Candlestick stick4 = candlestickList.get(i - 3 - 1);

	    Candlestick futureStick = candlestickList.get(i + 1);

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
	    } else if (purchased && delta.abs().compareTo(deltaST) == 1
		    && acceleration.abs().compareTo(accelerationST) == 1 && stick.getLow().compareTo(low) <= 0) {
		bankroll = bankroll.add(tickValue(stick.getOpen()));
		purchased = false;
	    }
	}

	// refund
	if (purchased) {
	    bankroll = bankroll.add(tickValue(lastBuyPrice));
	}

	return bankroll;
    }

    private BigDecimal tickValue(BigDecimal price) {
	return price.divide(TICK_SIZE, 0, RoundingMode.HALF_UP).multiply(TICK_VALUE);
    }

    public static void main(String[] args) {
	String path = "C:/Users/Matcha Green/Desktop/candlestick";

	File dir = new File(path);
	for (File file : dir.listFiles()) {
	    String symbol = StringUtils.substringBefore(file.getName(), Character.toString('_'));

	    // if (symbol.equals("ESZ16")) {
	    CandlestickParser parser = new CandlestickParser();
	    parser.parse(file);
	    System.out.println(symbol + " Bankroll: $" + parser.analyze3());
	    // }
	}
    }
}
