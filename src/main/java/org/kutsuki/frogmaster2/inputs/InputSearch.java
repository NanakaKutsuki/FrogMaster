package org.kutsuki.frogmaster2.inputs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.strategy.AbstractStrategy;

public class InputSearch implements Callable<InputResult> {
    private AbstractInput input;
    private AbstractStrategy strategy;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<Symbol, TreeMap<LocalDateTime, Bar>> tickerBarMap;
    private TreeMap<LocalDateTime, Bar> atEsBarMap;
    private Ticker ticker;

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

    public void setTickerBarMap(Map<Symbol, TreeMap<LocalDateTime, Bar>> tickerBarMap) {
	this.tickerBarMap = tickerBarMap;
    }

    public void setInput(AbstractInput input) {
	this.input = input;
    }

    public void setStartDate(LocalDate startDate) {
	this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
	this.endDate = endDate;
    }

    public void setStrategy(AbstractStrategy strategy) {
	this.strategy = strategy;
    }

    public void setTicker(Ticker ticker) {
	this.ticker = ticker;
    }

    private InputResult runAtEs() {
	Symbol at = new Symbol(ticker, 'A', 6);
	List<LocalDateTime> keyList = new ArrayList<LocalDateTime>(atEsBarMap.keySet());
	List<Bar> barList = new ArrayList<Bar>(atEsBarMap.values());
	strategy.setup(at, keyList, barList, input);
	applyDates();

	strategy.run();

	return new InputResult(input, ticker.getDivisor(), strategy.getBankroll() + strategy.getUnrealized(),
		strategy.getLowestEquity());
    }

    private InputResult runQuarterly() {
	int total = 0;
	int equity = 0;
	BigDecimal divisor = null;

	for (Symbol symbol : tickerBarMap.keySet()) {
	    List<LocalDateTime> keyList = new ArrayList<LocalDateTime>(tickerBarMap.get(symbol).keySet());
	    List<Bar> barList = new ArrayList<Bar>(tickerBarMap.get(symbol).values());
	    strategy.setup(symbol, keyList, barList, input);

	    if (divisor == null) {
		divisor = symbol.getTicker().getDivisor();
	    }

	    // if (ticker.toString().equals("ESZ18")) {
	    // strategy.setEndDate(END_DATE);
	    // }

	    strategy.run();

	    total += strategy.getBankroll() + strategy.getUnrealized();

	    if (strategy.getLowestEquity() < equity) {
		equity = strategy.getLowestEquity();
	    }
	}

	return new InputResult(input, divisor, total, equity);
    }

    private void applyDates() {
	if (startDate != null) {
	    strategy.setStartDate(startDate);
	}

	if (endDate != null) {
	    strategy.setEndDate(endDate);
	}
    }
}
