package org.kutsuki.frogmaster2.analytics;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.AbstractParser;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;

public class ESParser2 extends AbstractParser {
	private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES30.txt";

	@Override
	public File getFile(Ticker ticker) {
		return new File(FILE_NAME);
	}

	public void run() {
		File file = getFile(null);
		TreeMap<LocalDateTime, Bar> barMap = load(file);

		if (file.exists()) {
			int day = 0;
			int poc = 0;
			int val = 0;
			int vah = 0;
			int lastPoc = 0;
			int lastVah = 0;
			int lastVal = 0;
			int open = 0;
			LocalTime start = LocalTime.of(9, 30);
			LocalTime end = LocalTime.of(16, 31);
			LocalTime end2 = LocalTime.of(18, 30);
			TreeMap<Integer, Integer> marketProfile = new TreeMap<Integer, Integer>();

			int inside = 0;
			int outside = 0;
			int insideTotal = 0;

			for (LocalDateTime key : barMap.keySet()) {
				LocalTime time = key.toLocalTime();

				if (time.equals(start)) {
					Bar bar = barMap.get(key);
					open = bar.getOpen();
					day = key.getDayOfYear();
					marketProfile.clear();
				}

				if (time.equals(end2) && day == key.getDayOfYear()) {
					Bar bar = barMap.get(key);

					if (lastVah != 0 && lastVal != 0) {
						if (open > lastVah) {
							insideTotal++;

							if (bar.getClose() > lastVah) {
								outside++;
							}
						}
					}

					lastPoc = poc;
					lastVah = vah;
					lastVal = val;
				}

				if (time.isAfter(start) && time.isBefore(end)) {
					Bar bar = barMap.get(key);

					for (int price = bar.getLow(); price <= bar.getHigh(); price += 25) {
						Integer tpo = marketProfile.get(price);

						if (tpo == null) {
							tpo = 0;
						}

						marketProfile.put(price, tpo + 1);
					}

					double medianD = (double) (marketProfile.firstKey() + marketProfile.lastKey()) / 2;
					if (medianD % 1 != 0) {
						medianD = medianD + 12.5;
					}
					int median = (int) medianD;

					int length = 0;
					int total = 0;
					for (Entry<Integer, Integer> e : marketProfile.entrySet()) {
						if (e.getValue() > length) {
							length = e.getValue();
						}

						total += e.getValue();
					}

					int moveUp = 25;
					int moveDown = 25;
					poc = 0;
					if (marketProfile.get(median) == length) {
						poc = median;
					} else {
						while (poc == 0) {
							if (marketProfile.get(median + moveUp) == length) {
								poc = median + moveUp;
							} else if (marketProfile.get(median - moveDown) == length) {
								poc = median - moveDown;
							}

							moveUp += 25;
							moveDown += 25;
						}
					}

					length = marketProfile.get(poc);
					int goal = (int) (total * .7);
					moveDown = 25;
					moveUp = 25;

					while (length < goal) {
						int i = 0;
						if (poc + moveUp + 25 <= marketProfile.lastKey()) {
							i = marketProfile.get(poc + moveUp) + marketProfile.get(poc + moveUp + 25);
						}

						int j = 0;
						if (poc - moveDown - 25 >= marketProfile.firstKey()) {
							j = marketProfile.get(poc - moveDown) + marketProfile.get(poc - moveDown - 25);
						}

						if (i >= j) {
							length = length + marketProfile.get(poc + moveUp);
							vah = poc + moveUp;
							moveUp += 25;

							if (length < goal) {
								length = length + marketProfile.get(poc + moveUp);
								vah = poc + moveUp;
								moveUp += 25;
							}
						} else {
							length = length + marketProfile.get(poc - moveDown);
							val = poc - moveDown;
							moveDown += 25;

							if (length < goal) {
								length = length + marketProfile.get(poc - moveDown);
								val = poc - moveDown;
								moveDown += 25;
							}
						}
					}
				}
			}
			System.out.println(inside);
			System.out.println(outside);
			System.out.println(insideTotal);
		}
	}

	public static void main(String[] args) {
		ESParser2 parser = new ESParser2();
		parser.run();
	}
}
