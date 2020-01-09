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
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;

public abstract class AbstractParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public abstract File getFile(Symbol symbol);

    private Ticker ticker;

    public TreeMap<LocalDateTime, Bar> load(File file) {
	TreeMap<LocalDateTime, Bar> barMap = new TreeMap<LocalDateTime, Bar>();

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
			int open = Integer.parseInt(StringUtils.remove(split[2], '.'));
			int high = Integer.parseInt(StringUtils.remove(split[3], '.'));
			int low = Integer.parseInt(StringUtils.remove(split[4], '.'));
			int close = Integer.parseInt(StringUtils.remove(split[5], '.'));
			// int up = Integer.parseInt(split[6]);
			// int down = Integer.parseInt(split[7]);

			Bar bar = new Bar();
			bar.setDateTime(LocalDateTime.of(date, time));
			bar.setOpen(open);
			bar.setHigh(high);
			bar.setLow(low);
			bar.setClose(close);
			// bar.setUpTicks(up);
			// bar.setDownTicks(down);

			barMap.put(bar.getDateTime(), bar);
		    } catch (DateTimeParseException | NumberFormatException e) {
			e.printStackTrace();
		    }
		}
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
