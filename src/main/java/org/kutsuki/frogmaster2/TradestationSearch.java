package org.kutsuki.frogmaster2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.SystemUtils;
import org.kutsuki.frogmaster2.core.BarMap;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.InputResult;
import org.kutsuki.frogmaster2.inputs.InputSearch;
import org.kutsuki.frogmaster2.inputs.TimeInput;
import org.kutsuki.frogmaster2.strategy.TimeShort;

public class TradestationSearch extends AbstractParser {
    private static final boolean OUTPUT = false;
    private static final File AT_FILE = new File("atES.txt");
    private static final File WINDOWS_AT = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/" + AT_FILE.getName());
    private static final int CAPACITY = 100000;
    private static final int YEAR = 20;
    private static final LocalDate START_DATE = LocalDate.of(2010, 12, 17);
    private static final LocalDate END_DATE = LocalDate.of(2020, 3, 27);
    private static final String WINDOWS_DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String UNIX_DIR = "ES/";
    private static final String TXT = ".txt";

    private BarMap atBarMap;
    private ExecutorService es;
    private int cores;
    private Map<Symbol, BarMap> tickerBarMap;
    private List<Future<InputResult>> futureList;
    private List<InputResult> resultList;
    private long start;
    private TradestationStatus status;

    public TradestationSearch() {
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
	// loadQuarterly();

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

	// 1. Total $142846.00 LowestEquity -$34529.00 ROI 3.5245x Inputs:
	// ("23:20","00:00","15:45","00:00")
	LocalTime long1 = LocalTime.of(23, 15);
	LocalTime short1 = LocalTime.of(15, 45);
	LocalTime long2 = LocalTime.MIN;
	LocalTime short2 = LocalTime.MIN;
	for (int hour = 0; hour <= 23; hour++) {
	    for (int minute = 0; minute <= 55; minute += 5) {
		for (int hour2 = 0; hour2 <= 23; hour2++) {
		    for (int minute2 = 0; minute2 <= 55; minute2 += 5) {
			long1 = LocalTime.of(hour, minute);
			short1 = LocalTime.of(hour2, minute2);
			if (!skip(long1) && !skip(long2) && !skip(short1) && !skip(short2)) {
			    if (count) {
				tests++;
			    } else {
				AbstractInput input = new TimeInput(long1, long2, short1, short2);
				addTest(input);
			    }
			}
		    }
		}
	    }
	}

	// 1. Total $247861.00 LowestEquity -$22411.50 ROI 8.7240x Inputs: (3, -731,
	// 644, 200, 0)
	// 2. Total $245496.00 LowestEquity -$21277.00 ROI 9.0001x Inputs: (3, -411,
	// 644, 200, 0)

	// TODO
	// for (int core = 900; core < 1100; core += 1) {
	// for (int ah = -500; ah < -300; ah += 1) {
	//
	// if (count) {
	// tests++;
	// } else {
	// AbstractInput input = new LineInput(core, ah, 0);
	// addTest(input);
	// }
	// }
	// }

	return tests;
    }

    private void addTest(AbstractInput input) {
	InputSearch is = new InputSearch();
	is.setAtEsBarMap(atBarMap);
	is.setTickerBarMap(tickerBarMap);
	is.setTicker(getTicker());
	is.setInput(input);
	is.setStrategy(new TimeShort());
	// is.setStartDate(START_DATE);
	// is.setEndDate(END_DATE);

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

    private void loadQuarterly() {
	this.tickerBarMap = new HashMap<Symbol, BarMap>();

	for (int year = 6; year <= YEAR; year++) {
	    Symbol h = new Symbol(getTicker(), 'H', year);
	    tickerBarMap.put(h, load(getFile(h)));

	    Symbol m = new Symbol(getTicker(), 'M', year);
	    tickerBarMap.put(m, load(getFile(m)));

	    Symbol u = new Symbol(getTicker(), 'U', year);
	    tickerBarMap.put(u, load(getFile(u)));

	    Symbol z = new Symbol(getTicker(), 'Z', year);
	    tickerBarMap.put(z, load(getFile(z)));

	    System.out.println("Loaded: " + z.getFullYear());
	}

	System.out.println("Quarterly ES Loaded!");
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
	TradestationSearch search = new TradestationSearch();
	search.run();
    }
}
