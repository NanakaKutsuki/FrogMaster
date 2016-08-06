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

import org.apache.commons.lang3.text.StrBuilder;
import org.kutsuki.frogmaster.model.OutputModel;
import org.kutsuki.frogmaster.model.ProfileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlOutputter extends AbstractOutputter {
    private final Logger logger = LoggerFactory.getLogger(HtmlOutputter.class);

    private List<LocalDate> dateList;
    private Map<BigDecimal, Map<LocalDate, OutputModel>> outputMap;

    public HtmlOutputter(Map<LocalDate, ProfileModel> profileMap) {
        super(profileMap);

        this.outputMap = new TreeMap<BigDecimal, Map<LocalDate, OutputModel>>(Collections.reverseOrder());
    }

    @Override
    public void output() {
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(new File("MarketMap.html"));
            bw = new BufferedWriter(fw);

            writeHeader(bw);
            createMap();

            for (Entry<BigDecimal, Map<LocalDate, OutputModel>> entry : outputMap.entrySet()) {
                BigDecimal price = entry.getKey();
                Map<LocalDate, OutputModel> rowMap = entry.getValue();

                bw.write("<tr>");
                bw.newLine();

                for (LocalDate date : dateList) {
                    OutputModel output = rowMap.get(date);
                    writeRow(bw, price, output);
                }

                bw.newLine();
                bw.write("<td>" + format(price) + "</td>");
                bw.newLine();
                bw.write("</tr>");
                bw.newLine();

            }

            bw.write("<tr>");
            bw.newLine();

            for (LocalDate date : dateList) {
                bw.write("<td>" + date.getMonthValue() + "/" + date.getDayOfMonth() + "</td>");
            }
            bw.newLine();
            bw.write("</tr>");
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

                StrBuilder sb = new StrBuilder();
                for (char c : entry2.getValue()) {
                    sb.append(c);
                }

                OutputModel output = new OutputModel();
                output.setOutput(sb.toString());

                if (price.compareTo(profile.getHighValuePrice()) <= 0
                        && price.compareTo(profile.getLowValuePrice()) >= 0) {
                    output.setValue(true);
                } else {
                    output.setValue(false);
                }

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
        bw.write("<title>Market Map - ESU13</title>");
        bw.newLine();
        bw.write("</head>");
        bw.newLine();
        bw.write("<body>");
        bw.newLine();
        bw.write("<table cellspacing=\"0\">");
        bw.newLine();
    }

    private void writeRow(BufferedWriter bw, BigDecimal price, OutputModel output) throws IOException {
        if (output != null) {
            if (output.isValue()) {
                bw.write("<td bgcolor=\"#417DD4\">" + output.getOutput() + "</td>");
            } else {
                bw.write("<td>" + output.getOutput() + "</td>");
            }
        } else {
            bw.write("<td></td>");
        }
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
