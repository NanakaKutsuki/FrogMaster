package org.kutsuki.frogmaster2.inputs;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridOG;

public class InputSearch implements Callable<InputResult> {
    private static final Ticker AT_ES_TICKER = new Ticker('A', 6);

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
	return new InputResult(input, getTotal(), getEquity());
    }

    /**
     * CHANGE STRATEGY IN 2 SPOTS
     */
    private int getTotal() {
	AbstractStrategy strategy = new HybridOG();
	strategy.disableMarginCheck();
	strategy.setup(AT_ES_TICKER, atEsBarMap, input);
	strategy.run();
	return strategy.getBankroll() + strategy.getUnrealized();
    }

    /**
     * CHANGE STRATEGY IN 2 SPOTS
     */
    private int getEquity() {
	int equity = Integer.MAX_VALUE;

	for (Ticker ticker : tickerBarMap.keySet()) {
	    AbstractStrategy strategy = new HybridOG();
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
