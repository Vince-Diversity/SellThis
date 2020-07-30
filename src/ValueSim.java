import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Simulates the value of stocks over time.
 */
public class ValueSim {

    private BigDecimal bot = BigDecimal.ZERO;
    private BigDecimal top = BigDecimal.ZERO;
    private final int valSize;
    private final String simName;
    private Line line = new Line();
    private double amplitude = 0.; // models local fluctuation extremes
    private Random rand;


    /**
     * Constructor for using the arithmetic mean prediction.
     */
    public ValueSim(BigDecimal bot, BigDecimal top, int valSize) {
        this.bot = bot;
        this.top = top;
        this.valSize = valSize;
        this.simName = "MEAN";
    }

 /**
     * Constructor for fluctuating line prediction.
     */
    public ValueSim(double bot, double top, int valSize, double amplitude, Line line, long seed) {
        this.bot = new BigDecimal(bot).setScale(2, RoundingMode.HALF_UP);
        this.top = new BigDecimal(top).setScale(2, RoundingMode.HALF_UP);
        this.valSize = valSize;
        this.simName = "RAND";
        this.amplitude = amplitude;
        this.line = line;
        this.rand = new Random(seed);
    }

    /**
     * Models the market
     * @param next double between 0 and 1
     */
    private double nextChange(double next) {
        return line.next() + amplitude*(2*next - 1.);
    }

    public BigDecimal[] randLine() {
        BigDecimal[] val = new BigDecimal[valSize];
        for (int i = 0; i < valSize; i++) {
            double r = nextChange(rand.nextDouble());
            val[i] = new BigDecimal(r).setScale(2, RoundingMode.HALF_UP);
        }
        return val;
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

    public int getValSize() {
        return valSize;
    }

    public String getSimName() {
        return simName;
    }

    public Line getLine() {
        return line;
    }
}

class Line {
    private double current;
    private final double increment;
    public Line(double current, double increment) {
        this.current = current;
        this.increment = increment;
    }
    public Line() {
        this.current = 0.;
        this.increment = 0.;
    }
    public double next() {
        current += increment;
        return current;
    }

    public double getCurrent() {
        return current;
    }
}