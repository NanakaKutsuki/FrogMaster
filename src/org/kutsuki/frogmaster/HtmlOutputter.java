package org.kutsuki.frogmaster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.StrBuilder;
import org.kutsuki.frogmaster.model.ProfileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlOutputter {
    private final Logger logger = LoggerFactory.getLogger(HtmlOutputter.class);

    private static final NumberFormat NF = NumberFormat.getCurrencyInstance();

    private ProfileModel profile;

    public HtmlOutputter(ProfileModel profile) {
        this.profile = profile;
    }

    public void output() {
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(new File("MarketMap.html"));
            bw = new BufferedWriter(fw);

            writeHeader(bw);

            for (Entry<BigDecimal, List<Character>> entry : profile.getLetterMap().entrySet()) {
                writeRow(entry.getValue(), entry.getKey(), bw);
            }

            bw.write("<tr>");
            bw.newLine();
            bw.write("<td>9/3</td>");
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
        bw.write("<table border=\"0\">");
        bw.newLine();
    }

    private void writeRow(List<Character> letterList, BigDecimal price, BufferedWriter bw) throws IOException {
        StrBuilder sb = new StrBuilder();
        for (char c : letterList) {
            sb.append(c);
        }

        bw.write("<tr>");
        bw.newLine();

        if (price.compareTo(profile.getHighValuePrice()) <= 0 && price.compareTo(profile.getLowValuePrice()) >= 0) {
            bw.write("<td bgcolor=\"#417DD4\">" + sb.toString() + "</td>");
            bw.newLine();
        } else {
            bw.write("<td>" + sb.toString() + "</td>");
            bw.newLine();
        }

        bw.write("<td>" + NF.format(price) + "</td>");
        bw.newLine();
        bw.write("</tr>");
        bw.newLine();
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
