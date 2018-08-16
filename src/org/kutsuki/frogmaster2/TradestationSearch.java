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
	for (int mom = -700; mom <= -500; mom += 25) {
	    for (int accel = -200; accel <= -0; accel += 25) {
		for (int up = 100; up <= 1500; up += 25) {
		    for (int down = 100; down <= 1500; down += 25) {
			Input input = new Input(mom, accel, up, down);
			Future<InputResult> f = es.submit(new InputSearch(tickerBarMap, input));
			futureList.add(f);
		    }
		}
	    }
	}

	// test
	// Input input = HybridInputsOG.getInput();
	// Future<InputResult> f = es.submit(new InputSearch(tickerBarMap, input));
	// futureList.add(f);

	// shutdown
	es.shutdown();

	System.out.println("Starting " + futureList.size() + " tests with " + cores + " cores!");

	TradestationStatus status = new TradestationStatus(futureList.size());
	InputResult first = new InputResult();
	InputResult second = new InputResult();
	InputResult third = new InputResult();

	InputResult firstEquity = new InputResult();
	InputResult secondEquity = new InputResult();
	InputResult thirdEquity = new InputResult();
	for (Future<InputResult> future : futureList) {
	    try {
		InputResult result = future.get();
		status.complete();

		if (result.getRealized() > first.getRealized()) {
		    third = second;
		    second = first;
		    first = result;
		}

		if (result.getLowestEquity() >= first.getLowestEquity()) {
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

	// Realized 1. $241594.52 $-8581.50 Inputs: (-625, -125, 900, 1050)
	// Realized 2. $238568.08 $-6936.02 Inputs: (-625, -150, 1075, 925)
	// Realized 3. $233873.68 $-8656.50 Inputs: (-625, -150, 900, 1050)

    }

    public static void main(String[] args) {
	TradestationSearch search = new TradestationSearch();
	search.run();
    }
}
