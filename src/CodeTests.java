import org.junit.Test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Tests for meaningful method:
 * - 'total': all has been sold neither too early or too late.
 * - 'stability': if the value happens to be constant, no extremes.
 * - 'earn': method is better than selling day 1 or last day.
 *
 * Before and After run twice due to different import origins
 */
public class CodeTests {

    private final BigDecimal owned = new BigDecimal("100000.00");
    private final BigDecimal top = new BigDecimal("13.00");
    private final BigDecimal bot = new BigDecimal("9.00");
    private final int defaultSize = 20;
//    private final long defaultSeed = 400L;
    private final long defaultSeed = 400L;
    private final BigDecimal defaultValue = new BigDecimal("10.00");

    @Test
    public void reasonable() {
        int attempts = 9;
        BigDecimal[] values = fillValues(attempts);
        ValueSim sim = new ValueSim(BigDecimal.ZERO, defaultValue.multiply(new BigDecimal(2)), attempts+1);
        SellStocks selling = new SellStocks(owned, Limit.BOUNDED, sim, values);
        BigDecimal sellToday = selling.today(defaultValue);
        System.out.println("Bounded first sell reasonable? " + sellToday);
    }

    private SellStocks flexHelper(BigDecimal[] values, ValueSim sim) {
        SellStocks selling = new SellStocks(owned, Limit.FLEX, sim, values);
        for (int i=0; i<selling.getSim().getValSize(); i++) {
            BigDecimal value = values[i];
            BigDecimal sellToday = selling.today(value);
            selling.sell(sellToday, value);
        }
        return selling;
    }

    private BigDecimal[] fillValues(int attempts) {
        BigDecimal[] values = new BigDecimal[attempts];
        Arrays.fill(values, defaultValue);
        return values;
    }

    private SellStocks defaultFlexTest() {
        ValueSim sim = new ValueSim(bot, top, defaultSize);
        BigDecimal[] values = fillValues(sim.getValSize());
        return flexHelper(values, sim);
    }

    /**
     * In the end all should be spend.
     */
    @Test
    public void total() {
        SellStocks selling = defaultFlexTest();
        assertEquals(new BigDecimal("0.00"), selling.getOwned());
    }

    @Test
    public void checkStability() {
        SellStocks selling = defaultFlexTest();
        System.out.println("Stable? " + selling.getSellHistory());
    }

    static class CheckPack {
        private final BigDecimal known;
        private final BigDecimal test;
        public CheckPack(BigDecimal known, BigDecimal test) {
            this.known = known;
            this.test = test;
        }
        public BigDecimal getKnown() {
            return known;
        }
        public BigDecimal getTest() {
            return test;
        }
    }

    private CheckPack yieldCheck(SellStocks selling) {
        // instantiate a yield counter for each approach
        Bank flexBank = new Bank();
        // run and add to the yield counter
        LinkedList<BigDecimal> valueHist = selling.getValueHistory();
        LinkedList<BigDecimal> flexHist = selling.getSellHistory();
        YieldPack flexYield = flexBank.countYield(flexHist, valueHist);
        // naive yield

        // assert that method yields more than naive approach
        BigDecimal flex = flexYield.getTotal();
        BigDecimal naive = flexBank.naiveYield(owned, valueHist);
        System.out.println(
                selling.getSim().getSimName()
                + "Yield is " + flex
                + " instead of " + naive + " with history: \n"
                + Arrays.toString(flexYield.getYields()));
        return new CheckPack(naive, flex);
    }

    /**
     * Demonstrates gains from test runs,
     * using fixed values.
     */
    @Test
    public void yieldFixed() {
        SellStocks selling = defaultFlexTest();
        CheckPack check = yieldCheck(selling);
        assertEquals(check.getKnown(), check.getTest());
    }

    /**
     * Demonstrates gains from test runs,
     * using randomised values.
     */
    @Test
    public void yieldRand() {
        double horizontalVal = 10.;
        Line horizontal = new Line(horizontalVal, 0);
        ValueSim sim = new ValueSim(8., 12.,
                20, 2., horizontal, defaultSeed);
        BigDecimal[] values = sim.randLine();
        BigDecimal simMean = sim.mean(values);
        double bias = simMean.doubleValue()/horizontalVal;
        System.out.println("This seed is biased by " + bias + "\n"
                + "RandDist is \n"
                + Arrays.toString(values));
        SellStocks selling = flexHelper(values, sim);
        CheckPack check = yieldCheck(selling);
        assertTrue(check.getTest().doubleValue() >= check.getKnown().doubleValue()*bias);
    }

    @Test
    public void  mean() {
        ValueSim sim = new ValueSim(BigDecimal.ZERO, BigDecimal.ZERO, defaultSize);
        BigDecimal[] x = new BigDecimal[9];
        for (int i=0; i<9; i++) {
            x[i] = new BigDecimal(i+11);
        }
        assertEquals(sim.mean(x),new BigDecimal("15.00"));
    }

    @Test
    public void boundedMean() {
        ValueSim sim = new ValueSim(new BigDecimal("9.00"), new BigDecimal("11.00"), defaultSize);
        assertEquals(new BigDecimal("10.00"), sim.flexMean());
    }
}
