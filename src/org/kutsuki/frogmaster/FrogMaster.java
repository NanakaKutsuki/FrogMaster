package org.kutsuki.frogmaster;

import java.time.LocalDate;

import org.kutsuki.frogmaster.model.ProfileModel;

// https://github.com/NanakaKutsuki/FrogMaster.git
public class FrogMaster {
    // private final Logger logger = LoggerFactory.getLogger(FrogMaster.class);

    public void run() {
        TradeParser parser = new TradeParser();
        parser.parse();

        LocalDate key = LocalDate.of(2013, 9, 3);
        ProfileModel profile = parser.getProfile("ESU13", key);
        int maxSize = profile.getMaxSize();

        HtmlOutputter out = new HtmlOutputter(profile);
        out.output();

        // System.out.println(profile.getHighValuePrice());
        // System.out.println(profile.getLowValuePrice());
        //
        // for (Entry<BigDecimal, List<Character>> entry :
        // profile.getLetterMap().entrySet()) {
        // List<Character> letterList = entry.getValue();
        //
        // StrBuilder sb = new StrBuilder();
        // for (int i = 0; i < maxSize; i++) {
        // if (i < letterList.size()) {
        // sb.append(letterList.get(i));
        // } else {
        // sb.append(' ');
        // }
        // }
        // sb.append(' ');
        // sb.append(' ');
        // sb.append(entry.getKey());
        // sb.append(' ');
        // sb.append(' ');
        // sb.append(profile.getPriceVolumeMap().get(entry.getKey()));
        //
        // System.out.println(sb.toString());
        // }
        // System.out.println(key.toString());
    }

    public static void main(String[] args) {
        FrogMaster frogMaster = new FrogMaster();
        frogMaster.run();
    }
}
