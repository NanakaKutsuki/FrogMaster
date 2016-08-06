package org.kutsuki.frogmaster;

import org.kutsuki.frogmaster.outputter.AbstractOutputter;
import org.kutsuki.frogmaster.outputter.HtmlOutputter;

// https://github.com/NanakaKutsuki/FrogMaster.git
public class FrogMaster {
    // private final Logger logger = LoggerFactory.getLogger(FrogMaster.class);

    public void run() {
        long ms = System.currentTimeMillis();
        TradeParser parser = new TradeParser();
        parser.parse();
        System.out.println("Parsing Done: " + (System.currentTimeMillis() - ms) + "ms");

        AbstractOutputter out = new HtmlOutputter(parser.getProfileMapBySymbol("ESU13"));
        out.output();

        System.out.println("Output Done: " + (System.currentTimeMillis() - ms) + "ms");
    }

    public static void main(String[] args) {
        FrogMaster frogMaster = new FrogMaster();
        frogMaster.run();
    }
}
