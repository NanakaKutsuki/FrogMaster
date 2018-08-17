package org.kutsuki.frogmaster2.inputs;

import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.HybridStrategyOG;

public class InputSearch implements Callable<InputResult> {
    private static final Ticker TICKER = new Ticker('A', 6);

    private Input input;

    private TreeMap<LocalDateTime, Bar> barMap;

    public InputSearch(TreeMap<LocalDateTime, Bar> barMap, Input input) {
	this.input = input;
	this.barMap = barMap;
    }

    @Override
    public InputResult call() {

	HybridStrategyOG strategy = new HybridStrategyOG(TICKER, barMap, input);
	strategy.disableMarginCheck();
	strategy.run();

	return new InputResult(input, strategy.getBankroll());
    }

}
