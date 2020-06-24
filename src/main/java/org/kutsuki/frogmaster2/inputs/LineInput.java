package org.kutsuki.frogmaster2.inputs;

import java.time.LocalTime;

public class LineInput extends AbstractInput {
    private int coreLine;
    private int ahLine;
    private int onLine;
    private LocalTime coreTime;

    public LineInput(int coreLine, int ahLine, int onLine) {
	this.coreLine = coreLine;
	this.ahLine = ahLine;
	this.onLine = onLine;
	this.coreTime = null;
    }

    public LineInput(int coreLine, int ahLine, int onLine, LocalTime coreTime) {
	this.coreLine = coreLine;
	this.ahLine = ahLine;
	this.onLine = onLine;
	this.coreTime = coreTime;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");
	sb.append(getCoreLine()).append(',').append(' ');
	sb.append(getAhLine()).append(',').append(' ');
	sb.append(getOnLine());

	if (coreTime != null) {
	    sb.append(',').append(' ');
	    sb.append(getCoreTime());
	}

	sb.append(')');
	return sb.toString();
    }

    public int getCoreLine() {
	return coreLine;
    }

    public int getAhLine() {
	return ahLine;
    }

    public int getOnLine() {
	return onLine;
    }

    public LocalTime getCoreTime() {
	return coreTime;
    }
}
