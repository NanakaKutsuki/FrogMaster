package org.kutsuki.frogmaster.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.text.StrBuilder;

public class TpoModel {
    private String symbol;
    private LocalDate date;
    private char letter;
    private BigDecimal price;
    private int volume;

    @Override
    public String toString() {
        StrBuilder sb = new StrBuilder();
        sb.append(getSymbol()).append(',').append(' ');
        sb.append(getDate()).append(',').append(' ');
        sb.append(getLetter()).append(',').append(' ');
        sb.append(getPrice()).append(',').append(' ');
        sb.append(getVolume());

        return sb.toString();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
