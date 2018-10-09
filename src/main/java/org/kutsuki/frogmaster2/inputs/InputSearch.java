package org.kutsuki.frogmaster2.inputs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridCore;

public class InputSearch implements Callable<InputResult> {
    private static final Ticker AT_ES_TICKER = new Ticker('A', 6);
    private static final LocalDate END_DATE = LocalDate.of(2015, 12, 18);

    private Input input;
    private Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap;
    private TreeMap<LocalDateTime, Bar> atEsBarMap;

    public InputSearch(Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap, TreeMap<LocalDateTime, Bar> atEsBarMap,
	    Input input) {
	this.atEsBarMap = atEsBarMap;
	this.input = input;
	this.tickerBarMap = tickerBarMap;

    }

    @Override
    public InputResult call() {
	AbstractStrategy strategy = getStrategy();
	strategy.disableMarginCheck();
	strategy.setup(AT_ES_TICKER, atEsBarMap, input);
	// strategy.setEndDate(END_DATE);
	strategy.run();

	return new InputResult(input, strategy.getBankroll() + strategy.getUnrealized(), strategy.getLowestEquity());

	// return new InputResult(input, getTotal(), getEquity());
    }

    private AbstractStrategy getStrategy() {
	return new HybridCore();
    }

    private int getTotal() {
	AbstractStrategy strategy = getStrategy();
	strategy.disableMarginCheck();
	strategy.setup(AT_ES_TICKER, atEsBarMap, input);
	// strategy.setEndDate(END_DATE);
	strategy.run();

	return strategy.getBankroll() + strategy.getUnrealized();
    }

    private int getEquity() {
	int equity = 0;

	for (Ticker ticker : tickerBarMap.keySet()) {
	    AbstractStrategy strategy = getStrategy();
	    strategy.disableMarginCheck();
	    strategy.setup(ticker, tickerBarMap.get(ticker), input);
	    strategy.run();

	    if (strategy.getLowestEquity() < equity) {
		equity = strategy.getLowestEquity();
	    }
	}

	return equity;
    }
}
