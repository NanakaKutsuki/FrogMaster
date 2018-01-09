package org.kutsuki.frogmaster;

import java.math.BigDecimal;

public class Tradestation {
    public static final BigDecimal COMMISSION = new BigDecimal("5.38");
    public static final BigDecimal COST_PER_CONTRACT = new BigDecimal("15000");
    public static final BigDecimal SLIPPAGE = new BigDecimal("0.50");
    public static final BigDecimal STARTING_BANKROLL = new BigDecimal("100000");
    public static final String ES = "ES";

    private Tradestation() {
	// private constructor
    }
}
