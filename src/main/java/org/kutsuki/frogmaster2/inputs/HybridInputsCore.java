package org.kutsuki.frogmaster2.inputs;

public class HybridInputsCore {
    private final static Input INPUT;

    static {
	INPUT = new Input(-600, -50, 575, 1000);
    }

    private HybridInputsCore() {
	// private constructor
    }

    public static Input getInput() {
	return INPUT;
    }
}
