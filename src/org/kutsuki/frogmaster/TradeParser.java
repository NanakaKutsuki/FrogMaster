package org.kutsuki.frogmaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.model.ProfileModel;
import org.kutsuki.frogmaster.model.TpoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeParser {
    private final Logger logger = LoggerFactory.getLogger(TradeParser.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final LocalDate LABOR_DAY = LocalDate.of(2013, 9, 2);
    private static final LocalTime NINE = LocalTime.of(9, 30);
    private static final LocalTime FOUR = LocalTime.of(16, 15);

    private Map<String, Map<LocalDate, ProfileModel>> profileMap;

    public TradeParser() {
        this.profileMap = new HashMap<String, Map<LocalDate, ProfileModel>>();
    }

    public void parse() {
        BufferedReader br = null;
        FileReader fr = null;

        try {
            // TODO remove hard coded path
            fr = new FileReader(new File("src/resources/ES_Trades.csv"));
            br = new BufferedReader(fr);

            // skip first line
            String line = br.readLine();

            // parse data
            while ((line = br.readLine()) != null) {
                String[] split = StringUtils.split(line, ',');

                if (split.length > 5) {
                    // parse symbol
                    String symbol = split[0];

                    // parse time
                    char letter = '#';
                    LocalTime time = null;
                    try {
                        time = LocalTime.parse(split[2], TIME_FORMATTER);

                        // ignore after hours
                        if (time.isBefore(NINE) || time.isAfter(FOUR)) {
                            continue;
                        }

                        letter = parseLetter(time);
                    } catch (DateTimeParseException e) {
                        logger.error("Failed to parse Time: " + split[2] + " from: " + line, e);
                    }

                    // parse date
                    LocalDate date = null;
                    try {
                        date = LocalDate.parse(split[1], DATE_FORMATTER);

                        // ignore after hours
                        if (date.getDayOfWeek().getValue() >= 6 || date.isEqual(LABOR_DAY)) {
                            continue;
                        }
                    } catch (DateTimeParseException e) {
                        logger.error("Failed to parse Date: " + split[1] + " from: " + line, e);
                    }

                    // parse price
                    BigDecimal price = null;
                    try {
                        price = new BigDecimal(split[3]);
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse Price: " + split[3] + " from: " + line, e);
                    }

                    // parse volume
                    int volume = 0;
                    try {
                        volume = Integer.parseInt(split[4]);
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse Volume: " + split[4] + " from: " + line, e);
                    }

                    // create TPO
                    if (StringUtils.isNotEmpty(symbol) && date != null && time != null && price != null && volume > 0) {
                        TpoModel tpo = new TpoModel();
                        tpo.setDate(date);
                        tpo.setTime(time);
                        tpo.setLetter(letter);
                        tpo.setPrice(price);
                        tpo.setSymbol(symbol);
                        tpo.setVolume(volume);
                        addTpo(tpo);
                    }
                } else {
                    logger.error("Bad Line: " + line);
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading from File.", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Error while closing Buffered Reader!", e);
                    }
                }
            }

            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Error while closing File Reader!", e);
                    }
                }
            }
        }
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
            profile = new ProfileModel(tpo.getSymbol(), tpo.getDate());
        }

        // add tpo to profile
        profile.addTpo(tpo);

        // add profile to date map
        dateProfileMap.put(tpo.getDate(), profile);

        // add date map to profile map
        profileMap.put(tpo.getSymbol(), dateProfileMap);
    }
}
