import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Tells how much to sell day-by-day with respect to (current market) value by computing
 *
 *        value
 * ------------------- * remaining possession
 * value + future_value
 *
 * where future_value = selling_days_remaining * future_value_prediction.
 * When selling_days_remaining = 0, anything remaining is sold. Should behave smoothly though.
 *
 * Actually, the real idea is this:
 *
 *      v * a^q
 * -------------------- * remaining possession
 * v * a^q + (r^p * f)
 *
 * where p and q are risk parameters:
 * A higher q makes this seller more sensitive to deviations from prediction.
 * A higher p makes it more confident to wait on selling due to a positive outlook.
 * a is the ratio between current value and predicted current value.
 * Safest seller does q = 0 and p = 1.
 * Only the a^q is implemented for having somewhat use.
 */
public class SellStocks {

    private int attempts;
    private BigDecimal owned;
    private final BigDecimal original;
    private final ValueSim sim;
    private final LinkedList<BigDecimal> sellHistory;
    private final LinkedList<BigDecimal> valueHistory;
    private Function<BigDecimal,BigDecimal> evaluate;
    private BiConsumer<BigDecimal,BigDecimal> proceed;
    private double prediction = 0.;
    private final double p;

    public SellStocks(BigDecimal owned, Limit limit, ValueSim sim, double pParam) {
        attempts = sim.getValSize() - 1;
        this.owned = owned;
        original = owned;
        this.sim = sim;
        this.sellHistory = new LinkedList<>();
        this.valueHistory = new LinkedList<>();
        switch (limit) {
            case BOUNDED:
                evaluate = this::boundedRate;
                proceed = this::sellBounded;
                prediction = sim.boundedMean();
                break;
            case FLEX:
                evaluate = this::flexRate;
                proceed = this::sellFlex;
                break;
        }
        p = pParam;
    }

    /**
     * Yields proportion of owned
     */
    public BigDecimal today(BigDecimal value) {
        return evaluate.apply(value);
    }

    /**
     * Test result: only satisfies the 'total' test if value is mean and correctly predicted.
     */
    private BigDecimal boundedRate(BigDecimal val) {
        double value = val.doubleValue();
        double future = prediction * attempts;
        double denominator = value+future;
        double proportionD = value/denominator;
        BigDecimal proportion = BigDecimal.valueOf(proportionD).setScale(2, RoundingMode.HALF_UP);
        return proportion.multiply(original).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check today's amount to sell.
     * @return today's sold amount.
     */
    private BigDecimal flexRate(BigDecimal val) {
        double value = val.doubleValue();
        prediction = sim.lineMean(attempts);
        double future = prediction*attempts;
        double expectedValue = sim.getLine().getCurrent();
        double valueRatio = value/expectedValue;
        valueRatio = Math.pow(valueRatio,p);
        double weightedValue = value*valueRatio;
        double denominator = weightedValue + future;
        double proportion = weightedValue/denominator;
        return BigDecimal.valueOf(proportion*owned.doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Confirms today's deal, moves on to the next deal.
     */
    public void sell(BigDecimal yield, BigDecimal value) {
        proceed.accept(yield,value);
    }

    private void sellBounded(BigDecimal yield, BigDecimal value) {
        sellHelper(yield, value);
    }

    private void sellFlex(BigDecimal yield, BigDecimal value) {
        sellHelper(yield, value);
        attempts--;
    }

    private void sellHelper(BigDecimal yield, BigDecimal value) {
        sellHistory.add(yield);
        owned = owned.subtract(yield);
        valueHistory.add(value);
    }

    public BigDecimal getOwned() { return owned; }

    public LinkedList<BigDecimal> getSellHistory() { return sellHistory; }

    public LinkedList<BigDecimal> getValueHistory() {return valueHistory; }

    public ValueSim getSim() {
        return sim;
    }

    public int getAttempts() {
        return attempts;
    }
}
