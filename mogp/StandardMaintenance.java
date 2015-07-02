package mogp;

/**
 * StandardMaintence class is a vanilla implementation of the
 * GPMaintance interface, where fitness used in tournament selection 
 * is the aggregate fitness across test problems
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class StandardMaintenance implements GPMaintenance
{
    int[] fitness;
    Problem problem;
    Parameters parameters;
    int indexOfFittest = -1;
    int fittest = Integer.MAX_VALUE;
    MinimisationType type;
    int[] lengths;

    /**
     * Constructor initialises with standard minimisation type (aggregate fitness)
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     */
    StandardMaintenance(Problem problem, Parameters parameters) {
        this(problem,parameters,MinimisationType.STANDARD);
    }

    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of mimisiation (e.g. standard or parsimonious)
     */
    StandardMaintenance(Problem problem, Parameters parameters, MinimisationType type ) {
        this.problem = problem;
        this.parameters = parameters;
        this.type = type;
        fitness = new int[parameters.POPULATION_SIZE];
        for (int i=0; i<parameters.POPULATION_SIZE; i++)
            fitness[i] = Integer.MAX_VALUE;
        SetUpLength(); 
    } 

    /*
     * If using parsimounous approach want to keep track of the length of each solution 
     * alongside the fitness
     */
    private void SetUpLength() {
        if (type.equals(MinimisationType.PARSIMONIOUS))
            lengths =  new int[parameters.POPULATION_SIZE];
        else
            lengths =  new int[0];
    }

    /**
     * @InheritDoc
     */
    @Override
    public int tournament() {
        int bestIndex = getRandomParentIndex();
        int bestValue = fitness[bestIndex];

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            int comparisonIndex = getRandomParentIndex();
            while (comparisonIndex == bestIndex)
                comparisonIndex = getRandomParentIndex();
            int comparisonValue = fitness[comparisonIndex];
            if (comparisonValue<bestValue){
                bestValue = comparisonValue;
                bestIndex = comparisonIndex;
            }
        }
        return bestIndex;
    }

    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournament() {
        int worstIndex = indexOfFittest;
        while(worstIndex == indexOfFittest) {
            worstIndex = getRandomParentIndex();
        }
        int worstValue = fitness[worstIndex];

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            int comparisonIndex = getRandomParentIndex();
            while ((comparisonIndex == worstIndex) || (comparisonIndex == indexOfFittest))
                comparisonIndex = getRandomParentIndex();
            int comparisonValue = fitness[comparisonIndex];
            if (comparisonValue>worstValue){
                worstValue = comparisonValue;
                worstIndex = comparisonIndex;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (comparisonValue == worstValue) {
                    if (lengths[comparisonIndex] > lengths[worstIndex]) {
                        worstValue = comparisonValue;
                        worstIndex = comparisonIndex;
                    }
                }
            }
        }
        return worstIndex;
    }

    
    /**
     * @InheritDoc
     */
    @Override
    public int fitnessFunction(ArraySolution s, int index) {
        int f = 0;
        for (int i=0; i<problem.fitnessCases; i++ ){
            InputVector.setInput(problem.inputs[i]);
            if (s.process() != problem.targets[i])
                f++;
        }
        fitness[index] =f;
        if (f < fittest) {
            fittest =f;
            indexOfFittest = index;
        }
        if (type.equals(MinimisationType.PARSIMONIOUS))
            lengths[index] = s.size();
        return f;
    }

    /**
     * Method returns the index of a random member of the search 
     * population
     * 
     * @return random population member index
     */
    int getRandomParentIndex(){
        return RandomNumberGenerator.getRandom().nextInt(parameters.POPULATION_SIZE);
    }

    /**
     * @InheritDoc
     */
    @Override
    public int getFitness(int index) {
        return fitness[index];
    }
}
