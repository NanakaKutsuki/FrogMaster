package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridLimit;

// check file, strategy, and symbol
public class TradestationParserAt extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";
    private static final AbstractStrategy STRATEGY = new HybridLimit();
    private static final AbstractInput INPUT = new Input(8, -650, -75, 1025, 1050);
    // private static final LocalDate START_DATE = LocalDate.of(2018, 9, 21);
    // private static final LocalDate END_DATE = LocalDate.of(2018, 12, 21);
    private static final Symbol SYMBOL = new Symbol(Ticker.ES, 'A', 6);

    @Override
    public File getFile(Symbol symbol) {
	return new File(FILE_NAME);
    }

    public void run() {
	setTicker(StringUtils.substringAfterLast(FILE_NAME, Character.toString('/')));
	File file = getFile(null);
	TreeMap<LocalDateTime, Bar> barMap = load(file);

	if (file.exists()) {
	    STRATEGY.setup(SYMBOL, barMap, INPUT);
	    STRATEGY.disableMarginCheck();
	    // STRATEGY.setStartDate(START_DATE);
	    // STRATEGY.setEndDate(END_DATE);
	    STRATEGY.run();
	}
    }

    public void printSummary() {
	System.out.println("Realized: " + revertDollars(STRATEGY.getBankroll()));
	System.out.println("Unrealized: " + revertDollars(STRATEGY.getUnrealized()));
	System.out.println(
		"Lequity: " + revertDollars(STRATEGY.getLowestEquity()) + " " + STRATEGY.getLowestEquityDateTime());
	System.out.println("Total: " + revertDollars(STRATEGY.getBankroll() + STRATEGY.getUnrealized()));
    }

    public static void main(String[] args) {
	TradestationParserAt parser = new TradestationParserAt();
	parser.run();
	parser.printSummary();
    }
}
