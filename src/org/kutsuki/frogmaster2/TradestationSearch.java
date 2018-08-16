package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.inputs.InputResult;
import org.kutsuki.frogmaster2.inputs.InputSearch;

public class TradestationSearch extends AbstractParser {
    private static final int YEAR = LocalDate.now().getYear() - 2000;
    private static final String WINDOWS_DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String UNIX_DIR = "ES/";
    private static final String TXT = ".txt";

    private ExecutorService es;
    private int cores;
    private Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap;

    public TradestationSearch() {
	this.cores = Runtime.getRuntime().availableProcessors() - 1;
	this.es = Executors.newFixedThreadPool(cores);
	this.tickerBarMap = new HashMap<Ticker, TreeMap<LocalDateTime, Bar>>();
    }

    @Override
    public File getFile(Ticker ticker) {
	File file = null;
	File windowsDir = new File(WINDOWS_DIR);
	File unixDir = new File(UNIX_DIR);
	StringBuilder sb = new StringBuilder();

	if (windowsDir.exists()) {
	    sb.append(WINDOWS_DIR);
	    sb.append(ticker);
	    sb.append(TXT);
	    file = new File(sb.toString());
	} else if (unixDir.exists()) {
	    sb.append(UNIX_DIR);
	    sb.append(ticker);
	    sb.append(TXT);
	    file = new File(sb.toString());
	} else {
	    throw new IllegalArgumentException("No Directory Found!");
	}

	return file;
    }

    public void run() {
	// load data
	for (int year = 6; year <= YEAR; year++) {
	    Ticker h = new Ticker('H', year);
	    tickerBarMap.put(h, load(getFile(h)));

	    Ticker m = new Ticker('M', year);
	    tickerBarMap.put(m, load(getFile(m)));

	    Ticker u = new Ticker('U', year);
	    tickerBarMap.put(u, load(getFile(u)));

	    Ticker z = new Ticker('Z', year);
	    tickerBarMap.put(z, load(getFile(z)));
	}

	// setup inputs
	List<Future<InputResult>> futureList = new ArrayList<>();
	for (int mom = -1000; mom <= -0; mom += 25) {
	    for (int accel = -500; accel <= -0; accel += 25) {
		for (int up = 100; up <= 1500; up += 25) {
		    for (int down = 100; down <= 1500; down += 25) {
			Input input = new Input(mom, accel, up, down);
			Future<InputResult> f = es.submit(new InputSearch(tickerBarMap, input));
			futureList.add(f);
		    }
		}
	    }
	}

	// shutdown
	es.shutdown();

	System.out.println("Starting " + futureList.size() + " tests with " + cores + " cores!");

	TradestationStatus status = new TradestationStatus(futureList.size());
	InputResult first = new InputResult(null, -1, Integer.MAX_VALUE);
	InputResult second = new InputResult(null, -1, Integer.MAX_VALUE);
	InputResult third = new InputResult(null, -1, Integer.MAX_VALUE);

	InputResult firstEquity = new InputResult(null, -1, Integer.MAX_VALUE);
	InputResult secondEquity = new InputResult(null, -1, Integer.MAX_VALUE);
	InputResult thirdEquity = new InputResult(null, -1, Integer.MAX_VALUE);
	for (Future<InputResult> future : futureList) {
	    try {
		InputResult result = future.get();
		status.complete();

		if (result.getRealized() > first.getRealized()) {
		    third = second;
		    second = first;
		    first = result;
		}

		if (result.getEquity() <= first.getEquity()) {
		    thirdEquity = secondEquity;
		    secondEquity = firstEquity;
		    firstEquity = result;
		}
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}

	System.out.println("Realized 1. " + first);
	System.out.println("Realized 2. " + second);
	System.out.println("Realized 3. " + third);

	System.out.println("Lowest Equity 1. " + firstEquity);
	System.out.println("Lowest Equity 2. " + secondEquity);
	System.out.println("Lowest Equity 3. " + thirdEquity);
    }

    public static void main(String[] args) {
	TradestationSearch search = new TradestationSearch();
	search.run();
    }
}
