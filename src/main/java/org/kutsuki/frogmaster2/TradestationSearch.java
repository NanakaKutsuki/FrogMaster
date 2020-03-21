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
import org.kutsuki.frogmaster2.inputs.AbstractInput;
import org.kutsuki.frogmaster2.inputs.Input;
import org.kutsuki.frogmaster2.inputs.InputResult;
import org.kutsuki.frogmaster2.inputs.InputSearch;
import org.kutsuki.frogmaster2.strategy.HybridTest;

public class TradestationSearch extends AbstractParser {
    private static final boolean OUTPUT = false;
    private static final File AT_FILE = new File("atES.txt");
    private static final File WINDOWS_AT = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/" + AT_FILE.getName());
    private static final int CAPACITY = 100000;
    private static final int YEAR = 19;
    private static final LocalDate START_DATE = LocalDate.of(2010, 12, 17);
    private static final LocalDate END_DATE = LocalDate.of(2019, 3, 15);
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

	// 1. Total $326833.64 LowestEquity -$34232.28 ROI 8.1237x Inputs: (8, -650,
	// -75, 1025, 1050)
	// 2. Total $324006.26 LowestEquity -$35301.40 ROI 7.8449x Inputs: (8, -650,
	// -75, 1025, 1075)
	// 3. Total $323842.36 LowestEquity -$34906.54 ROI 7.9166x Inputs: (8, -650,
	// -75, 1075, 1050)
	// 4. Total $323830.14 LowestEquity -$31159.74 ROI 8.7145x Inputs: (8, -650,
	// -50, 625, 1025)
	// 5. Total $323797.46 LowestEquity -$23633.02 ROI 10.9269x Inputs: (8, -650,
	// -75, 1100, 1050)
	// 6. Total $323757.66 LowestEquity -$37181.78 ROI 7.4976x Inputs: (8, -625, 0,
	// 625, 1025)
	// 7. Total $322189.32 LowestEquity -$32273.60 ROI 8.4181x Inputs: (8, -650,
	// -100, 1025, 1050)
	// 8. Total $322159.80 LowestEquity -$24677.88 ROI 10.5014x Inputs: (8, -650,
	// -75, 1100, 1075)
	// 9. Total $321817.20 LowestEquity -$35963.90 ROI 7.6689x Inputs: (8, -650,
	// -75, 1075, 1075)

	// 1. Total $285482.78 LowestEquity -$23254.40 ROI 9.7586x Inputs: (8, -700,
	// -100, 1100, 1100)
	// 2. Total $273971.58 LowestEquity -$35226.44 ROI 6.6455x Inputs: (8, -700, 0,
	// 1100, 1100)
	// 3. Total $264422.94 LowestEquity -$25267.02 ROI 8.4569x Inputs: (8, -700,
	// -200, 1100, 1100)
	// 4. Total $262621.26 LowestEquity -$35049.38 ROI 6.3977x Inputs: (8, -700,
	// -100, 1000, 1100)
	// 5. Total $261045.58 LowestEquity -$35154.32 ROI 6.3431x Inputs: (8, -700,
	// -100, 700, 1100)
	// 6. Total $260445.26 LowestEquity -$35017.56 ROI 6.3496x Inputs: (8, -700,
	// -100, 800, 1100)
	// 7. Total $259706.78 LowestEquity -$34876.44 ROI 6.3535x Inputs: (8, -700,
	// -100, 900, 1100)
	// 8. Total $258982.38 LowestEquity -$32422.16 ROI 6.7404x Inputs: (8, -600,
	// -100, 900, 1100)
	// 9. Total $255731.42 LowestEquity -$40153.32 ROI 5.5409x Inputs: (5, -600,
	// -300, 900, 1000)

	// TODO
	// for (int length = 1; length <= 12; length += 1) {
	for (int mom = -800; mom <= -500; mom += 25) {
	    for (int accel = -300; accel <= -0; accel += 25) {
		for (int up = 600; up <= 1200; up += 25) {
		    for (int down = 1000; down <= 1200; down += 25) {
			if (count) {
			    tests++;
			} else {
			    AbstractInput input = new Input(8, mom, accel, up, down);
			    addTest(input);
			}
		    }

		}
	    }
	}
	// }

	return tests;

    }

    private void addTest(AbstractInput input) {
	InputSearch is = new InputSearch();
	is.setAtEsBarMap(atBarMap);
	is.setTickerBarMap(tickerBarMap);
	is.setTicker(getTicker());
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
	this.tickerBarMap = new HashMap<Symbol, TreeMap<LocalDateTime, Bar>>();

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
