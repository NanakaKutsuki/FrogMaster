package org.kutsuki.frogmaster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class TradestationParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final LocalDate TARGET_DATE = LocalDate.of(2017, 8, 17);
    private static final LocalTime START_TIME = LocalTime.of(9, 14);
    private static final LocalTime END_TIME = LocalTime.of(16, 15);
    private static final String FILE = "C:/Users/Matcha Green/Desktop/august17.txt";
    private static final String HEADER = "price,duration,count";
    private static final String OUTPUT_FILE = "all.csv";

    public void run() {
	try (BufferedReader br = new BufferedReader(new FileReader(new File(FILE)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(OUTPUT_FILE)))) {
	    System.out.println(HEADER);
	    bw.write(HEADER);
	    bw.newLine();

	    // skip first line
	    br.readLine();

	    String line = null;
	    TreeMap<BigDecimal, FrogData> frogMap = new TreeMap<BigDecimal, FrogData>();
	    while ((line = br.readLine()) != null) {
		String[] split = StringUtils.split(line, ',');

		try {
		    LocalDate date = LocalDate.parse(split[0], DATE_FORMAT);
		    LocalTime time = LocalTime.parse(split[1], TIME_FORMAT);
		    // BigDecimal open = new BigDecimal(split[2]);
		    // BigDecimal high = new BigDecimal(split[3]);
		    // BigDecimal low = new BigDecimal(split[4]);
		    BigDecimal close = new BigDecimal(split[5]);
		    // int up = Integer.parseInt(split[6]);
		    // int down = Integer.parseInt(split[7]);

		    if (date.isEqual(TARGET_DATE) && time.isAfter(START_TIME) && time.isBefore(END_TIME)) {
			FrogData data = frogMap.get(close);

			if (data == null) {
			    data = new FrogData(close, time);
			}

			data.addTime(time);
			frogMap.put(close, data);
		    }
		} catch (DateTimeParseException | NumberFormatException e) {
		    e.printStackTrace();
		}
	    }

	    for (FrogData data : frogMap.values()) {
		System.out.println(data);
		bw.write(data.toString());
		bw.newLine();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	TradestationParser parser = new TradestationParser();
	parser.run();
    }
}
