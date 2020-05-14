package org.kutsuki.frogmaster2.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class BarMap {
    private List<LocalDateTime> dateList;
    private List<Bar> barList;

    public BarMap(List<LocalDateTime> dateList, List<Bar> barList, boolean precalc) {
	this.dateList = dateList;
	this.barList = barList;

	if (precalc) {
	    for (int i = 0; i < barList.size(); i++) {
		Bar bar = barList.get(i);
		bar.setPo(priceOscillator(5, 34, i));
	    }
	}
    }

    public int averageFC(int index, int length) {
	int result = 0;

	if (index > length) {
	    int sum = 0;

	    for (int i = 0; i < length; i++) {
		sum += getPrevBar(index, length).getClose();
	    }

	    result = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(length), 0, RoundingMode.HALF_UP).intValue();
	}

	return result;
    }

    public int priceOscillator(int fastLength, int slowLength, int index) {
	BigDecimal fastAvg = BigDecimal.ZERO;
	BigDecimal slowAvg = BigDecimal.ZERO;

	if (index > fastLength) {
	    BigDecimal sum = BigDecimal.ZERO;

	    for (int i = 0; i < fastLength; i++) {
		sum = sum.add(getPrevBar(index, i).getMedian());
	    }

	    fastAvg = sum.divide(BigDecimal.valueOf(fastLength), 2, RoundingMode.HALF_UP);
	}

	if (index > slowLength) {
	    BigDecimal sum = BigDecimal.ZERO;

	    for (int i = 0; i < slowLength; i++) {
		sum = sum.add(getPrevBar(index, i).getMedian());
	    }

	    slowAvg = sum.divide(BigDecimal.valueOf(slowLength), 2, RoundingMode.HALF_UP);
	}

	return fastAvg.subtract(slowAvg).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public Bar get(int i) {
	return barList.get(i);
    }

    public List<LocalDateTime> getDateList() {
	return dateList;
    }

    public Bar getPrevBar(int index, int length) {
	Bar bar = null;

	if (index > length) {
	    bar = barList.get(index - length);
	}

	return bar;
    }

    public boolean isEmpty() {
	return barList.isEmpty();
    }

    public int size() {
	return barList.size();
    }
}
