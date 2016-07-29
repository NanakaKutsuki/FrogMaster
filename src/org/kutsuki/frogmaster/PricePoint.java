package org.kutsuki.frogmaster;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.text.StrBuilder;

public class PricePoint implements Comparable<PricePoint> {
    private String symbol;
    private LocalDate date;
    private LocalTime time;
    private char letter;
    private BigDecimal price;

    @Override
    public int compareTo(PricePoint rhs) {
        CompareToBuilder ctb = new CompareToBuilder();
        ctb.append(getSymbol(), rhs.getSymbol());
        ctb.append(getDate(), rhs.getDate());
        ctb.append(getPrice(), rhs.getPrice());
        ctb.append(getLetter(), rhs.getLetter());
        return ctb.toComparison();
    }

    @Override
    public String toString() {
        StrBuilder sb = new StrBuilder();
        sb.append(getSymbol()).append(',').append(' ');
        sb.append(getDate()).append('T');
        sb.append(getTime()).append(',').append(' ');
        sb.append(getPrice());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        // do not compare time
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(getSymbol());
        hcb.append(getDate());
        hcb.append(getLetter());
        hcb.append(getPrice());
        return hcb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;

        if (obj == null) {
            equals = false;
        } else if (obj == this) {
            equals = true;
        } else if (obj.getClass() != getClass()) {
            equals = false;
        } else {
            // do not compare time
            PricePoint rhs = (PricePoint) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(getSymbol(), rhs.getSymbol());
            eb.append(getDate(), rhs.getDate());
            eb.append(getLetter(), rhs.getLetter());
            eb.append(getPrice(), rhs.getPrice());
            equals = eb.isEquals();
        }

        return equals;
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

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
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
}
