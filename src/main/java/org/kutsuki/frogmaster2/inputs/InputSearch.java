package org.kutsuki.frogmaster2.inputs;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;

public class InputSearch implements Callable<InputResult> {
    private static final Ticker AT_ES_TICKER = new Ticker('A', 6);
    // private static final LocalDate END_DATE = LocalDate.of(2015, 12, 18);

    private Input input;
    private Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap;
    private TreeMap<LocalDateTime, Bar> atEsBarMap;
    private AbstractStrategy strategy;

    @Override
    public InputResult call() {
	InputResult result = null;

	if (atEsBarMap != null) {
	    result = runAtEs();
	} else if (tickerBarMap != null) {
	    result = runQuarterly();
	}

	return result;
    }

    public void setAtEsBarMap(TreeMap<LocalDateTime, Bar> atEsBarMap) {
	this.atEsBarMap = atEsBarMap;
    }

    public void setTickerBarMap(Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap) {
	this.tickerBarMap = tickerBarMap;
    }

    public void setInput(Input input) {
	this.input = input;
    }

    public void setStrategy(AbstractStrategy strategy) {
	this.strategy = strategy;
    }

    private InputResult runAtEs() {
	strategy.disableMarginCheck();
	strategy.setup(AT_ES_TICKER, atEsBarMap, input);
	// strategy.setEndDate(END_DATE);
	strategy.run();

	return new InputResult(input, strategy.getBankroll() + strategy.getUnrealized(), strategy.getLowestEquity());
    }

    private InputResult runQuarterly() {
	int total = 0;
	int equity = 0;

	for (Ticker ticker : tickerBarMap.keySet()) {
	    strategy.disableMarginCheck();
	    strategy.setup(ticker, tickerBarMap.get(ticker), input);
	    strategy.run();

	    total += strategy.getBankroll() + strategy.getUnrealized();

	    if (strategy.getLowestEquity() < equity) {
		equity = strategy.getLowestEquity();
	    }
	}

	return new InputResult(input, total, equity);
    }
}
