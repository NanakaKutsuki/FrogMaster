package org.kutsuki.frogmaster2.inputs;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.HybridStrategyOG;

public class InputSearch implements Callable<InputResult> {
    private Input input;
    private Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap;

    public InputSearch(Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap, Input input) {
	this.input = input;
	this.tickerBarMap = tickerBarMap;
    }

    @Override
    public InputResult call() {
	int realized = 0;
	int equity = Integer.MAX_VALUE;

	for (Ticker ticker : tickerBarMap.keySet()) {
	    HybridStrategyOG strategy = new HybridStrategyOG(ticker, tickerBarMap.get(ticker), input);
	    strategy.run();
	    realized += strategy.getBankroll();

	    if (strategy.getLowestEquity() < equity) {
		equity = strategy.getLowestEquity();
	    }
	}

	return new InputResult(input, realized, equity);
    }

}
