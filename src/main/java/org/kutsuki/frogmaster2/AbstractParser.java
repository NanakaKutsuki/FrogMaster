package org.kutsuki.frogmaster2;

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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;

public abstract class AbstractParser {
    private static final boolean PRICE_OSCILLATOR = false;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public abstract File getFile(Symbol symbol);

    private Ticker ticker;

    public BarMap load(File file) {
	BarMap barMap = null;

	if (file.exists()) {
	    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		List<LocalDateTime> dateList = new ArrayList<LocalDateTime>();
		List<Bar> barList = new ArrayList<Bar>();

		// skip first line
		br.readLine();

		String line = null;
		while ((line = br.readLine()) != null) {
		    String[] split = StringUtils.split(line, ',');

		    try {
			LocalDate date = LocalDate.parse(split[0], DATE_FORMAT);
			LocalTime time = LocalTime.parse(split[1], TIME_FORMAT);
			int open = Integer.parseInt(StringUtils.remove(split[2], '.'));
			int high = Integer.parseInt(StringUtils.remove(split[3], '.'));
			int low = Integer.parseInt(StringUtils.remove(split[4], '.'));
			int close = Integer.parseInt(StringUtils.remove(split[5], '.'));
			// int up = Integer.parseInt(split[6]);
			// int down = Integer.parseInt(split[7]);

			Bar bar = new Bar(LocalDateTime.of(date, time), open, high, low, close);
			// bar.setUpTicks(up);
			// bar.setDownTicks(down);

			barList.add(bar);
			dateList.add(bar.getDateTime());

		    } catch (DateTimeParseException | NumberFormatException e) {
			e.printStackTrace();
		    }
		}

		barMap = new BarMap(dateList, barList, PRICE_OSCILLATOR);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	return barMap;
    }

    public Ticker getTicker() {
	return ticker;
    }

    public void setTicker(String ticker) {
	if (StringUtils.contains(ticker, Ticker.ES.getTicker())) {
	    this.ticker = Ticker.ES;
	} else if (StringUtils.contains(ticker, Ticker.GC.getTicker())) {
	    this.ticker = Ticker.GC;
	}
    }

    public BigDecimal revertDollars(int i) {
	BigDecimal bd = new BigDecimal(i);
	bd = bd.divide(ticker.getDivisor(), 2, RoundingMode.HALF_UP);
	return bd;
    }
}
