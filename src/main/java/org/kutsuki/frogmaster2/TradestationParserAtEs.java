package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.HybridInputsOG;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridStrategyOG;

public class TradestationParserAtEs extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";
    private static final AbstractStrategy STRATEGY = new HybridStrategyOG();
    private static final Input INPUT = HybridInputsOG.getInput();
    private static final Ticker TICKER = new Ticker('A', 6);

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    STRATEGY.init(TICKER, barMap, INPUT);
	    STRATEGY.run();
	}
    }

    public void printSummary() {
	System.out.println("Realized: " + revertInt(STRATEGY.getBankroll()));
	System.out.println("Unrealized: " + revertInt(STRATEGY.getUnrealized()));
    }

    public static void main(String[] args) {
	TradestationParserAtEs parser = new TradestationParserAtEs();
	parser.run();
	parser.printSummary();
    }
}
