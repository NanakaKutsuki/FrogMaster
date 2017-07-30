package org.kutsuki.frogmaster.outputter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.kutsuki.frogmaster.model.OutputModel;
import org.kutsuki.frogmaster.model.ProfileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlOutputter extends AbstractOutputter {
    private final Logger logger = LoggerFactory.getLogger(HtmlOutputter.class);

    private static final String BLACK = "#000000";
    private static final String BLUE = "#3399ff";
    private static final String ORANGE = "#ff9900";
    private static final String WHITE = "#ffffff";

    private static final String END = "\">";
    private static final String END_TD = "</td>";
    private static final String END_TR = "</tr>";
    private static final String HTML = ".html";
    private static final String TD = "<td>";
    private static final String TD_BGCOLOR = "<td bgcolor=\"";
    private static final String TR = "<tr>";
    private static final String TITLE = "\" title=\"";

    private List<LocalDate> dateList;
    private Map<BigDecimal, Map<LocalDate, OutputModel>> outputMap;
    private String filename;
    private String symbol;

    public HtmlOutputter(Map<LocalDate, ProfileModel> profileMap, String symbol, String filename) {
	super(profileMap);

	this.filename = StringUtils.substringBeforeLast(filename, Character.toString('_'));
	this.outputMap = new TreeMap<BigDecimal, Map<LocalDate, OutputModel>>(Collections.reverseOrder());
	this.symbol = symbol;
    }

    @Override
    public void output() {
	FileWriter fw = null;
	BufferedWriter bw = null;

	try {
	    File dir = new File(symbol);
	    if (!dir.exists()) {
		dir.mkdirs();
	    }

	    File file = new File(symbol + Character.toString('/') + symbol + Character.toString('_') + filename + HTML);
	    fw = new FileWriter(file);
	    bw = new BufferedWriter(fw);

	    writeHeader(bw);
	    createMap();

	    for (Entry<BigDecimal, Map<LocalDate, OutputModel>> entry : outputMap.entrySet()) {
		BigDecimal price = entry.getKey();
		Map<LocalDate, OutputModel> rowMap = entry.getValue();

		bw.write(TR);
		bw.newLine();

		for (LocalDate date : dateList) {
		    OutputModel output = rowMap.get(date);
		    writeRow(bw, price, output);
		}

		bw.newLine();
		bw.write(TD + format(price) + END_TD);
		bw.newLine();
		bw.write(END_TR);
		bw.newLine();

	    }

	    bw.write(TR);
	    bw.newLine();

	    for (LocalDate date : dateList) {
		bw.write(TD + date.getMonthValue() + Character.toString('/') + date.getDayOfMonth() + END_TD);
	    }
	    bw.newLine();
	    bw.write(END_TR);
	    bw.newLine();

	    writeFooter(bw);
	} catch (IOException e) {
	    logger.error("Error while writing to File.", e);
	} finally {
	    if (bw != null) {
		try {
		    bw.close();
		} catch (IOException e) {
		    if (logger.isDebugEnabled()) {
			logger.debug("Error while closing Buffered Reader!", e);
		    }
		}
	    }

	    if (fw != null) {
		try {
		    fw.close();
		} catch (IOException e) {
		    if (logger.isDebugEnabled()) {
			logger.debug("Error while closing File Reader!", e);
		    }
		}
	    }
	}
    }

    private void createMap() {
	Set<LocalDate> dateSet = new HashSet<LocalDate>();

	for (Entry<LocalDate, ProfileModel> entry : getProfileMap().entrySet()) {
	    LocalDate date = entry.getKey();
	    ProfileModel profile = entry.getValue();

	    dateSet.add(date);

	    for (Entry<BigDecimal, List<Character>> entry2 : profile.getLetterMap().entrySet()) {
		BigDecimal price = entry2.getKey();

		Map<LocalDate, OutputModel> rowMap = outputMap.get(price);

		if (rowMap == null) {
		    rowMap = new HashMap<LocalDate, OutputModel>();
		}

		OutputModel output = new OutputModel();

		if (price.compareTo(profile.getClosePrice()) == 0) {
		    output.setClose(true);
		}

		if ((profile.isPoorHigh() && price.compareTo(profile.getHighPrice()) == 0)
			|| (profile.isPoorLow() && price.compareTo(profile.getLowPrice()) == 0)) {
		    output.setPoor(true);
		}

		if (price.compareTo(profile.getHighValuePrice()) <= 0
			&& price.compareTo(profile.getLowValuePrice()) >= 0) {
		    output.setValue(true);
		}

		StrBuilder sb = new StrBuilder();
		for (int i = 0; i < entry2.getValue().size(); i++) {
		    char c = entry2.getValue().get(i);

		    if (i == 0) {
			if (output.isClose()) {
			    if (output.isValue()) {
				sb.append("<font color=\"" + BLUE + "\">");
			    } else if (output.isPoor()) {
				sb.append("<font color=\"" + ORANGE + "\">");
			    } else {
				sb.append("<font color=\"#ffffff\">");
			    }
			}

			if (price.compareTo(profile.getOpenPrice()) == 0) {
			    sb.append("<font color=\"#ff5050\">");
			    sb.append(c);
			    sb.append("</font>");
			} else {
			    if (output.isPoor() && output.isValue()) {
				sb.append("<font color=\"" + BLUE + "\">");
			    }

			    sb.append(c);
			}
		    } else {
			sb.append(c);
		    }

		    if (i == entry2.getValue().size() - 1
			    && (output.isClose() || (output.isPoor() && output.isValue()))) {
			sb.append("</font>");
		    }
		}
		output.setOutput(sb.toString());

		rowMap.put(date, output);
		outputMap.put(price, rowMap);
	    }
	}

	dateList = new ArrayList<LocalDate>(dateSet);
	Collections.sort(dateList);
    }

    private void writeHeader(BufferedWriter bw) throws IOException {
	bw.write("<!DOCTYPE html>");
	bw.newLine();
	bw.write("<html>");
	bw.newLine();
	bw.write("<head>");
	bw.newLine();
	bw.write("<title>" + symbol + Character.toString('_') + filename + "</title>");
	bw.newLine();
	bw.write("</head>");
	bw.newLine();
	bw.write("<body>");
	bw.newLine();
	bw.write("<table cellspacing=\"0\">");
	bw.newLine();
    }

    private void writeRow(BufferedWriter bw, BigDecimal price, OutputModel output) throws IOException {
	StrBuilder sb = new StrBuilder();

	if (output != null) {
	    sb.append(TD_BGCOLOR);

	    if (output.isPoor()) {
		sb.append(ORANGE);
	    } else if (output.isClose()) {
		sb.append(BLACK);
	    } else if (output.isValue()) {
		sb.append(BLUE);
	    } else {
		sb.append(WHITE);
	    }

	    sb.append(TITLE);
	    sb.append(format(price));
	    sb.append(END);
	    sb.append(output.getOutput());
	    sb.append(END_TD);
	} else {
	    sb.append(TD);
	    sb.append(END_TD);
	}

	bw.write(sb.toString());
    }

    private void writeFooter(BufferedWriter bw) throws IOException {
	bw.write("</table>");
	bw.newLine();
	bw.write("</body>");
	bw.newLine();
	bw.write("</html>");
	bw.newLine();
    }
}
