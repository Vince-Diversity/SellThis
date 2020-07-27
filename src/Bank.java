import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

/**
 * Where the yield goes.
 */
public class Bank {

    public YieldPack countYield(LinkedList<BigDecimal> sellHistory,
                                LinkedList<BigDecimal> valueHistory) {
        BigDecimal[] yields = new BigDecimal[sellHistory.size()];
        BigDecimal counter = new BigDecimal("0.00");
        for (int i=0; i<sellHistory.size(); i++) {
            BigDecimal sold = sellHistory.get(i);
            BigDecimal value = valueHistory.get(i);
            BigDecimal yield = value.multiply(sold).setScale(value.scale(), RoundingMode.HALF_UP);
            yields[i] = yield;
            counter = counter.add(yield);
        }
        BigDecimal total = counter;
        return new YieldPack(yields, total);
    }

    public BigDecimal naiveYield(BigDecimal original, LinkedList<BigDecimal> valueHist) {
        BigDecimal value = valueHist.getLast();
        return original.multiply(value).setScale(value.scale(), RoundingMode.HALF_UP);
    }

}

class YieldPack {
    private final BigDecimal[] yields;
    private final BigDecimal total;
    public YieldPack(BigDecimal[] yields, BigDecimal total) {
        this.yields = yields;
        this.total = total;
    }
    public BigDecimal[] getYields() {
        return yields;
    }
    public BigDecimal getTotal() {
        return total;
    }
}
