package mogp;

/**
 * Object holds experimental parameter values.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class Parameters
{
    final int MAX_LENGTH, POPULATION_SIZE, GENERATIONS, TOURNAMENT_SIZE, MAX_DEPTH=10; 
    final double MUTATION_PROBABILITY_PER_NODE, CROSSOVER_PROBABILITY;
    
    /**
     * Constructor sets default parameter of the GP
     */
    Parameters() { 
        MAX_LENGTH = 10000; POPULATION_SIZE = 10; GENERATIONS = 1000000;
        TOURNAMENT_SIZE = 2; MUTATION_PROBABILITY_PER_NODE = 0.05; CROSSOVER_PROBABILITY = 0.9;
    }
    
    /**
     * Constructor to set values of parameters
     * 
     * @param MAX_LENGTH maximum number of elements permitted in a tree
     * @param POPULATION_SIZE size of search population
     * @param GENERATIONS number of generations to evolve population for
     * @param TOURNAMENT_SIZE number of members in a tournament
     * @param MUTATION_PROBABILITY_PER_NODE probability of a node being mutation
     * @param CROSSOVER_PROBABILITY probability of crossover
     */
    Parameters(int MAX_LENGTH, int POPULATION_SIZE, int GENERATIONS, int TOURNAMENT_SIZE, 
    double MUTATION_PROBABILITY_PER_NODE, double CROSSOVER_PROBABILITY) {
        this.MAX_LENGTH = MAX_LENGTH; this.POPULATION_SIZE = POPULATION_SIZE;
        this.GENERATIONS = GENERATIONS; this.TOURNAMENT_SIZE = TOURNAMENT_SIZE; 
        this.MUTATION_PROBABILITY_PER_NODE = MUTATION_PROBABILITY_PER_NODE;
        this.CROSSOVER_PROBABILITY = CROSSOVER_PROBABILITY;
    }

}
