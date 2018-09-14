package org.kutsuki.frogmaster2.inputs;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.TradestationSearch;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;
import org.kutsuki.frogmaster2.strategy.HybridTest;

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
	int unrealized = 0;
	int equity = Integer.MAX_VALUE;

	for (Ticker ticker : tickerBarMap.keySet()) {
	    if (TradestationSearch.AT_ES || (!TradestationSearch.AT_ES && ticker.getFullYear() <= 2018)) {
		AbstractStrategy strategy = new HybridTest();
		strategy.disableMarginCheck();
		strategy.setup(ticker, tickerBarMap.get(ticker), input);
		// strategy.setEndDate(strategy.calcEndDate('Z', 2009));
		strategy.run();

		realized += strategy.getBankroll();
		unrealized += strategy.getUnrealized();

		if (strategy.getLowestEquity() < equity) {
		    equity = strategy.getLowestEquity();
		}
	    }
	}

	return new InputResult(input, realized, unrealized, equity);
    }
}
