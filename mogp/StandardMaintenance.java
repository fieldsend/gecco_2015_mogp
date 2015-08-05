package mogp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
/**
 * StandardMaintence class is a vanilla implementation of the
 * GPMaintance interface, where fitness used in tournament selection 
 * is the aggregate fitness across test problems
 * 
 * @author Jonathan Fieldsend 
 * @version 1.1
 */
public class StandardMaintenance implements GPMaintenance
{
    Problem problem;
    Parameters parameters;
    ArraySolution bestFitnessSolution;
    MinimisationType type;
    
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
    } 

    /**
     * @InheritDoc
     */
    @Override
    public ArraySolution tournament(HashMap<Integer, ArraySolution> pop) {
        ArraySolution solution = getRandomParent(pop);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            ArraySolution comparisonSolution = getRandomParent(pop);
            while (solution == comparisonSolution)
                comparisonSolution = getRandomParent(pop);
            if (comparisonSolution.getSumOfTestsFailed() < solution.getSumOfTestsFailed()){
                solution = comparisonSolution;
            }
        }
        return solution;
    }

    /**
     * @InheritDoc
     */
    @Override
    public ArraySolution negativeTournament(HashMap<Integer, ArraySolution> pop) {
        ArraySolution solution = getRandomParent(pop);
        while(solution == bestFitnessSolution) {
            solution = getRandomParent(pop);
        }

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            ArraySolution comparisonSolution = getRandomParent(pop);
            while ((comparisonSolution == solution) || (comparisonSolution == bestFitnessSolution))
                comparisonSolution = getRandomParent(pop);

            if (comparisonSolution.getSumOfTestsFailed() > solution.getSumOfTestsFailed()){
                solution = comparisonSolution;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (comparisonSolution.getSumOfTestsFailed() == solution.getSumOfTestsFailed()){
                    if (comparisonSolution.size() > solution.size()) {
                        solution = comparisonSolution;
                    }
                }
            }
        }
        return solution;
    }

    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournamentKey(HashMap<Integer, ArraySolution> pop) {
        int solutionKey = getRandomParentKey(pop);
        while(pop.get(solutionKey) == bestFitnessSolution) {
            solutionKey = getRandomParentKey(pop);
        }

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            int comparisonSolutionKey = getRandomParentKey(pop);
            while ((comparisonSolutionKey == solutionKey) || (pop.get(comparisonSolutionKey) == bestFitnessSolution))
                comparisonSolutionKey = getRandomParentKey(pop);

            if (pop.get(comparisonSolutionKey).getSumOfTestsFailed() > pop.get(solutionKey).getSumOfTestsFailed()){
                solutionKey = comparisonSolutionKey;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (pop.get(comparisonSolutionKey).getSumOfTestsFailed() == pop.get(solutionKey).getSumOfTestsFailed()){
                    if (pop.get(comparisonSolutionKey).size() > pop.get(solutionKey).size()) {
                        solutionKey = comparisonSolutionKey;
                    }
                }
            }
        }
        return solutionKey;
    }
    
    /**
     * @InheritDoc
     */
    @Override
    public void evaluateFitness(HashMap<Integer, ArraySolution> pop, ArraySolution s) {
        boolean[] results = new boolean[problem.fitnessCases];
        int f = 0;
        for (int i=0; i<problem.fitnessCases; i++ ){
            InputVector.setInput(problem.inputs[i]);
            if (s.process() != problem.targets[i]){
                f++;
                results[i] = false;
            } else {
                results[i] = true;
            }
        }
        // track best seen so far
        if (bestFitnessSolution==null){
             bestFitnessSolution = s;
        } else if (f < bestFitnessSolution.getSumOfTestsFailed()) {
            bestFitnessSolution = s;
        }

        s.setTestsPassed(results);  
    }

    /**
     * Method returns the a random member of the set pop
     * 
     * @param set to select a random member from
     * @return random population member
     */
    ArraySolution getRandomParent(HashMap<Integer, ArraySolution> pop){
        return pop.get(RandomNumberGenerator.getRandom().nextInt(pop.size()));
    }

    /**
     * Method returns the a random key of the set pop - assumes keys are 0 to size()-1
     * 
     * @param set to select a random member from
     * @return random population member key
     */
    int getRandomParentKey(HashMap<Integer, ArraySolution> pop){
        return RandomNumberGenerator.getRandom().nextInt(pop.size());
    }

    /**
     * @InheritDoc
     */
    @Override
    public void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children) {
        // note, the keys in pop must not overlap with the keys in children
        HashMap<Integer,ArraySolution> combinedPopulation = new HashMap<>(pop);
        combinedPopulation.putAll(children);

        Set<ArraySolution> setOfBestSolutions = new HashSet<>();
        setOfBestSolutions.add(bestFitnessSolution); // always take best
        while (setOfBestSolutions.size() < parameters.POPULATION_SIZE) {
            setOfBestSolutions.add(tournamentWithParsimony(combinedPopulation));
        }
        // setOfBestSolutions now includes parameters.POPULATION_SIZE solutions to preserve
        int i=0;
        // replace the search population
        for (ArraySolution s : setOfBestSolutions){
            pop.put(i,s);
            i++;
        }
    }

    ArraySolution tournamentWithParsimony(HashMap<Integer, ArraySolution> pop) {
        ArraySolution solution = getRandomParent(pop);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            ArraySolution comparisonSolution = getRandomParent(pop);
            while (solution == comparisonSolution)
                comparisonSolution = getRandomParent(pop);
            if (comparisonSolution.getSumOfTestsFailed() < solution.getSumOfTestsFailed()){
                solution = comparisonSolution;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (comparisonSolution.getSumOfTestsFailed() == solution.getSumOfTestsFailed()){
                    if (comparisonSolution.size() < solution.size()) {
                        solution = comparisonSolution;
                    }
                }
            }
        }
        return solution;
    }
}
