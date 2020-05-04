package org.kutsuki.frogmaster2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.SystemUtils;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.InputResult;
import org.kutsuki.frogmaster2.inputs.InputSearch;
import org.kutsuki.frogmaster2.inputs.TimeInput;
import org.kutsuki.frogmaster2.strategy.HybridTest;

public class TradestationSearch222 extends AbstractParser {
    private static final boolean OUTPUT = false;
    private static final File AT_FILE = new File("atES.txt");
    private static final File WINDOWS_AT = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/" + AT_FILE.getName());
    private static final int CAPACITY = 100000;
    private static final int YEAR = 20;
    private static final String WINDOWS_DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String UNIX_DIR = "ES/";
    private static final String TXT = ".txt";

    private ExecutorService es;
    private int cores;
    private Map<Symbol, TreeMap<LocalDateTime, Bar>> tickerBarMap;
    private List<Future<InputResult>> futureList;
    private List<InputResult> resultList;
    private long start;
    private TradestationStatus status;
    private TreeMap<LocalDateTime, Bar> atBarMap;

    public TradestationSearch222() {
	setTicker(AT_FILE.getName());
	this.cores = Runtime.getRuntime().availableProcessors();
	if (SystemUtils.IS_OS_WINDOWS) {
	    this.cores -= 2;
	}

	this.es = Executors.newFixedThreadPool(cores);
	this.futureList = new ArrayList<Future<InputResult>>(CAPACITY);
	this.resultList = new ArrayList<InputResult>();
	this.start = System.currentTimeMillis();
    }

    @Override
    public File getFile(Symbol symbol) {
	File file = null;
	StringBuilder sb = new StringBuilder();

	if (SystemUtils.IS_OS_WINDOWS) {
	    sb.append(WINDOWS_DIR);
	    sb.append(symbol);
	    sb.append(TXT);
	    file = new File(sb.toString());
	} else if (SystemUtils.IS_OS_UNIX) {
	    sb.append(UNIX_DIR);
	    sb.append(symbol);
	    sb.append(TXT);
	    file = new File(sb.toString());
	}

	return file;
    }

    public void run() {
	// load data
	loadAt();

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

    private boolean skip(LocalTime time) {
	int hour = time.getHour();
	int min = time.getMinute();

	return (hour == 16 && min == 10) || (hour == 16 && min == 15) || (hour == 16 && min == 20)
		|| (hour == 16 && min == 25) || (hour == 16 && min == 30) || (hour == 16 && min == 55) || hour == 17
		|| (hour == 18 && min == 0);
    }

    private int stage(boolean count) {
	int tests = 0;

	LocalTime long1 = null;
	LocalTime long2 = null;
	for (int hour = 0; hour <= 23; hour++) {
	    for (int minute = 0; minute <= 55; minute += 5) {
		for (int hour2 = 0; hour2 <= 23; hour2++) {
		    for (int minute2 = 0; minute2 <= 55; minute2 += 5) {
			long1 = LocalTime.of(hour, minute);
			long2 = LocalTime.of(hour2, minute2);
			if (!skip(long1) && !skip(long2)) {
			    if (count) {
				tests++;
			    } else {
				AbstractInput input = new TimeInput(long1, long2, null, null);
				addTest(input);
			    }
			}
		    }
		}
	    }
	}

	return tests;

    }

    private void addTest(AbstractInput input) {
	InputSearch is = new InputSearch();
	is.setAtEsBarMap(atBarMap);
	is.setTickerBarMap(tickerBarMap);
	is.setTicker(getTicker());
	is.setInput(input);
	is.setStrategy(new HybridTest());

	Future<InputResult> f = es.submit(is);
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

	futureList = new ArrayList<Future<InputResult>>(CAPACITY);
	print();
    }

    private void loadAt() {
	File file = null;

	if (WINDOWS_AT.exists()) {
	    file = WINDOWS_AT;
	} else if (AT_FILE.exists()) {
	    file = AT_FILE;
	} else {
	    throw new IllegalArgumentException("File Not Found!");
	}

	atBarMap = load(file);
	System.out.println(AT_FILE.getName() + " Loaded!");
    }

    private void print() {
	TreeMap<LocalTime, InputResult> resultMap = new TreeMap<LocalTime, InputResult>();
	for (InputResult ir : resultList) {
	    LocalTime key = ((TimeInput) ir.getInput()).getLong1();
	    InputResult result = resultMap.get(key);
	    if (result != null) {
		if (result.getTotal() < ir.getTotal()) {
		    result = ir;
		}
	    } else {
		result = ir;
	    }

	    resultMap.put(key, result);
	}

	StringBuilder sb = new StringBuilder();
	for (InputResult result : resultMap.values()) {
	    sb.append(((TimeInput) result.getInput()).getLong1()).append(',');
	    sb.append(((TimeInput) result.getInput()).getLong2()).append(',');
	    sb.append(result.getTotal());
	    sb.append('\n');
	}

	long runtime = System.currentTimeMillis() - start;
	sb.append("\nRuntime: ");
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
	TradestationSearch222 search = new TradestationSearch222();
	search.run();
    }
}
