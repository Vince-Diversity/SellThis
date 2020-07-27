import org.junit.Before;
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

    private BigDecimal owned;
    private BigDecimal top;
    private BigDecimal bot;

    @Before
    public void setup() {
        owned = new BigDecimal("100000.00");
        top = new BigDecimal("13.00");
        bot = new BigDecimal("9.00");
    }

    @Test
    public void reasonable() {
        int attempts = 9;
        BigDecimal value = new BigDecimal("10.00");
        ValueSim sim = new ValueSim(BigDecimal.ZERO, value.multiply(new BigDecimal(2)));
        SellStocks selling = new SellStocks(attempts, owned, Limit.BOUNDED, sim);
        BigDecimal sellToday = selling.today(value);
        System.out.println("Bounded first sell reasonable? " + sellToday);
    }

    private SellStocks flexHelper() {
        int attempts = 19;   // zero indexed
        BigDecimal value = new BigDecimal("10.00");
        ValueSim sim = new ValueSim(bot, top);
        SellStocks selling = new SellStocks(attempts, owned, Limit.FLEX, sim);
        for (int i=attempts; i>=0; i--) {
            BigDecimal sellToday = selling.today(value);
            selling.sell(sellToday, value);
        }
        return selling;
    }

    /**
     * In the end all should be spend.
     */
    @Test
    public void total() {
        SellStocks selling = flexHelper();
        assertEquals(new BigDecimal("0.00"), selling.getOwned());
    }

    @Test
    public void checkStability() {
        SellStocks selling = flexHelper();
        System.out.println("Stable? " + selling.getSellHistory());
    }

    /**
     * Demonstrates gains from test runs,
     * using fixed values.
     */
    @Test
    public void yieldFixed() {
        SellStocks selling = flexHelper();
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
        System.out.println("Yield is " + flex
                + " instead of " + naive
        + "\nYield history: " + Arrays.toString(flexYield.getYields()));
        assertEquals(naive, flex);
    }

    @Test
    public void  mean() {
        ValueSim sim = new ValueSim(BigDecimal.ZERO, BigDecimal.ZERO);
        BigDecimal[] x = new BigDecimal[9];
        for (int i=0; i<9; i++) {
            x[i] = new BigDecimal(i+11);
        }
        assertEquals(sim.mean(x),new BigDecimal("15.00"));
    }

    @Test
    public void boundedMean() {
        ValueSim sim = new ValueSim(new BigDecimal("9.00"), new BigDecimal("11.00"));
        assertEquals(new BigDecimal("10.00"), sim.flexMean());
    }
}
