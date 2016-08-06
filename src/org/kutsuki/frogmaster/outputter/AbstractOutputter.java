package org.kutsuki.frogmaster.outputter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Map;

import org.kutsuki.frogmaster.model.ProfileModel;

public abstract class AbstractOutputter {
    private static final NumberFormat NF = NumberFormat.getCurrencyInstance();

    private Map<LocalDate, ProfileModel> profileMap;

    public abstract void output();

    public AbstractOutputter(Map<LocalDate, ProfileModel> profileMap) {
        this.profileMap = profileMap;
    }

    public String format(BigDecimal bd) {
        return NF.format(bd);
    }

    public Map<LocalDate, ProfileModel> getProfileMap() {
        return profileMap;
    }
}
