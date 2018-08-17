package org.kutsuki.frogmaster2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private static final File WINDOWS_FILE = new File(
	    "C:/Users/" + System.getProperty("user.name") + "/Desktop/ES/atEs.txt");
    private static final File UNIX_FILE = new File("ES/atEs.txt");

    private ExecutorService es;
    private int cores;

    public TradestationSearch() {
	this.cores = Runtime.getRuntime().availableProcessors();
	if (WINDOWS_FILE.exists()) {
	    this.cores -= 1;
	}

	this.es = Executors.newFixedThreadPool(cores);
    }

    @Override
    public File getFile(Ticker ticker) {
	File file = null;

	if (WINDOWS_FILE.exists()) {
	    file = WINDOWS_FILE;
	} else if (UNIX_FILE.exists()) {
	    file = UNIX_FILE;
	} else {
	    throw new IllegalArgumentException("File Not Found!");
	}

	return file;
    }

    public void run() {
	// load data

	TreeMap<LocalDateTime, Bar> barMap = load(getFile(null));

	// setup inputs
	List<Future<InputResult>> futureList = new ArrayList<>();
	for (int mom = -700; mom <= -500; mom += 25) {
	    for (int accel = -200; accel <= -0; accel += 25) {
		for (int up = 100; up <= 1500; up += 25) {
		    for (int down = 100; down <= 1500; down += 25) {
			Input input = new Input(mom, accel, up, down);
			Future<InputResult> f = es.submit(new InputSearch(barMap, input));
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
	for (Future<InputResult> future : futureList) {
	    try {
		InputResult result = future.get();
		status.complete();

		if (result.getRealized() > first.getRealized()) {
		    third = second;
		    second = first;
		    first = result;
		}

	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}

	System.out.println("Realized 1. " + first);
	System.out.println("Realized 2. " + second);
	System.out.println("Realized 3. " + third);
    }

    public static void main(String[] args) {
	TradestationSearch search = new TradestationSearch();
	search.run();
    }
}
