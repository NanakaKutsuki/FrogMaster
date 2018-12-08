package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridTest;

public class TradestationParserAtEs extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";
    private static final AbstractStrategy STRATEGY = new HybridTest();
    private static final Input INPUT = new Input(5, -600, -325, 1300, 1000);
    private static final Ticker TICKER = new Ticker('A', 6);

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    STRATEGY.setup(TICKER, barMap, INPUT);
	    STRATEGY.run();
	}
    }

    public void printSummary() {
	System.out.println("Realized: " + revertDollars(STRATEGY.getBankroll()));
	System.out.println("Unrealized: " + revertDollars(STRATEGY.getUnrealized()));
	System.out.println("Equity: " + revertDollars(STRATEGY.getLowestEquity()));
	System.out.println("Total: " + revertDollars(STRATEGY.getBankroll() + STRATEGY.getUnrealized()));
    }

    public static void main(String[] args) {
	TradestationParserAtEs parser = new TradestationParserAtEs();
	parser.run();
	parser.printSummary();
    }
}
