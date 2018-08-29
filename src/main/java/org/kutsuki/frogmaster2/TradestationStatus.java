package org.kutsuki.frogmaster2;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradestationStatus {
    private static final BigDecimal SIXTY = new BigDecimal(60);
    private static final BigDecimal THOUSAND = new BigDecimal(1000);
    private static final BigDecimal TWENTY_FOUR = new BigDecimal(24);
    private static final long PERIOD = 60 * 1000;

    private long completed;
    private long trials;
    private long start;
    private long timeout;

    public TradestationStatus(long trials) {
	this.trials = trials;
	this.completed = 0;
	this.start = System.currentTimeMillis();
	timeout = start + PERIOD;
    }

    public void complete() {
	this.completed++;

	if (System.currentTimeMillis() > timeout) {
	    printStatus();
	    timeout = System.currentTimeMillis() + PERIOD;
	}
    }

    private String formatTime(long ms) {
	StringBuilder sb = new StringBuilder();

	BigDecimal bd = new BigDecimal(ms);
	BigDecimal days = bd.divide(THOUSAND.multiply(SIXTY).multiply(SIXTY).multiply(TWENTY_FOUR), 0,
		RoundingMode.FLOOR);
	BigDecimal hours = bd.divide(THOUSAND.multiply(SIXTY).multiply(SIXTY), 0, RoundingMode.FLOOR)
		.remainder(TWENTY_FOUR);

	boolean isDay = days.compareTo(BigDecimal.ZERO) == 1;
	boolean isHour = hours.compareTo(BigDecimal.ZERO) == 1;

	if (isDay) {
	    sb.append(days).append('d').append(' ');
	}

	if (isHour) {
	    sb.append(hours).append('h').append(' ');
	}

	if (!isDay) {
	    BigDecimal minutes = bd.divide(THOUSAND.multiply(SIXTY), 0, RoundingMode.FLOOR).remainder(SIXTY);
	    sb.append(minutes).append('m').append(' ');
	}

	if (!isDay && !isHour) {
	    BigDecimal seconds = bd.divide(THOUSAND, 0, RoundingMode.HALF_UP).remainder(SIXTY);
	    sb.append(seconds).append('s');
	}

	return sb.toString();
    }

    private void printStatus() {
	BigDecimal elapsedTime = BigDecimal.valueOf(System.currentTimeMillis() - start);
	BigDecimal rate = BigDecimal.valueOf(completed).divide(elapsedTime, 4, RoundingMode.HALF_UP).multiply(THOUSAND)
		.setScale(0, RoundingMode.HALF_UP);

	if (rate.compareTo(BigDecimal.ZERO) == 1) {
	    BigDecimal remainingTime = elapsedTime.multiply(BigDecimal.valueOf(trials))
		    .divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP).subtract(elapsedTime);

	    StringBuilder sb = new StringBuilder();
	    sb.append("Completed: ").append(completed);
	    sb.append(", Rate: ").append(rate).append("t/s");
	    sb.append(", Time Left: ").append(formatTime(remainingTime.longValue()));
	    System.out.println(sb.toString());
	} else {
	    System.out.println("Completed: " + completed + ", Rate: ?, Time Left: ?");
	}
    }
}
