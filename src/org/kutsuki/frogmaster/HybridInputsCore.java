package org.kutsuki.frogmaster;

public class HybridInputsCore {
    private final static Input INPUT;

    static {
	INPUT = new Input("-6.0", "-0.5", "5.75", "10");
	// INPUT = new Input("-6.25", "-1.5", "9", "10");
    }

    private HybridInputsCore() {
	// private constructor
    }

    public static Input getInput() {
	return INPUT;
    }
}
