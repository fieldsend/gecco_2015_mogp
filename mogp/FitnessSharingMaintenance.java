package mogp;

/**
 * FitnessSharingMaintenance class uses fitness sharing to maintain 
 * a population when conducting tournament selection.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class FitnessSharingMaintenance extends StandardMaintenance
{
    int[] totalSolvedByPopulation;
    boolean[][] sucessOnEachTest;

    /**
     * Constructor initialises with standard minimisation type (aggregate fitness)
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     */
    FitnessSharingMaintenance(Problem problem, Parameters parameters) {
        this(problem,parameters,MinimisationType.STANDARD);
    }

    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of mimisiation (e.g. standard or parsimonious)
     */
    FitnessSharingMaintenance(Problem problem, Parameters parameters, MinimisationType type ) {
        super(problem, parameters, type);
        totalSolvedByPopulation = new int[problem.fitnessCases];
        sucessOnEachTest = new boolean[parameters.POPULATION_SIZE][];
        for (int i = 0; i<totalSolvedByPopulation.length; i++)
            totalSolvedByPopulation[i]=0;
    } 

    /**
     * @InheritDoc
     */
    public int negativeTournament() {
        int worstIndex = indexOfFittest;
        while(worstIndex == indexOfFittest) {
            worstIndex = getRandomParentIndex();
        }
        double worstValue = getSharedFitness(worstIndex);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            int comparisonIndex = getRandomParentIndex();
            while ((comparisonIndex == worstIndex) || (comparisonIndex == indexOfFittest))
                comparisonIndex = getRandomParentIndex();
            double comparisonValue = getSharedFitness(comparisonIndex);
            if (comparisonValue<worstValue){ // if comparison is less fit
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

        // keep totals up to date
        for (int i=0; i<problem.fitnessCases; i++ )
            if (sucessOnEachTest[worstIndex][i])
                totalSolvedByPopulation[i]--;

        return worstIndex;
    }

    /*
     * Calculates shared fitness assocated with solution at given index
     */
    private double getSharedFitness(int index) {
        double value = 0.0;
        for (int i=0; i<problem.fitnessCases; i++ )
            if (sucessOnEachTest[index][i])
                value += 1.0/totalSolvedByPopulation[i];
        return value;        
    }

    
    /**
     * @InheritDoc
     */
    @Override
    public int fitnessFunction(ArraySolution s, int index) {
        int f = 0;
        boolean[] results = new boolean[problem.fitnessCases];
        for (int i=0; i<problem.fitnessCases; i++ ){
            InputVector.setInput(problem.inputs[i]);
            if (s.process() != problem.targets[i]){
                results[i] = false;
                f++;
            } else {
                results[i] = true;
                totalSolvedByPopulation[i]++;
            }
        }
        sucessOnEachTest[index] = results;
        fitness[index] =f;
        if (f < fittest) {
            fittest =f;
            indexOfFittest = index;
        }
        if (type.equals(MinimisationType.PARSIMONIOUS))
            lengths[index] = s.size();
        return f;
    }
}
