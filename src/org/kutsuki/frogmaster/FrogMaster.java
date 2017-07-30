package org.kutsuki.frogmaster;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster.outputter.AbstractOutputter;
import org.kutsuki.frogmaster.outputter.HtmlOutputter;

// https://github.com/NanakaKutsuki/FrogMaster.git
public class FrogMaster {
    private static final String DIR_PATH = "C:/TickData/TickWrite7/DATA/FUT/E/ES";
    private static final String GZ = ".gz";
    private static final String SUMMARY = "SUMMARY";

    public void run() {
	long ms = System.currentTimeMillis();

	File dir = new File(DIR_PATH);
	for (File file : dir.listFiles()) {
	    if (file.isDirectory()) {
		TradeParser parser = new TradeParser();

		for (File gzFile : file.listFiles()) {
		    if (!StringUtils.contains(gzFile.getName(), SUMMARY)
			    && StringUtils.endsWith(gzFile.getName(), GZ)) {
			String symbol = StringUtils.substringBefore(gzFile.getName(), Character.toString('_'));
			parser.parse(gzFile, symbol);
		    }
		}

		System.out.println(file.getName() + ": Parsing Done: " + (System.currentTimeMillis() - ms) + "ms");

		for (String symbol : parser.getSymbolList()) {
		    AbstractOutputter out = new HtmlOutputter(parser.getProfileMapBySymbol(symbol), symbol,
			    file.getName());
		    out.output();
		}

		System.out.println(file.getName() + ": Output Done: " + (System.currentTimeMillis() - ms) + "ms");
	    }
	}

    }

    public static void main(String[] args) {
	FrogMaster frogMaster = new FrogMaster();
	frogMaster.run();
    }
}
