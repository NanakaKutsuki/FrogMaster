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
import org.kutsuki.frogmaster.strategy.ShortStrategy;

public class TradestationParser {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	private static final File DIR = new File("C:/Users/jleung/Desktop/ES");

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

		LongStrategy ls = new LongStrategy(barMap);
		ls.run();

		ShortStrategy ss = new ShortStrategy(barMap, input);
		ss.run();

		BigDecimal min = new BigDecimal(10000);
		LocalDateTime minDateTime = null;
		for (LocalDateTime key : ls.getEquityMap().keySet()) {
			Equity le = ls.getEquityMap().get(key);
			Equity se = ss.getEquityMap().get(key);

			BigDecimal total = le.getUnrealized().add(le.getRealized()).add(se.getUnrealized()).add(se.getRealized());

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

	public static void main(String[] args) {
		TradestationParser parser = new TradestationParser();

		for (File file : DIR.listFiles()) {
			Input input = Inputs.getInput(file.getName());
			parser.run(file, input);
		}

		// File file = new File(DIR + "/ESU15.txt");
		// Input input = Inputs.getInput(file.getName());
		// parser.run(file, input);

		for (Entry<String, String> entry : parser.getResultMap().entrySet()) {
			System.out.println(entry.getKey() + StringUtils.SPACE + entry.getValue());
		}
	}
}
