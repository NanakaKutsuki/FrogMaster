package org.kutsuki.frogmaster2.inputs;

import java.time.LocalTime;

public class TimeInput extends AbstractInput {
    private LocalTime long1;
    private LocalTime long2;
    private LocalTime short1;
    private LocalTime short2;

    public TimeInput(LocalTime long1, LocalTime long2, LocalTime short1, LocalTime short2) {
	this.long1 = long1;
	this.long2 = long2;
	this.short1 = short1;
	this.short2 = short2;
    }

    public TimeInput(String long1, String long2, String short1, String short2) {
	this.long1 = LocalTime.parse(long1);
	this.long2 = LocalTime.parse(long2);
	this.short1 = LocalTime.parse(short1);
	this.short2 = LocalTime.parse(short2);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Inputs: (");
	sb.append('"').append(getLong1()).append('"').append(',');
	sb.append('"').append(getLong2()).append('"').append(',');
	sb.append('"').append(getShort1()).append('"').append(',');
	sb.append('"').append(getShort2()).append('"');
	sb.append(')');
	return sb.toString();
    }

    public LocalTime getLong1() {
	return long1;
    }

    public LocalTime getLong2() {
	return long2;
    }

    public LocalTime getShort1() {
	return short1;
    }

    public LocalTime getShort2() {
	return short2;
    }
}
