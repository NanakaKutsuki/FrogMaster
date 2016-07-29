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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeParser {
    private final Logger logger = LoggerFactory.getLogger(TradeParser.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // TODO remove temporary variables
    private static final LocalDate THIRD = LocalDate.of(2013, 9, 3);
    private static final LocalTime NINE = LocalTime.of(9, 30);
    private static final LocalTime FOUR = LocalTime.of(16, 15);

    public Set<PricePoint> parse() {
        Set<PricePoint> priceSet = new HashSet<PricePoint>();
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

                    // parse date
                    LocalDate date = null;
                    try {
                        date = LocalDate.parse(split[1], DATE_FORMATTER);

                        // TODO remove
                        if (!date.isEqual(THIRD)) {
                            continue;
                        }
                    } catch (DateTimeParseException e) {
                        logger.error("Failed to parse Date: " + split[1] + " from: " + line, e);
                    }

                    // parse time
                    LocalTime time = null;
                    char letter = '#';
                    try {
                        time = LocalTime.parse(split[2], TIME_FORMATTER);

                        // TODO remove DO NOT COPY
                        if (time.isBefore(NINE) || time.isAfter(FOUR)) {
                            continue;
                        }

                        letter = parseLetter(time);
                    } catch (DateTimeParseException e) {
                        logger.error("Failed to parse Time: " + split[2] + " from: " + line, e);
                    }

                    // parse price
                    BigDecimal price = null;
                    try {
                        price = new BigDecimal(split[3]);
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse Price: " + split[3] + " from: " + line, e);
                    }

                    // parse volume
                    // TODO decide if volume is needed
                    // int volume = -1;
                    // try {
                    // volume = Integer.parseInt(split[4]);
                    // } catch (NumberFormatException e) {
                    // logger.error("Failed to parse Volume: " + split[4] +
                    // " from: " + line, e);
                    // }

                    if (StringUtils.isNotEmpty(symbol) && date != null && time != null && price != null) {
                        PricePoint point = new PricePoint();
                        point.setDate(date);
                        point.setLetter(letter);
                        point.setPrice(price);
                        point.setSymbol(symbol);
                        point.setTime(time);
                        priceSet.add(point);
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

        return priceSet;
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
}
