import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

public class SellStocks {

    private int attempts;
    private BigDecimal owned;
    private final BigDecimal original;
    private final Limit limit;
    private final ValueSim sim;
    private final LinkedList<BigDecimal> sellHistory;
    private final LinkedList<BigDecimal> valueHistory;

    public SellStocks(int attempts, BigDecimal owned, Limit limit, ValueSim sim) {
        this.attempts = attempts;
        this.owned = owned;
        this.original = owned;
        this.limit = limit;
        this.sim = sim;
        this.sellHistory = new LinkedList<>();
        this.valueHistory = new LinkedList<>();
    }

    /**
     * Yields proportion of owned
     */
    public BigDecimal today(BigDecimal value) {
        switch (limit) {
            case BOUNDED: return boundedRate(value);
            case FLEX: return flexRate(value);
            default: return new BigDecimal("-1");
        }
    }

    /**
     * Test result: only satisfies the 'total' test if value is mean and correctly predicted.
     */
    private BigDecimal boundedRate(BigDecimal value) {
        BigDecimal diff = sim.boundedMean(value);
        BigDecimal future = diff.multiply(new BigDecimal(attempts));
        BigDecimal denominator = value.add(future);
        BigDecimal proportion = value.divide(denominator, RoundingMode.HALF_UP);
        return proportion.multiply(original).setScale(value.scale(), RoundingMode.HALF_UP);
    }

    /**
     * Check today's amount to sell.
     * @return today's sold amount.
     */
    private BigDecimal flexRate(BigDecimal value) {
        BigDecimal average = sim.flexMean();
        BigDecimal future = average.multiply(new BigDecimal(attempts));
        BigDecimal denominator = value.add(future);
        BigDecimal proportion = value.divide(denominator, RoundingMode.HALF_UP);
        return proportion.multiply(owned).setScale(value.scale(), RoundingMode.HALF_UP);
    }

    /**
     * Confirms today's deal, moves on to the next deal.
     */
    public void sell(BigDecimal yield, BigDecimal value) {
        sellHistory.add(yield);
        owned = owned.subtract(yield);
        if (!limit.equals(Limit.BOUNDED)) {
            attempts--;
        }
        valueHistory.add(value);
    }

    public BigDecimal getOwned() { return owned; }

    public LinkedList<BigDecimal> getSellHistory() { return sellHistory; }

    public LinkedList<BigDecimal> getValueHistory() {return valueHistory; }
}
