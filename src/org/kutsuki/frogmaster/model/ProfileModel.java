package org.kutsuki.frogmaster.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileModel {
    private static final BigDecimal SEVEN = new BigDecimal(0.7);

    private boolean calculated;
    private boolean poorHigh;
    private boolean poorLow;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal openPrice;
    private BigDecimal pocPrice;
    private BigDecimal highValuePrice;
    private BigDecimal lowValuePrice;
    private int totalVolume;
    private List<TpoModel> tpoList;
    private Map<BigDecimal, List<Character>> letterMap;

    public ProfileModel() {
	this.calculated = false;
	this.closePrice = BigDecimal.ZERO;
	this.highPrice = BigDecimal.ZERO;
	this.highValuePrice = BigDecimal.ZERO;
	this.lowPrice = new BigDecimal(1000000);
	this.lowValuePrice = new BigDecimal(1000000);
	this.letterMap = new HashMap<BigDecimal, List<Character>>();
	this.openPrice = BigDecimal.ZERO;
	this.pocPrice = BigDecimal.ZERO;
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
	}
    }

    public void calculateValueArea() {
	if (!calculated) {
	    Map<BigDecimal, Integer> priceVolumeMap = new HashMap<BigDecimal, Integer>();

	    // find target volume, 70% of total volume
	    int targetVolume = BigDecimal.valueOf(totalVolume).multiply(SEVEN).intValue();

	    int maxVolume = 0;
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
		    pocPrice = tpo.getPrice();
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
		if (pocPrice.compareTo(priceList.get(i)) == 0) {
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

	    // calculate open and close
	    if (!tpoList.isEmpty()) {
		Collections.sort(tpoList, new Comparator<TpoModel>() {
		    @Override
		    public int compare(TpoModel lhs, TpoModel rhs) {
			return lhs.getTime().compareTo(rhs.getTime());
		    }
		});

		openPrice = tpoList.get(0).getPrice();
		closePrice = tpoList.get(tpoList.size() - 1).getPrice();
	    }

	    // calculate poors
	    if (priceList.size() > 1) {
		highPrice = priceList.get(priceList.size() - 1);
		BigDecimal highPrice2 = priceList.get(priceList.size() - 2);

		if (letterMap.get(highPrice).size() == 1 && letterMap.get(highPrice2).size() > 1) {
		    poorHigh = true;
		}

		lowPrice = priceList.get(0);
		BigDecimal lowPrice2 = priceList.get(1);

		if (letterMap.get(lowPrice).size() == 1 && letterMap.get(lowPrice2).size() > 1) {
		    poorLow = true;
		}
	    }

	    calculated = true;
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

    public BigDecimal getClosePrice() {
	calculateValueArea();
	return closePrice;
    }

    public BigDecimal getHighPrice() {
	calculateValueArea();
	return highPrice;
    }

    public BigDecimal getHighValuePrice() {
	calculateValueArea();
	return highValuePrice;
    }

    public Map<BigDecimal, List<Character>> getLetterMap() {
	return letterMap;
    }

    public BigDecimal getLowPrice() {
	calculateValueArea();
	return lowPrice;
    }

    public BigDecimal getLowValuePrice() {
	calculateValueArea();
	return lowValuePrice;
    }

    public BigDecimal getOpenPrice() {
	calculateValueArea();
	return openPrice;
    }

    public BigDecimal getPocPrice() {
	calculateValueArea();
	return pocPrice;
    }

    public List<TpoModel> getTpoList() {
	return tpoList;
    }

    public boolean isPoorHigh() {
	return poorHigh;
    }

    public boolean isPoorLow() {
	return poorLow;
    }
}
