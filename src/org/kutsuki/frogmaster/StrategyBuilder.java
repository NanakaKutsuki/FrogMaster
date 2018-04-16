package org.kutsuki.frogmaster;

public class StrategyBuilder {
    public void printBuy() {
	for (int i = 0; i < 100; i++) {
	    System.out.println("\tIF cc < amt AND cc = " + i + " THEN BEGIN");
	    System.out.println("\t\tBUY(\"L" + (i + 1) + "\") 1 CONTRACT NEXT BAR AT MARKET;");
	    System.out.println("\t\tcc += 1;");
	    System.out.println("\tEND;");
	    System.out.println("");

	}
    }

    public void printShort() {
	for (int i = 0; i < 100; i++) {
	    System.out.println("\tIF cc < amt AND cc = " + i + " THEN BEGIN");
	    System.out.println("\t\tSELLSHORT(\"SS" + (i + 1) + "\") 1 CONTRACT NEXT BAR AT MARKET;");
	    System.out.println("\t\tcc += 1;");
	    System.out.println("\tEND;");
	    System.out.println("");
	}
    }

    public void printSell() {
	for (int i = 100; i > 0; i--) {
	    System.out.println("\tIF cc > amt AND cc = " + i + " THEN BEGIN");
	    System.out.println("\t\tSELL(\"S" + i + "\") FROM ENTRY(\"L" + i + "\") 1 CONTRACT NEXT BAR AT MARKET;");
	    System.out.println("\t\tcc -= 1;");
	    System.out.println("\tEND;");
	    System.out.println("");
	}
    }

    public void printCover() {
	for (int i = 100; i > 0; i--) {
	    System.out.println("\tIF cc > amt AND cc = " + i + " THEN BEGIN");
	    System.out.println(
		    "\t\tBUYTOCOVER(\"C" + i + "\") FROM ENTRY(\"SS" + i + "\") 1 CONTRACT NEXT BAR AT MARKET;");
	    System.out.println("\t\tcc -= 1;");
	    System.out.println("\tEND;");
	    System.out.println("");
	}
    }

    public static void main(String[] args) {
	StrategyBuilder sb = new StrategyBuilder();
	sb.printSell();
    }
}
