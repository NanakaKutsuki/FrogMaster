package org.kutsuki.frogmaster2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    public static boolean AT_ES = true;

    private static final File WINDOWS_ATES = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt");
    private static final File UNIX_ATES = new File("atES.txt");
    private static final String WINDOWS_DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String UNIX_DIR = "ES/";
    private static final String TXT = ".txt";
    private static final int YEAR = LocalDate.now().getYear() - 2000;

    private ExecutorService es;
    private int cores;
    private Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap;

    public TradestationSearch() {
	this.tickerBarMap = new HashMap<Ticker, TreeMap<LocalDateTime, Bar>>();
	this.cores = Runtime.getRuntime().availableProcessors() - 1;
	this.es = Executors.newFixedThreadPool(cores);
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
	if (AT_ES) {
	    loadAtEs();
	} else {
	    loadQuarterly();
	}

	// setup inputs
	List<Future<InputResult>> futureList = new ArrayList<>();
	for (int mom = -1000; mom <= -100; mom += 25) {
	    for (int accel = -1000; accel <= -0; accel += 25) {
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

	// wait for all threads to complete
	TradestationStatus status = new TradestationStatus(futureList.size());
	for (Future<InputResult> future : futureList) {
	    try {
		future.get();
		status.complete();
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}

	// print top 10
	File out = new File("output/FrogMaster-" + System.currentTimeMillis() + ".txt");
	try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
	    Collections.sort(futureList, new InputResultComparator());
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < 10; i++) {
		sb.append(i + 1);
		sb.append('.');
		sb.append(' ');
		sb.append(futureList.get(i).get());
		sb.append('\n');
	    }

	    System.out.println(sb.toString());
	    bw.write(sb.toString());
	} catch (IOException | InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	}

    }

    private void loadAtEs() {
	File file = null;

	if (WINDOWS_ATES.exists()) {
	    file = WINDOWS_ATES;
	} else if (UNIX_ATES.exists()) {
	    file = UNIX_ATES;
	} else {
	    throw new IllegalArgumentException("File Not Found!");
	}

	Ticker ticker = new Ticker('A', 6);
	tickerBarMap.put(ticker, load(file));
    }

    private void loadQuarterly() {
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
    }

    public static void main(String[] args) {
	TradestationSearch search = new TradestationSearch();
	search.run();
    }
}
