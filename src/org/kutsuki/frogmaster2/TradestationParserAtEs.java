package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.inputs.HybridInputsOG;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.strategy.HybridStrategyOG;

public class TradestationParserAtEs extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "Desktop/atEs.txt";

    private Ticker ticker;

    public TradestationParserAtEs() {
	this.ticker = new Ticker('A', 6);
    }

    @Override
    public File getFile(Ticker ticker) {
	return new File(FILE_NAME);
    }

    public void run() {
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    Input input = HybridInputsOG.getInput();
	    HybridStrategyOG strategy = new HybridStrategyOG(ticker, barMap, input);
	    strategy.run();

	    // set ticker data
	    ticker.setRealized(strategy.getBankroll());
	}
    }

    public void printSummary() {
	System.out.println("Realized: " + ticker.getRealized());
    }

    public static void main(String[] args) {
	TradestationParserAtEs parser = new TradestationParserAtEs();
	parser.run();
	parser.printSummary();
    }
}
