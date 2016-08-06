package org.kutsuki.frogmaster.outputter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.StrBuilder;
import org.kutsuki.frogmaster.model.ProfileModel;

public class StringOutputter extends AbstractOutputter {

    public StringOutputter(Map<LocalDate, ProfileModel> profileMap) {
        super(profileMap);
    }

    @Override
    public void output() {
        LocalDate key = LocalDate.of(2013, 9, 3);

        System.out.println(getProfileMap().get(key).getHighValuePrice());
        System.out.println(getProfileMap().get(key).getLowValuePrice());

        int maxSize = 0;
        for (Entry<BigDecimal, List<Character>> entry : getProfileMap().get(key).getLetterMap().entrySet()) {
            if (entry.getValue().size() > maxSize) {
                maxSize = entry.getValue().size();
            }
        }

        for (Entry<BigDecimal, List<Character>> entry : getProfileMap().get(key).getLetterMap().entrySet()) {
            List<Character> letterList = entry.getValue();

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
            sb.append(format(entry.getKey()));

            System.out.println(sb.toString());
        }
        System.out.println(key.toString());
    }
}
