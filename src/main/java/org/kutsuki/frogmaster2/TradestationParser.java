package org.kutsuki.frogmaster2;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.HybridInputsOG;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridStrategyOG;

public class TradestationParser extends AbstractParser {
    private static final AbstractStrategy STRATEGY = new HybridStrategyOG();
    private static final Input INPUT = HybridInputsOG.getInput();
    private static final int YEAR = LocalDate.now().getYear() - 2000;
    private static final String DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String TXT = ".txt";

    private Map<String, Ticker> tickerMap;

    public TradestationParser() {
	this.tickerMap = new HashMap<String, Ticker>();

	for (int year = 6; year <= YEAR; year++) {
	    Ticker h = new Ticker('H', year);
	    Ticker m = new Ticker('M', year);
	    Ticker u = new Ticker('U', year);
	    Ticker z = new Ticker('Z', year);

	    tickerMap.put(h.toString(), h);
	    tickerMap.put(m.toString(), m);
	    tickerMap.put(u.toString(), u);
	    tickerMap.put(z.toString(), z);
	}
    }

    @Override
    public File getFile(Ticker ticker) {
	StringBuilder sb = new StringBuilder();
	sb.append(DIR);
	sb.append(ticker);
	sb.append(TXT);
	return new File(sb.toString());
    }

    public void run(char month, int year) {
	Ticker ticker = getTicker(month, year);
	File file = getFile(ticker);

	if (file.exists()) {
	    TreeMap<LocalDateTime, Bar> barMap = load(file);

	    STRATEGY.setup(ticker, barMap, INPUT);
	    STRATEGY.run();

	    // set ticker data
	    ticker.setEquityDateTime(STRATEGY.getLowestEquityDateTime());
	    ticker.setEquity(STRATEGY.getLowestEquity());
	    ticker.setRealized(STRATEGY.getBankroll());
	    ticker.setUnrealized(STRATEGY.getUnrealized());
	    ticker.setBankrollRE(STRATEGY.getBankrollRE());
	    tickerMap.put(ticker.toString(), ticker);
	}
    }

    public Ticker getTicker(char month, int year) {
	return tickerMap.get(Ticker.getKey(month, year));
    }

    public void printEquityDateTime() {
	System.out.println("Lowest Equity Dates");
	for (int year = YEAR; year >= 6; year--) {
	    Ticker h = getTicker('H', year);
	    Ticker m = getTicker('M', year);
	    Ticker u = getTicker('U', year);
	    Ticker z = getTicker('Z', year);

	    LocalDateTime hDate = h.getEquityDateTime();
	    LocalDateTime uDate = u.getEquityDateTime();
	    LocalDateTime mDate = m.getEquityDateTime();
	    LocalDateTime zDate = z.getEquityDateTime();

	    if (hDate != null) {
		System.out.println(h + "," + hDate.toLocalDate() + "," + hDate.toLocalTime());
	    }

	    if (mDate != null) {
		System.out.println(m + "," + mDate.toLocalDate() + "," + mDate.toLocalTime());
	    }

	    if (uDate != null) {
		System.out.println(u + "," + uDate.toLocalDate() + "," + uDate.toLocalTime());
	    }

	    if (zDate != null) {
		System.out.println(z + "," + zDate.toLocalDate() + "," + zDate.toLocalTime());
	    }
	}
    }

    public void printRealized() {
	System.out.println("--------------------------");
	System.out.println("Unrealized: " + getUnrealized());
	System.out.println("Realized");
	for (int year = YEAR; year >= 6; year--) {
	    BigDecimal h = revertInt(getTicker('H', year).getRealized());
	    BigDecimal m = revertInt(getTicker('M', year).getRealized());
	    BigDecimal u = revertInt(getTicker('U', year).getRealized());
	    BigDecimal z = revertInt(getTicker('Z', year).getRealized());
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    public BigDecimal getUnrealized() {
	int unrealized = 0;
	for (int year = YEAR; year >= 6; year--) {
	    unrealized += getTicker('H', year).getUnrealized();
	    unrealized += getTicker('M', year).getUnrealized();
	    unrealized += getTicker('U', year).getUnrealized();
	    unrealized += getTicker('Z', year).getUnrealized();
	}

	return revertInt(unrealized);
    }

    public void printEquity() {
	System.out.println("--------------------------");
	System.out.println("Lowest Equity");
	for (int year = YEAR; year >= 6; year--) {
	    BigDecimal h = revertInt(getTicker('H', year).getEquity());
	    BigDecimal m = revertInt(getTicker('M', year).getEquity());
	    BigDecimal u = revertInt(getTicker('U', year).getEquity());
	    BigDecimal z = revertInt(getTicker('Z', year).getEquity());
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    public void printBankrollRE() {
	System.out.println("--------------------------");
	System.out.println("Rebalance");
	for (int year = YEAR; year >= 6; year--) {
	    BigDecimal h = revertInt(getTicker('H', year).getBankrollRE());
	    BigDecimal m = revertInt(getTicker('M', year).getBankrollRE());
	    BigDecimal u = revertInt(getTicker('U', year).getBankrollRE());
	    BigDecimal z = revertInt(getTicker('Z', year).getBankrollRE());
	    System.out.println(h + "," + m + "," + u + "," + z);
	}
    }

    public static void main(String[] args) {
	TradestationParser parser = new TradestationParser();
	// parser.run('H', 18);

	for (int year = 6; year <= YEAR; year++) {
	    parser.run('H', year);
	    parser.run('M', year);
	    parser.run('U', year);
	    parser.run('Z', year);
	}

	// parser.printEquityDateTime();
	parser.printRealized();
	parser.printEquity();
	parser.printBankrollRE();
    }
}
