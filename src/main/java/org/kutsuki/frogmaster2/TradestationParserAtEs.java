package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.HybridInputsCore;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.strategy.HybridStrategyCore;

public class TradestationParserAtEs extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";

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
	    Input input = HybridInputsCore.getInput();
	    HybridStrategyCore strategy = new HybridStrategyCore(ticker, barMap, input);
	    strategy.run();

	    // set ticker data
	    ticker.setRealized(strategy.getBankroll());
	    ticker.setEquity(strategy.getUnrealized());
	}
    }

    public void printSummary() {
	System.out.println("Realized: " + ticker.getRealized());
	System.out.println("Unrealized: " + ticker.getEquity());
    }

    public static void main(String[] args) {
	TradestationParserAtEs parser = new TradestationParserAtEs();
	parser.run();
	parser.printSummary();
    }
}
