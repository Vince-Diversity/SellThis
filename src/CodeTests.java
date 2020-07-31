import org.junit.Test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Tests for meaningful method:
 * - 'total': all has been sold neither too early nor too late.
 * - 'stability': if the value happens to be constant, no unstable fluctuations.
 * - 'yield': method can be better than selling day 1 or last day.
 *
 * Further tests someplace else
 * - 'converge': the closer prediction is to input value, the greater yield.
 */
public class CodeTests {

    private final BigDecimal owned = new BigDecimal("100000.00");
    private final int defaultSize = 20;
    private final BigDecimal defaultValue = new BigDecimal("10.00");

    @Test
    public void reasonable() {
        int attempts = 9;
        ValueSim sim = new ValueSim(0., 0., attempts+1);
        SellStocks selling = new SellStocks(owned, Limit.BOUNDED, sim);
        BigDecimal sellToday = selling.today(defaultValue);
        System.out.println("Bounded first sell reasonable? " + sellToday);
    }

    private SellStocks flexHelper(BigDecimal[] values, ValueSim sim) {
        SellStocks selling = new SellStocks(owned, Limit.FLEX, sim);
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
        double top = 13.;
        double bot = 9.;
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
        System.out.println("\n"
                + selling.getSim().getSimName()
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
        double val = 10.;
        Line line = new Line(val, 0.05);
        long defaultSeed = 400L;
        ValueSim sim = new ValueSim(
                20, 2., line, defaultSeed);
        BigDecimal[] values = sim.randLine();
        BigDecimal simMean = sim.mean(values);
        double bias = simMean.doubleValue()/val;
        SellStocks selling = flexHelper(values, sim);
        CheckPack check = yieldCheck(selling);
        System.out.println("This seed is biased by " + bias + ",\n"
                + "Line goes from " + val + " to " + selling.getSim().getLine().getCurrent() + "\n"
                + "RandDist is \n"
                + Arrays.toString(values));
        assertTrue(check.getTest().doubleValue() >= check.getKnown().doubleValue());
        // Check that the final prediction is first value + increment*size
        assertEquals(new BigDecimal("11.00"), selling.getPrediction());
    }

    @Test
    public void  mean() {
        ValueSim sim = new ValueSim(0., 0., defaultSize);
        BigDecimal[] x = new BigDecimal[9];
        for (int i=0; i<9; i++) {
            x[i] = new BigDecimal(i+11);
        }
        assertEquals(sim.mean(x),new BigDecimal("15.00"));
    }

    @Test
    public void boundedMean() {
        ValueSim sim = new ValueSim(9, 11., defaultSize);
        assertEquals(new BigDecimal("10.00"), sim.flexMean());
    }

    @Test
    public void lineMean() {
        ValueSim flexSim = new ValueSim(9., 12., 6);
        // add one increment to edgeMean to simulate taking the future mean
        BigDecimal compensate = BigDecimal.valueOf(( 12. - 9. ) / (6 - 1)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal edgeMean = flexSim.flexMean().add( compensate );
        ValueSim lineSim = new ValueSim(9., 12., 6);
        BigDecimal lineMean = lineSim.lineMean(5);
        assertEquals(edgeMean, lineMean);
    }
}
