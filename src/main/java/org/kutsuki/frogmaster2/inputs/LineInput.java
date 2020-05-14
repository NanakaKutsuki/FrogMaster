package org.kutsuki.frogmaster2.inputs;

public class LineInput extends AbstractInput {
    private int coreLine;
    private int ahLine;
    private int onLine;
    private int ahEscape;
    private int onEscape;

    public LineInput(int coreLine, int ahLine, int onLine) {
	this.coreLine = coreLine;
	this.ahLine = ahLine;
	this.onLine = onLine;
    }

    public LineInput(int coreLine, int ahLine, int onLine, int ahEscape, int onEscape) {
	this.coreLine = coreLine;
	this.ahLine = ahLine;
	this.onLine = onLine;
	this.ahEscape = ahEscape;
	this.onEscape = onEscape;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");
	sb.append(getCoreLine()).append(',').append(' ');
	sb.append(getAhLine()).append(',').append(' ');
	sb.append(getOnLine()).append(',').append(' ');
	sb.append(getAhEscape()).append(',').append(' ');
	sb.append(getOnEscape());
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

    public int getAhEscape() {
	return ahEscape;
    }

    public int getOnEscape() {
	return onEscape;
    }
}
