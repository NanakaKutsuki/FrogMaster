package org.kutsuki.frogmaster.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ProfileModel {
    private static final BigDecimal SEVEN = new BigDecimal(0.7);

    private BigDecimal highValuePrice;
    private BigDecimal lowValuePrice;
    private int totalVolume;
    private List<TpoModel> tpoList;
    private LocalDate date;
    private Map<BigDecimal, List<Character>> letterMap;
    private String symbol;

    // TODO remove
    private int maxSize = 0;
    private Map<BigDecimal, Integer> priceVolumeMap = new HashMap<BigDecimal, Integer>();

    public ProfileModel(String symbol, LocalDate date) {
        this.symbol = symbol;
        this.date = date;

        this.highValuePrice = BigDecimal.ZERO;
        this.lowValuePrice = new BigDecimal(1000000);
        this.letterMap = new TreeMap<BigDecimal, List<Character>>(Collections.reverseOrder());
        this.totalVolume = 0;
        this.tpoList = new ArrayList<TpoModel>();
    }

    public void addTpo(TpoModel tpo) {
        totalVolume += tpo.getVolume();
        tpoList.add(tpo);

        List<Character> letterList = letterMap.get(tpo.getPrice());
        if (letterList == null) {
            letterList = new ArrayList<Character>();
        }

        if (!letterList.contains(tpo.getLetter())) {
            letterList.add(tpo.getLetter());
            letterMap.put(tpo.getPrice(), letterList);

            // TODO remove
            if (letterList.size() > maxSize) {
                maxSize = letterList.size();
            }
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getHighValuePrice() {
        if (highValuePrice.compareTo(BigDecimal.ZERO) == 0) {
            calculateValueArea();
        }

        return highValuePrice;
    }

    public Map<BigDecimal, List<Character>> getLetterMap() {
        return letterMap;
    }

    public BigDecimal getLowValuePrice() {
        if (highValuePrice.compareTo(BigDecimal.ZERO) == 0) {
            calculateValueArea();
        }

        return lowValuePrice;
    }

    public Map<BigDecimal, Integer> getPriceVolumeMap() {
        return priceVolumeMap;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public String getSymbol() {
        return symbol;
    }

    private void calculateValueArea() {
        // find target volume, 70% of total volume
        int targetVolume = BigDecimal.valueOf(totalVolume).multiply(SEVEN).intValue();

        int maxVolume = 0;
        BigDecimal poc = BigDecimal.ZERO;
        // Map<BigDecimal, Integer> priceVolumeMap = new HashMap<BigDecimal,
        // Integer>();
        for (TpoModel tpo : tpoList) {
            // find by price
            Integer volume = priceVolumeMap.get(tpo.getPrice());

            // set volume to 0 if no volume was found.
            if (volume == null) {
                volume = 0;
            }

            // add volume
            volume += tpo.getVolume();

            // check max volume
            if (volume > maxVolume) {
                maxVolume = volume;
                poc = tpo.getPrice();
            }

            // put into price volume map
            priceVolumeMap.put(tpo.getPrice(), volume);
        }

        // find starting index
        List<BigDecimal> priceList = new ArrayList<BigDecimal>(priceVolumeMap.keySet());
        Collections.sort(priceList);

        int i = 0;
        int pocIndex = -1;
        while (pocIndex == -1 && i < priceList.size()) {
            if (poc.compareTo(priceList.get(i)) == 0) {
                pocIndex = i;
            }

            i++;
        }

        // calculate value area
        int volume = maxVolume;
        Set<Integer> indexSet = new HashSet<Integer>();
        indexSet.add(pocIndex);
        while (volume < targetVolume) {
            Integer highIndex = findHighIndex(pocIndex, priceList.size(), indexSet);
            Integer lowIndex = findLowIndex(pocIndex, indexSet);

            if (highIndex == null && lowIndex == null) {
                // shouldn't happen
                volume = totalVolume;
            } else if (highIndex != null && lowIndex == null) {
                int highIndex2 = highIndex - 1;
                indexSet.add(highIndex);
                indexSet.add(highIndex2);

                BigDecimal highPrice = priceList.get(highIndex);
                BigDecimal highPrice2 = priceList.get(highIndex2);
                volume += priceVolumeMap.get(highPrice);
                volume += priceVolumeMap.get(highPrice2);

                if (highPrice.compareTo(highValuePrice) == 1) {
                    highValuePrice = highPrice;
                }
            } else if (highIndex == null && lowIndex != null) {
                int lowIndex2 = lowIndex + 1;
                indexSet.add(lowIndex);
                indexSet.add(lowIndex2);

                BigDecimal lowPrice = priceList.get(lowIndex);
                BigDecimal lowPrice2 = priceList.get(lowIndex2);
                volume += priceVolumeMap.get(lowPrice);
                volume += priceVolumeMap.get(lowPrice2);

                if (lowPrice.compareTo(lowValuePrice) == -1) {
                    lowValuePrice = lowPrice;
                }
            } else if (highIndex != null && lowIndex != null) {
                int highIndex2 = highIndex - 1;
                BigDecimal highPrice = priceList.get(highIndex);
                BigDecimal highPrice2 = priceList.get(highIndex2);
                int highVolume = priceVolumeMap.get(highPrice) + priceVolumeMap.get(highPrice2);

                int lowIndex2 = lowIndex + 1;
                BigDecimal lowPrice = priceList.get(lowIndex);
                BigDecimal lowPrice2 = priceList.get(lowIndex2);
                int lowVolume = priceVolumeMap.get(lowPrice) + priceVolumeMap.get(lowPrice2);

                if (highVolume > lowVolume) {
                    indexSet.add(highIndex);
                    indexSet.add(highIndex2);
                    volume += highVolume;

                    if (highPrice.compareTo(highValuePrice) == 1) {
                        highValuePrice = highPrice;
                    }
                } else if (highVolume < lowVolume) {
                    indexSet.add(lowIndex);
                    indexSet.add(lowIndex2);
                    volume += lowVolume;

                    if (lowPrice.compareTo(lowValuePrice) == -1) {
                        lowValuePrice = lowPrice;
                    }
                } else {
                    indexSet.add(highIndex);
                    indexSet.add(highIndex2);
                    volume += highVolume;

                    if (highPrice.compareTo(highValuePrice) == 1) {
                        highValuePrice = highPrice;
                    }

                    indexSet.add(lowIndex);
                    indexSet.add(lowIndex2);
                    volume += lowVolume;

                    if (lowPrice.compareTo(lowValuePrice) == -1) {
                        lowValuePrice = lowPrice;
                    }
                }
            }
        }
    }

    private Integer findHighIndex(int pocIndex, int priceSize, Set<Integer> indexSet) {
        Integer target = null;

        int i = pocIndex;
        while (indexSet.contains(i)) {
            i += 2;
        }

        if (i < priceSize) {
            target = i;
        }

        return target;
    }

    private Integer findLowIndex(int pocIndex, Set<Integer> indexSet) {
        Integer target = null;

        int i = pocIndex;
        while (indexSet.contains(i)) {
            i -= 2;
        }

        if (i > -1) {
            target = i;
        }

        return target;
    }
}
