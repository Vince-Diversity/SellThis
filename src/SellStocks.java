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
    private BigDecimal prediction = BigDecimal.ZERO;

    public SellStocks(BigDecimal owned, Limit limit, ValueSim sim) {
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
    private BigDecimal boundedRate(BigDecimal value) {
        BigDecimal future = prediction.multiply(new BigDecimal(attempts));
        BigDecimal denominator = value.add(future);
        BigDecimal proportion = value.divide(denominator, RoundingMode.HALF_UP);
        return proportion.multiply(original).setScale(value.scale(), RoundingMode.HALF_UP);
    }

    /**
     * Check today's amount to sell.
     * @return today's sold amount.
     */
    private BigDecimal flexRate(BigDecimal value) {
        prediction = sim.lineMean(attempts);
        BigDecimal future = prediction.multiply(new BigDecimal(attempts));
        BigDecimal denominator = value.add(future);
        BigDecimal proportion = value.divide(denominator, RoundingMode.HALF_UP);
        return proportion.multiply(owned).setScale(value.scale(), RoundingMode.HALF_UP);
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

    public BigDecimal getPrediction() {
        return prediction;
    }
}
