package org.kutsuki.frogmaster2;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.kutsuki.frogmaster2.inputs.InputResult;

public class InputResultComparator implements Comparator<Future<InputResult>> {
    @Override
    public int compare(Future<InputResult> lhs, Future<InputResult> rhs) {
	int result = 0;

	try {
	    result = Integer.compare(rhs.get().getTotal(), lhs.get().getTotal());
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	}

	return result;
    }

}
