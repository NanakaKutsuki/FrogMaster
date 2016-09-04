package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrBuilder;
import org.kutsuki.frogmaster.model.ProfileModel;
import org.kutsuki.frogmaster.model.TpoModel;

public class Analytics {
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M/d");
	private static final NumberFormat PERCENT = NumberFormat.getPercentInstance();

	private List<LocalDate> dateList;
	private Map<LocalDate, ProfileModel> profileMap;

	public Analytics(Map<LocalDate, ProfileModel> profileMap) {
		this.profileMap = profileMap;
		this.dateList = new ArrayList<LocalDate>(profileMap.keySet());
		Collections.sort(dateList);
	}

	public void run() {
		outsideOpenBackToValue();
		pocFrequency();
		openFrequency();
		openType();
		trendLines();
		movingAverage();
	}

	private void outsideOpenBackToValue() {
		int total = 0;
		int valueCount = 0;
		int valueHighCount = 0;
		List<String> outsideOpenList = new ArrayList<String>();

		for (LocalDate date : dateList) {
			ProfileModel profile = profileMap.get(date);

			// opens below value
			if (profile.getOpenPrice().compareTo(profile.getLowValuePrice()) == -1) {
				boolean value = false;
				boolean valueHigh = false;
				Iterator<TpoModel> itr = profile.getTpoList().iterator();
				while ((!value || !valueHigh) && itr.hasNext()) {
					TpoModel tpo = itr.next();

					if (tpo.getPrice().compareTo(profile.getLowValuePrice()) >= 0) {
						value = true;
					}

					if (tpo.getPrice().compareTo(profile.getHighValuePrice()) == 1) {
						valueHigh = true;
					}
				}

				if (value) {
					valueCount++;
				}

				if (valueHigh) {
					valueHighCount++;
				}

				outsideOpenList.add(DTF.format(date));
				total++;
			}
		}

		System.out.println("Dates with Low Outside Open: " + outsideOpenList);
		System.out.println("Low Outside Open goes to Value: " + PERCENT
				.format(BigDecimal.valueOf(valueCount).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)));
		System.out.println("Low Outside Open passes Value High: " + PERCENT
				.format(BigDecimal.valueOf(valueHighCount).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)));
		System.out.println("----------------------------------------------------");
	}

	private void pocFrequency() {
		List<String> resultList = new ArrayList<String>();

		for (LocalDate date : dateList) {
			ProfileModel profile = profileMap.get(date);

			List<TpoModel> tpoList = profile.getTpoList();
			Collections.sort(tpoList, new Comparator<TpoModel>() {
				@Override
				public int compare(TpoModel lhs, TpoModel rhs) {
					return lhs.getTime().compareTo(rhs.getTime());
				}
			});

			int count = 0;
			boolean poc = false;
			for (TpoModel tpo : tpoList) {
				if (tpo.getPrice().compareTo(profile.getPocPrice()) == 0) {
					if (!poc) {
						count++;
					}

					poc = true;
				} else {
					poc = false;
				}
			}

			StrBuilder sb = new StrBuilder();
			sb.append(DTF.format(date));
			sb.append(':');
			sb.append(' ');
			sb.append(count);
			resultList.add(sb.toString());
		}

		System.out.println("How many times do we go back to POC: " + resultList);
		System.out.println("----------------------------------------------------");
	}

	private void openFrequency() {
		List<String> resultList = new ArrayList<String>();

		for (LocalDate date : dateList) {
			ProfileModel profile = profileMap.get(date);

			List<TpoModel> tpoList = profile.getTpoList();
			Collections.sort(tpoList, new Comparator<TpoModel>() {
				@Override
				public int compare(TpoModel lhs, TpoModel rhs) {
					return lhs.getTime().compareTo(rhs.getTime());
				}
			});

			int count = 0;
			boolean open = false;
			for (TpoModel tpo : tpoList) {
				if (tpo.getPrice().compareTo(profile.getOpenPrice()) == 1) {
					if (!open) {
						count++;
					}

					open = true;
				} else if (tpo.getPrice().compareTo(profile.getOpenPrice()) == -1) {
					open = false;
				}
			}

			StrBuilder sb = new StrBuilder();
			sb.append(DTF.format(date));
			sb.append(':');
			sb.append(' ');
			sb.append(count);
			resultList.add(sb.toString());
		}

		System.out.println("How many times do we go through the Open: " + resultList);
		System.out.println("----------------------------------------------------");
	}

	public void openType() {
		List<String> resultList = new ArrayList<String>();

		for (int i = 0; i < dateList.size(); i++) {
			if (i - 1 > -1) {
				ProfileModel today = profileMap.get(dateList.get(i));
				ProfileModel yesterday = profileMap.get(dateList.get(i - 1));

				StrBuilder sb = new StrBuilder();
				sb.append(DTF.format(dateList.get(i)));
				sb.append(':');
				sb.append(' ');

				if (today.getOpenPrice().compareTo(yesterday.getHighValuePrice()) <= 0
						&& today.getOpenPrice().compareTo(yesterday.getLowValuePrice()) >= 0) {
					sb.append("Inside");
				} else if (today.getOpenPrice().compareTo(yesterday.getHighPrice()) <= 0
						&& today.getOpenPrice().compareTo(yesterday.getLowPrice()) >= 0) {
					sb.append("Outside");
				} else {
					sb.append("Gap");
				}

				resultList.add(sb.toString());
			}
		}

		System.out.println("Open Type: " + resultList);
		System.out.println("----------------------------------------------------");
	}

	public void trendLines() {
		for (LocalDate date : dateList) {
			ProfileModel profile = profileMap.get(date);

			Map<Character, BigDecimal> lowMap = new HashMap<Character, BigDecimal>();
			Map<Character, BigDecimal> highMap = new HashMap<Character, BigDecimal>();

			List<TpoModel> tpoList = profile.getTpoList();
			Collections.sort(tpoList, new Comparator<TpoModel>() {
				@Override
				public int compare(TpoModel lhs, TpoModel rhs) {
					return lhs.getTime().compareTo(rhs.getTime());
				}
			});

			for (TpoModel tpo : tpoList) {
				char key = tpo.getLetter();
				BigDecimal lowPrice = lowMap.get(key);
				BigDecimal highPrice = lowMap.get(key);

				if (lowPrice == null || tpo.getPrice().compareTo(lowPrice) == -1) {
					lowMap.put(tpo.getLetter(), tpo.getPrice());
				}

				if (highPrice == null || tpo.getPrice().compareTo(highPrice) == 1) {
					highMap.put(tpo.getLetter(), tpo.getPrice());
				}
			}

			List<Character> keyList = new ArrayList<Character>(lowMap.keySet());
			Collections.sort(keyList);

			List<Character> downList = new ArrayList<Character>();
			List<Character> upList = new ArrayList<Character>();
			for (int i = 1; i < keyList.size(); i++) {
				BigDecimal currentLow = lowMap.get(keyList.get(i));
				BigDecimal previousLow = lowMap.get(keyList.get(i - 1));

				BigDecimal currentHigh = highMap.get(keyList.get(i));
				BigDecimal previousHigh = highMap.get(keyList.get(i - 1));

				if (currentLow.compareTo(previousLow) == 1) {
					upList.add(keyList.get(i));
				}

				if (currentHigh.compareTo(previousHigh) == -1) {
					downList.add(keyList.get(i));
				}
			}

			if (!downList.isEmpty()) {
				StrBuilder sb = new StrBuilder();
				sb.append(date);
				sb.append(' ');

				for (char c : downList) {
					sb.append(c);
				}

				sb.append(' ');
				sb.append(" momentum up!");
				System.out.println(sb.toString());
			}

			if (!upList.isEmpty()) {
				StrBuilder sb = new StrBuilder();
				sb.append(date);
				sb.append(' ');

				for (char c : upList) {
					sb.append(c);
				}

				sb.append(' ');
				sb.append(" momentum down!");
				System.out.println(sb.toString());
			}
		}

		System.out.println("----------------------------------------------------");
	}

	public void movingAverage() {
		for (LocalDate date : dateList) {
			ProfileModel profile = profileMap.get(date);

			List<BigDecimal> movingList = new ArrayList<BigDecimal>();

			List<TpoModel> tpoList = profile.getTpoList();
			Collections.sort(tpoList, new Comparator<TpoModel>() {
				@Override
				public int compare(TpoModel lhs, TpoModel rhs) {
					return lhs.getTime().compareTo(rhs.getTime());
				}
			});

			Map<Character, Integer> upMap = new HashMap<Character, Integer>();
			Map<Character, Integer> downMap = new HashMap<Character, Integer>();
			Map<Character, Integer> pushMap = new HashMap<Character, Integer>();

			for (TpoModel tpo : tpoList) {
				if (movingList.size() < 50) {
					movingList.add(tpo.getPrice());
				} else {
					BigDecimal sum = BigDecimal.ZERO;
					for (BigDecimal price : movingList) {
						sum = sum.add(price);
					}

					BigDecimal average = sum.divide(BigDecimal.valueOf(movingList.size()), 2, RoundingMode.HALF_UP);

					if (tpo.getPrice().compareTo(average) == 1) {
						Integer up = upMap.get(tpo.getLetter());

						if (up == null) {
							up = 0;
						}

						upMap.put(tpo.getLetter(), up + 1);
					} else if (tpo.getPrice().compareTo(average) == -1) {
						Integer down = downMap.get(tpo.getLetter());

						if (down == null) {
							down = 0;
						}

						downMap.put(tpo.getLetter(), down + 1);
					} else {
						Integer push = pushMap.get(tpo.getLetter());

						if (push == null) {
							push = 0;
						}

						pushMap.put(tpo.getLetter(), push + 1);
					}

					movingList.remove(0);
					movingList.add(tpo.getPrice());
				}
			}

			List<Character> keyList = new ArrayList<Character>(upMap.keySet());
			Collections.sort(keyList);

			List<Character> downList = new ArrayList<Character>();
			List<Character> upList = new ArrayList<Character>();

			BigDecimal offset = new BigDecimal(0.05);
			for (Character key : keyList) {
				BigDecimal up = new BigDecimal(upMap.get(key));
				BigDecimal down = new BigDecimal(downMap.get(key));
				BigDecimal push = new BigDecimal(pushMap.get(key));

				BigDecimal total = up.add(down).add(push);

				BigDecimal upPercent = up.divide(total, 2, RoundingMode.HALF_UP);
				BigDecimal downPercent = down.divide(total, 2, RoundingMode.HALF_UP);

				if (upPercent.compareTo(downPercent.add(offset)) == 1) {
					upList.add(key);
				} else if (downPercent.compareTo(upPercent.add(offset)) == 1) {
					downList.add(key);
				}
			}

			if (!downList.isEmpty()) {
				StrBuilder sb = new StrBuilder();
				sb.append(date);
				sb.append(' ');

				for (char c : downList) {
					sb.append(c);
				}

				sb.append(' ');
				sb.append(" momentum up!");
				System.out.println(sb.toString());
			}

			if (!upList.isEmpty()) {
				StrBuilder sb = new StrBuilder();
				sb.append(date);
				sb.append(' ');

				for (char c : upList) {
					sb.append(c);
				}

				sb.append(' ');
				sb.append(" momentum down!");
				System.out.println(sb.toString());
			}
		}
	}
}
