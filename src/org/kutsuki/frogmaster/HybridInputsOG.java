package org.kutsuki.frogmaster;

public class HybridInputsOG {
    private final static Input INPUT;

    static {
	// INPUT = new Input("-6.25", "-1.5", "8.25", "10");
	// INPUT = new Input("-6.25", "-1.5", "10.75", "9.25");
	INPUT = new Input("-6.25", "-1.5", "9.5", "10.5");
	// INPUT = new Input("-6.25", "-1.5", "10.25", "10.5");
    }

    private HybridInputsOG() {
	// private constructor
    }

    public static Input getInput() {
	return INPUT;
    }
}
