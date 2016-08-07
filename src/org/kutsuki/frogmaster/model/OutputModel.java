package org.kutsuki.frogmaster.model;

public class OutputModel {
    private boolean close;
    private boolean poor;
    private boolean value;
    private String output;

    public OutputModel() {
        this.close = false;
        this.poor = false;
        this.value = false;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public boolean isPoor() {
        return poor;
    }

    public void setPoor(boolean poor) {
        this.poor = poor;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

}
