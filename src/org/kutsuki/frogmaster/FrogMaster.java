package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.text.StrBuilder;

public class FrogMaster {
    // private final Logger logger = LoggerFactory.getLogger(FrogMaster.class);

    public void run() {
        TradeParser parser = new TradeParser();
        Set<PricePoint> priceSet = parser.parse();

        int maxSize = 0;
        Map<BigDecimal, List<Character>> priceMap = new TreeMap<BigDecimal, List<Character>>(Collections.reverseOrder());
        for (PricePoint point : priceSet) {
            BigDecimal price = point.getPrice();
            List<Character> letterList = priceMap.get(price);
            if (letterList == null) {
                letterList = new ArrayList<Character>();
            }

            letterList.add(point.getLetter());
            priceMap.put(price, letterList);

            // find max size
            if (letterList.size() > maxSize) {
                maxSize = letterList.size();
            }
        }

        for (Entry<BigDecimal, List<Character>> entry : priceMap.entrySet()) {
            List<Character> letterList = entry.getValue();
            Collections.sort(letterList);

            StrBuilder sb = new StrBuilder();
            for (int i = 0; i < maxSize; i++) {
                if (i < letterList.size()) {
                    sb.append(letterList.get(i));
                } else {
                    sb.append(' ');
                }
            }
            sb.append(' ');
            sb.append(' ');
            sb.append(entry.getKey());

            System.out.println(sb.toString());
        }
        System.out.println("9/3");
    }

    public static void main(String[] args) {
        FrogMaster frogMaster = new FrogMaster();
        frogMaster.run();
    }
}
