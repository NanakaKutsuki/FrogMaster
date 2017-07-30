package org.kutsuki.frogmaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.model.ProfileModel;
import org.kutsuki.frogmaster.model.TpoModel;

public class TradeParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final LocalTime NINE = LocalTime.of(9, 30);
    private static final LocalTime FOUR = LocalTime.of(16, 15);

    private Map<String, Map<LocalDate, ProfileModel>> profileMap;

    public TradeParser() {
	this.profileMap = new HashMap<String, Map<LocalDate, ProfileModel>>();
    }

    public void parse(File gzFile, String symbol) {
	try (FileInputStream fis = new FileInputStream(gzFile);
		GZIPInputStream gis = new GZIPInputStream(fis);
		InputStreamReader isr = new InputStreamReader(gis);
		BufferedReader br = new BufferedReader(isr);) {
	    // parse data
	    String line = null;
	    while ((line = br.readLine()) != null) {
		String[] split = StringUtils.split(line, ',');

		if (split.length > 4) {
		    // parse time first
		    boolean afterHours = false;
		    LocalTime time = null;
		    try {
			time = LocalTime.parse(split[1], TIME_FORMATTER);

			// ignore after hours
			if (time.isBefore(NINE) || time.isAfter(FOUR)) {
			    afterHours = true;
			}

		    } catch (DateTimeParseException e) {
			e.printStackTrace();
		    }

		    // parse date
		    LocalDate date = null;
		    if (!afterHours) {
			try {
			    date = LocalDate.parse(split[0], DATE_FORMATTER);

			    // ignore after hours
			    if (date.getDayOfWeek().getValue() >= 6) {
				afterHours = true;
			    }
			} catch (DateTimeParseException e) {
			    e.printStackTrace();
			}
		    }

		    // check if after hours
		    if (!afterHours) {
			// parse letter
			char letter = parseLetter(time);

			// parse price
			BigDecimal price = null;
			try {
			    price = new BigDecimal(split[2]);
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}

			// parse volume
			int volume = 0;
			try {
			    volume = Integer.parseInt(split[3]);
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}

			// create TPO
			if (StringUtils.isNotEmpty(symbol) && date != null && time != null && price != null
				&& volume > 0) {
			    TpoModel tpo = new TpoModel();
			    tpo.setSymbol(symbol);
			    tpo.setDate(date);
			    tpo.setTime(time);
			    tpo.setLetter(letter);
			    tpo.setPrice(price);
			    tpo.setVolume(volume);
			    addTpo(tpo);
			}
		    }
		} else {
		    System.err.println("Bad Line: " + line);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public List<String> getSymbolList() {
	List<String> symbolList = new ArrayList<String>(profileMap.keySet());
	Collections.sort(symbolList);
	return symbolList;
    }

    public Map<LocalDate, ProfileModel> getProfileMapBySymbol(String symbol) {
	return profileMap.get(symbol);
    }

    public ProfileModel getProfile(String symbol, LocalDate date) {
	ProfileModel profile = null;

	Map<LocalDate, ProfileModel> dateMap = getProfileMapBySymbol(symbol);
	if (dateMap != null) {
	    profile = dateMap.get(date);
	}

	return profile;
    }

    private char parseLetter(LocalTime time) {
	char letter = '#';

	int hour = time.getHour();
	int minute = time.getMinute();

	switch (hour) {
	case 9:
	    if (minute >= 30) {
		letter = 'A';
	    }
	    break;
	case 10:
	    if (minute >= 0 && minute < 30) {
		letter = 'B';
	    } else {
		letter = 'C';
	    }
	    break;
	case 11:
	    if (minute >= 0 && minute < 30) {
		letter = 'D';
	    } else {
		letter = 'E';
	    }
	    break;
	case 12:
	    if (minute >= 0 && minute < 30) {
		letter = 'F';
	    } else {
		letter = 'G';
	    }
	    break;
	case 13:
	    if (minute >= 0 && minute < 30) {
		letter = 'H';
	    } else {
		letter = 'I';
	    }
	    break;
	case 14:
	    if (minute >= 0 && minute < 30) {
		letter = 'J';
	    } else {
		letter = 'K';
	    }
	    break;
	case 15:
	    if (minute >= 0 && minute < 30) {
		letter = 'L';
	    } else {
		letter = 'M';
	    }
	    break;
	case 16:
	    if (minute >= 0 && minute < 15) {
		letter = 'N';
	    }
	    break;
	default:
	    // ignore after hours
	    break;
	}

	return letter;
    }

    private void addTpo(TpoModel tpo) {
	// get by symbol
	Map<LocalDate, ProfileModel> dateProfileMap = profileMap.get(tpo.getSymbol());

	// create a new date map if none was found
	if (dateProfileMap == null) {
	    dateProfileMap = new TreeMap<LocalDate, ProfileModel>();
	}

	// get by date
	ProfileModel profile = dateProfileMap.get(tpo.getDate());

	// create a new profile if none was found
	if (profile == null) {
	    profile = new ProfileModel();
	}

	// add tpo to profile
	profile.addTpo(tpo);

	// add profile to date map
	dateProfileMap.put(tpo.getDate(), profile);

	// add date map to profile map
	profileMap.put(tpo.getSymbol(), dateProfileMap);
    }
}
