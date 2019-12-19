package org.kutsuki.frogmaster2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import org.apache.commons.lang3.SystemUtils;
import org.kutsuki.frogmaster2.core.Bar;
import org.kutsuki.frogmaster2.core.Symbol;
import org.kutsuki.frogmaster2.core.Ticker;
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.inputs.InputResult;
import org.kutsuki.frogmaster2.inputs.InputSearch;
import org.kutsuki.frogmaster2.strategy.HybridTest;

// Check File and Ticker
public class TradestationSearch extends AbstractParser {
    private static final boolean OUTPUT = false;
    private static final File WINDOWS_ATES = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/atGC.txt");
    private static final File UNIX_ATES = new File("atGC.txt");
    private static final int CAPACITY = 100000;
    private static final int YEAR = 19;
    private static final LocalDate START_DATE = LocalDate.of(2010, 12, 17);
    private static final LocalDate END_DATE = LocalDate.of(2017, 12, 15);
    private static final String WINDOWS_DIR = "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/";
    private static final String UNIX_DIR = "ES/";
    private static final String TXT = ".txt";
    private static final Ticker TICKER = Ticker.GC;

    private ExecutorService es;
    private int cores;
    private Map<Symbol, TreeMap<LocalDateTime, Bar>> tickerBarMap;
    private List<Future<InputResult>> futureList;
    private List<InputResult> resultList;
    private long start;
    private TradestationStatus status;
    private TreeMap<LocalDateTime, Bar> atEsBarMap;

    public TradestationSearch() {
	super(TICKER.getDivisor());
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
	loadAtEs();
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

	// LocalTime long1 = LocalTime.of(23, 15);
	// LocalTime short1 = LocalTime.of(15, 45);
	// LocalTime long2 = LocalTime.MIN;
	// LocalTime short2 = LocalTime.MIN;
	// for (int hour = 0; hour <= 23; hour++) {
	// for (int minute = 0; minute <= 55; minute += 5) {
	// for (int hour2 = 0; hour2 <= 23; hour2++) {
	// for (int minute2 = 0; minute2 <= 55; minute2 += 5) {
	// long1 = LocalTime.of(hour, minute);
	// short1 = LocalTime.of(hour2, minute2);
	// if (!skip(long1) && !skip(long2) && !skip(short1) && !skip(short2)) {
	// if (count) {
	// tests++;
	// } else {
	// AbstractInput input = new TimeInput(long1, long2, short1, short2);
	// addTest(input);
	// }
	// }
	// }
	// }
	// }
	// }

	// 1. Total $367991.38 LowestEquity -$21418.06 ROI 13.4215x Inputs: (8, -600,
	// -25, 575, 1100, 11, -1700, -200, 2000, 400)
	// 3. Total $366397.38 LowestEquity -$21468.06 ROI 13.3390x Inputs: (8, -600,
	// -25, 575, 1100, 11, -1700, -200, 2000, 300)
	// 4. Total $366166.38 LowestEquity -$21543.06 ROI 13.2943x Inputs: (8, -600,
	// -25, 575, 1100, 11, -1700, -200, 2100, 400)
	// 6. Total $365881.10 LowestEquity -$21418.06 ROI 13.3445x Inputs: (8, -600,
	// -25, 575, 1100, 11, -1700, -200, 1900, 400)
	// 7. Total $365873.12 LowestEquity -$21468.06 ROI 13.3199x Inputs: (8, -600,
	// -25, 575, 1100, 11, -1700, -200, 1900, 300)
	// 8. Total $365645.82 LowestEquity -$21168.06 ROI 13.4587x Inputs: (8, -600,
	// -25, 575, 1100, 11, -1700, -200, 1800, 400)

	tests++;
	AbstractInput input = new Input(8, -60, 0, 50, 90);
	addTest(input);

	// for (int length = 1; length <= 12; length += 1) {
	// for (int mom = -200; mom <= -0; mom += 10) {
	// for (int accel = -200; accel <= -0; accel += 10) {
	// for (int up = 10; up <= 300; up += 10) {
	// for (int down = 10; down <= 300; down += 10) {
	// if (count) {
	// tests++;
	// } else {
	// AbstractInput input = new Input(length, mom, accel, up, down);
	// addTest(input);
	// }
	// }
	//
	// }
	// }
	// }
	// }

	return tests;

    }

    private void addTest(AbstractInput input) {
	InputSearch is = new InputSearch();
	is.setAtEsBarMap(atEsBarMap);
	is.setTickerBarMap(tickerBarMap);
	is.setTicker(TICKER);
	is.setInput(input);
	is.setStrategy(new HybridTest());
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

    public void loadAtEs() {
	File file = null;

	if (WINDOWS_ATES.exists()) {
	    file = WINDOWS_ATES;
	} else if (UNIX_ATES.exists()) {
	    file = UNIX_ATES;
	} else {
	    throw new IllegalArgumentException("File Not Found!");
	}

	atEsBarMap = load(file);
	System.out.println("@ES Loaded!");
    }

    public void loadQuarterly() {
	this.tickerBarMap = new HashMap<Symbol, TreeMap<LocalDateTime, Bar>>();

	for (int year = 6; year <= YEAR; year++) {
	    Symbol h = new Symbol(TICKER, 'H', year);
	    tickerBarMap.put(h, load(getFile(h)));

	    Symbol m = new Symbol(TICKER, 'M', year);
	    tickerBarMap.put(m, load(getFile(m)));

	    Symbol u = new Symbol(TICKER, 'U', year);
	    tickerBarMap.put(u, load(getFile(u)));

	    Symbol z = new Symbol(TICKER, 'Z', year);
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
	sb.append("\nQuarterly ES Runtime: ");
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
