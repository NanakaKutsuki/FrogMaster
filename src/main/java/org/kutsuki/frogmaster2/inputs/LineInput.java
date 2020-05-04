package org.kutsuki.frogmaster2.inputs;

public class LineInput extends AbstractInput {
    private int coreLine;
    private int ahLine;
    private int onLine;

    public LineInput(int coreLine, int ahLine, int onLine) {
	this.coreLine = coreLine;
	this.ahLine = ahLine;
	this.onLine = onLine;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");
	sb.append(getCoreLine()).append(',').append(' ');
	sb.append(getAhLine()).append(',').append(' ');
	sb.append(getOnLine());
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
}
