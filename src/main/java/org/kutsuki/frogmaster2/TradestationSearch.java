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
    public static final boolean AT_ES = true;
    private static final boolean OUTPUT = false;
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
    private List<Future<InputResult>> futureList;
    private List<InputResult> resultList;
    private TradestationStatus status;

    public TradestationSearch() {
	this.cores = Runtime.getRuntime().availableProcessors() - 1;
	this.es = Executors.newFixedThreadPool(cores);
	this.futureList = new ArrayList<Future<InputResult>>(100000);
	this.resultList = new ArrayList<InputResult>();
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
	long start = System.currentTimeMillis();

	// load data
	if (AT_ES) {
	    loadAtEs();
	} else {
	    loadQuarterly();
	}

	// count
	long count = 0;
	for (int mom = -1000; mom <= -200; mom += 25) {
	    for (int accel = -500; accel <= 0; accel += 25) {
		for (int up = 500; up <= 1500; up += 25) {
		    for (int down = 500; down <= 1500; down += 25) {
			count++;
		    }
		}
	    }
	}

	System.out.println("Starting " + count + " tests with " + cores + " cores!");
	this.status = new TradestationStatus(count);

	// stage inputs
	// for (int upOG = 100; upOG <= 2000; upOG += 100) {
	// for (int downOG = 100; downOG <= 2000; downOG += 100) {
	// for (int mom = -1000; mom <= 0; mom += 100) {
	// for (int accel = -1000; accel <= 0; accel += 100) {
	// for (int up = 100; up <= 2000; up += 100) {
	// for (int down = 100; down <= 2000; down += 100) {
	// Input input = new Input(-625, -150, upOG, downOG, 6, 3, mom, accel, up,
	// down);
	// addTest(input);
	// }
	// }
	// }
	// }
	// }
	// }

	for (int mom = -1000; mom <= -200; mom += 25) {
	    for (int accel = -500; accel <= 0; accel += 25) {
		for (int up = 500; up <= 1500; up += 25) {
		    for (int down = 500; down <= 1500; down += 25) {
			Input input = new Input(mom, accel, up, down, 0, 0, 0, 0, 0, 0);
			addTest(input);
		    }
		}
	    }
	}

	// shutdown
	es.shutdown();

	waitForFutures();
	System.out.println("11Runtime: " + status.formatTime(System.currentTimeMillis() - start));
    }

    private void addTest(Input input) {
	Future<InputResult> f = es.submit(new InputSearch(tickerBarMap, input));
	futureList.add(f);

	if (futureList.size() >= 100000) {
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
	for (int i = 0; i < 9; i++) {
	    resultList2.add(resultList.get(i));
	}

	resultList = new ArrayList<InputResult>(resultList2);
	futureList = new ArrayList<Future<InputResult>>(100000);

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

    private void print() {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < resultList.size(); i++) {
	    sb.append(i + 1);
	    sb.append('.');
	    sb.append(' ');
	    sb.append(resultList.get(i));
	    sb.append('\n');
	}

	System.out.println(sb.toString());

	if (OUTPUT) {
	    File out = new File("FrogMaster-" + System.currentTimeMillis() + ".txt");
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
