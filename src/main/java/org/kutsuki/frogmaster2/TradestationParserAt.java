package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.LineInput;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.Oscillator;

// check file, strategy, and symbol
public class TradestationParserAt extends AbstractParser {
    private static final String FILE_NAME = "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt";
    private static final AbstractStrategy STRATEGY = new Oscillator();
    private static final AbstractInput INPUT = new LineInput(3, -373, 644, 1558, -1395);
    // private static final AbstractInput INPUT = new TimeInput("23:55", "23:15",
    // "15:45", "00:00");
    private static final LocalDate START_DATE = LocalDate.of(2020, 4, 3);
    private static final LocalDate END_DATE = LocalDate.of(2020, 4, 17);
    private static final Symbol SYMBOL = new Symbol(Ticker.ES, 'A', 6);

    @Override
    public File getFile(Symbol symbol) {
	return new File(FILE_NAME);
    }

    public void run() {
	setTicker(StringUtils.substringAfterLast(FILE_NAME, Character.toString('/')));
	File file = getFile(null);
	BarMap barMap = load(file);

	if (file.exists()) {
	    STRATEGY.setup(SYMBOL, barMap, INPUT);
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
