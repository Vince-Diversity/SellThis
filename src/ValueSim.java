import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Simulates the value of stocks over time.
 */
public class ValueSim {

    private final double bot;
    private final double top;
    private final int valSize;
    private final String simName;
    private final Line line;
    private double amplitude = 0.; // models local fluctuation extremes
    private Random rand;

    /**
     * When line is the prediction.
     */
    public ValueSim(double bot, double top, int valSize) {
        this.bot = bot;
        this.top = top;
        this.line = new Line(bot, ( top - bot ) / (valSize - 1));
        this.valSize = valSize;
        this.simName = "MEAN";
    }

    /**
     * When line is not necessarily the prediction.
     */
    public ValueSim(int valSize, double amplitude, Line line, long seed) {
        this.bot = 0.;
        this.top = 0.;
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
    public BigDecimal boundedMean() {
        return new BigDecimal(top - bot).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Mean from uniform distribution around bot and top
     */
    public BigDecimal flexMean() {
        return new BigDecimal(bot + (top - bot) / 2).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Uses the right hand side of the line as future values.
     * A line with increment dx and s steps left has
     * a value sum of current + dx*s/2)
     */
    public BigDecimal lineMean() {
        int remain = valSize - 1;
        return new BigDecimal( line.getCurrent() + line.getIncrement()*remain/2 )
                .setScale(2, RoundingMode.HALF_UP);
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
    public double next() {
        current += increment;
        return current;
    }

    public double getCurrent() {
        return current;
    }

    public double getIncrement() {
        return increment;
    }
}