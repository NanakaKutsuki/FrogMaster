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
    private static final boolean OUTPUT = false;
    private static final File WINDOWS_ATES = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/atES.txt");
    private static final File UNIX_ATES = new File("atES.txt");
    private static final int CAPACITY = 100000;
    private static final int YEAR = LocalDate.now().getYear() - 2000;
    private static final String WINDOWS_DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String UNIX_DIR = "ES/";
    private static final String TXT = ".txt";

    private ExecutorService es;
    private int cores;
    private Map<Ticker, TreeMap<LocalDateTime, Bar>> tickerBarMap;
    private List<Future<InputResult>> futureList;
    private List<InputResult> resultList;
    private long start;
    private TradestationStatus status;
    private TreeMap<LocalDateTime, Bar> atEsBarMap;

    public TradestationSearch() {
	this.cores = Runtime.getRuntime().availableProcessors() - 1;
	this.es = Executors.newFixedThreadPool(cores);
	this.futureList = new ArrayList<Future<InputResult>>(CAPACITY);
	this.resultList = new ArrayList<InputResult>();
	this.start = System.currentTimeMillis();
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
	loadAtEs();
	loadQuarterly();

	// count
	int count = stage(true);
	System.out.println("Starting " + count + " tests with " + cores + " cores!");
	this.status = new TradestationStatus(count);

	// stage inputs
	stage(false);

	// shutdown
	es.shutdown();

	waitForFutures();
    }

    private int stage(boolean count) {
	int tests = 0;

	// 1. Total $235552.88 LowestEquity -$8357.22 ROI 16.6384x Inputs: (-600, -100,
	// 925, 1050)
	// 1. Total $223872.92 LowestEquity -$9308.66 ROI 14.8175x Inputs: (5, -600,
	// -300, 925, 1025)

	for (int mom = -700; mom <= -500; mom += 25) {
	    for (int accel = -400; accel <= 0; accel += 25) {
		for (int up = 500; up <= 1500; up += 25) {
		    for (int down = 500; down <= 1500; down += 25) {
			if (count) {
			    tests++;
			} else {
			    Input input = new Input(5, mom, accel, up, down);
			    addTest(input);
			}
		    }
		}
	    }
	}
	return tests;
    }

    private void addTest(Input input) {
	Future<InputResult> f = es.submit(new InputSearch(tickerBarMap, atEsBarMap, input));
	futureList.add(f);

	if (futureList.size() >= CAPACITY) {
	    waitForFutures();
	}
    }

    private void waitForFutures() {
	for (Future<InputResult> future : futureList) {
	    try {
		InputResult result = future.get();
		resultList.add(result);
		status.complete();
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}

	Collections.sort(resultList);
	List<InputResult> resultList2 = new ArrayList<InputResult>();

	int size = resultList.size() < 9 ? resultList.size() : 9;
	for (int i = 0; i < size; i++) {
	    resultList2.add(resultList.get(i));
	}

	resultList = new ArrayList<InputResult>(resultList2);
	futureList = new ArrayList<Future<InputResult>>(CAPACITY);

	print();
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

	atEsBarMap = load(file);
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

    private void print() {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < resultList.size(); i++) {
	    sb.append(i + 1);
	    sb.append('.');
	    sb.append(' ');
	    sb.append(resultList.get(i));
	    sb.append('\n');
	}

	long runtime = System.currentTimeMillis() - start;
	sb.append("\nCore+AH ES Runtime: ");
	sb.append(status.formatTime(runtime));
	System.out.println(sb.toString());

	if (OUTPUT) {
	    File out = new File("FrogMaster-" + runtime + ".txt");
	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
		bw.write(sb.toString());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public static void main(String[] args) {
	TradestationSearch search = new TradestationSearch();
	search.run();
    }
}
