package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Ticker {
    private char month;
    private int year;

    private BigDecimal equity;
    private BigDecimal numContracts;
    private BigDecimal numContractsBar;
    private BigDecimal running;
    private BigDecimal realized;
    private BigDecimal runningBar;
    private LocalDateTime equityDateTime;

    public Ticker(char month, int year) {
	this.month = month;
	this.year = year;

	this.equity = null;
	this.numContracts = Tradestation.STARTING_BANKROLL.divide(Tradestation.COST_PER_CONTRACT, 0,
		RoundingMode.FLOOR);
	this.numContractsBar = Tradestation.STARTING_BANKROLL.divide(Tradestation.COST_PER_CONTRACT, 0,
		RoundingMode.FLOOR);
	this.realized = null;
	this.running = Tradestation.STARTING_BANKROLL;
	this.runningBar = Tradestation.STARTING_BANKROLL;
	this.equityDateTime = null;
    }

    @Override
    public String toString() {
	return Ticker.getKey(getMonth(), getYear());
    }

    public static String getKey(char month, int year) {
	StringBuilder sb = new StringBuilder();

	sb.append(Tradestation.ES);
	sb.append(month);

	if (year < 10) {
	    sb.append(0);
	}
	sb.append(year);

	return sb.toString();
    }

    public int getFullYear() {
	int fullYear = -1;

	if (getYear() >= 97) {
	    fullYear = 1900 + getYear();
	} else {
	    fullYear = 2000 + getYear();
	}

	return fullYear;
    }

    public char getMonth() {
	return month;
    }

    public int getYear() {
	return year;
    }

    public BigDecimal getEquity() {
	return equity;
    }

    public void setEquity(BigDecimal equity) {
	this.equity = equity;
    }

    public BigDecimal getNumContracts() {
	return numContracts;
    }

    public void setNumContracts(BigDecimal numContracts) {
	this.numContracts = numContracts;
    }

    public BigDecimal getNumContractsBar() {
	return numContractsBar;
    }

    public void setNumContractsBar(BigDecimal numContractsBar) {
	this.numContractsBar = numContractsBar;
    }

    public BigDecimal getRunning() {
	return running;
    }

    public void setRunning(BigDecimal running) {
	this.running = running;
    }

    public BigDecimal getRealized() {
	return realized;
    }

    public void setRealized(BigDecimal realized) {
	this.realized = realized;
    }

    public BigDecimal getRunningBar() {
	return runningBar;
    }

    public void setRunningBar(BigDecimal runningBar) {
	this.runningBar = runningBar;
    }

    public LocalDateTime getEquityDateTime() {
	return equityDateTime;
    }

    public void setEquityDateTime(LocalDateTime equityDateTime) {
	this.equityDateTime = equityDateTime;
    }
}
