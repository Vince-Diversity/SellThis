import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simulates the value of stocks over time.
 */
public class ValueSim {

    private final BigDecimal bot;
    private final BigDecimal top;

    /**
     * Constructor for using the arithmetic mean prediction.
     */
    public ValueSim(BigDecimal bot, BigDecimal top) {
        this.bot = bot;
        this.top = top;
    }

    public BigDecimal mean(BigDecimal[] xx) {
        BigDecimal sum = new BigDecimal("0.00");
        for (BigDecimal x : xx) {
            sum = sum.add(x);
        }
        return sum.divide(BigDecimal.valueOf(xx.length), RoundingMode.HALF_UP);
    }

    /**
     * Only works with bounded case.
     */
    public BigDecimal boundedMean(BigDecimal value) {
        return top.subtract(value);
    }

    /**
     * Mean from uniform distribution around bot and top
     */
    public BigDecimal flexMean() {
        return bot.add(top.subtract(bot).divide(new BigDecimal(2), RoundingMode.HALF_UP));
    }
}