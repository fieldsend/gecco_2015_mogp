package mogp;
import java.util.Random;

/**
 * RandomNumberGenerator provides singleton class random
 * number generator.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class RandomNumberGenerator
{
    private static final Random rng = new Random();
    
    /*
     * private constructor to prevent direct construction externally
     */
    private RandomNumberGenerator() {}
    
    /**
     * Returns the random number generator represented maintained 
     * by the class
     * 
     * @return returns a random number generator instance
     */
    static Random getRandom() {
        return rng;        
    }
    
    /**
     * Sets the seed used by the random number generator represented by the class
     */
    static void setSeed(long seed) {
        rng.setSeed(seed);
    }
}
