package mogp;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
/**
 * FitnessSharingMaintenance class uses fitness sharing to maintain 
 * a population when conducting tournament selection.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class FitnessSharingMaintenance extends StandardMaintenance
{
    int[] totalSolvedByPopulation; // tracks how many of the population solve each test
    
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
        double worstValue = getSharedFitness(pop,solutionKey);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            int comparisonSolutionKey = getRandomParentKey(pop);
            while ((comparisonSolutionKey == solutionKey) || (pop.get(comparisonSolutionKey) == bestFitnessSolution))
                comparisonSolutionKey = getRandomParentKey(pop);

            double comparisonValue = getSharedFitness(pop,comparisonSolutionKey);
            if (comparisonValue < worstValue){ // if comparison is less fit
                worstValue = comparisonValue;
                solutionKey = comparisonSolutionKey;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (comparisonValue == worstValue) {
                    if (pop.get(comparisonSolutionKey).size() > pop.get(solutionKey).size()) {
                        worstValue = comparisonValue;
                        solutionKey = comparisonSolutionKey;
                    }
                }
            }
        }

        // keep totals up to date
        boolean[] a = pop.get(solutionKey).getTestsPassed();
        for (int i=0; i<problem.fitnessCases; i++ )
            if (a[i])
                totalSolvedByPopulation[i]--;

        return solutionKey;
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
        double worstValue = getSharedFitness(solution);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            ArraySolution comparisonSolution = getRandomParent(pop);
            while ((comparisonSolution == solution) || (comparisonSolution == bestFitnessSolution))
                comparisonSolution = getRandomParent(pop);

            double comparisonValue = getSharedFitness(comparisonSolution);
            if (comparisonValue < worstValue){ // if comparison is less fit
                worstValue = comparisonValue;
                solution = comparisonSolution;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (comparisonValue == worstValue) {
                    if (comparisonSolution.size() > solution.size()) {
                        worstValue = comparisonValue;
                        solution = comparisonSolution;
                    }
                }
            }
        }

        // keep totals up to date
        boolean[] a = solution.getTestsPassed();
        for (int i=0; i<problem.fitnessCases; i++ )
            if (a[i])
                totalSolvedByPopulation[i]--;

        return solution;
    }

    
    
    
    
    /*
     * Calculates shared fitness assocated with solution at given index
     */
    private double getSharedFitness(HashMap<Integer, ArraySolution> pop, int key) {
        return getSharedFitness(pop.get(key));        
    }

    private double getSharedFitness(ArraySolution s) {
        double value = 0.0;
        boolean[] a = s.getTestsPassed();
        
        for (int i=0; i<problem.fitnessCases; i++ )
            if (a[i])
                value += 1.0/totalSolvedByPopulation[i];
        return value;        
    }
    
    @Override
    public void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children) {
        // note, the keys in pop must not overlap with the keys in children
        HashMap<Integer,ArraySolution> combinedPopulation = new HashMap<>(pop);
        combinedPopulation.putAll(children);

        Set<ArraySolution> setOfBestSolutions = new HashSet<>();
        setOfBestSolutions.add(bestFitnessSolution); // always take best
        while (setOfBestSolutions.size() < parameters.POPULATION_SIZE) {
            setOfBestSolutions.add(fitnessBinaryTournament(combinedPopulation, setOfBestSolutions));
        }
        // setOfBestSolutions now includes parameters.POPULATION_SIZE solutions to preserve
        for (int i=0; i<problem.fitnessCases; i++ )
            totalSolvedByPopulation[i] = 0; // reset tracked totals
        int j=0;
        // replace the search population
        for (ArraySolution s : setOfBestSolutions){
            pop.put(j,s);
            boolean[] a = s.getTestsPassed();
            // update tracked totals
            for (int i=0; i<problem.fitnessCases; i++ )
                if (a[i])
                    totalSolvedByPopulation[i]++;
            j++;
        }
    }
    
    private ArraySolution fitnessBinaryTournament(HashMap<Integer, ArraySolution> pop, Set<ArraySolution> exclude) {
        ArraySolution solution = getRandomParent(pop);
        while(exclude.contains(solution)) {
            solution = getRandomParent(pop);
        }
        double bestValue = getSharedFitness(solution);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            ArraySolution comparison = getRandomParent(pop);
            while ((comparison == solution) || (exclude.contains(comparison)))
                comparison = getRandomParent(pop);
            double comparisonValue = getSharedFitness(comparison);
            if (comparisonValue > bestValue){ // if comparison is less fit
                bestValue = comparisonValue;
                solution = comparison;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (comparisonValue == bestValue) {
                    if (comparison.size() < solution.size()) {
                        bestValue = comparisonValue;
                        solution = comparison;
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
    public void evaluateFitness(HashMap<Integer, ArraySolution> pop, ArraySolution s) {
        super.evaluateFitness(pop,s);
        boolean[] results = s.getTestsPassed();
        for (int i=0; i<problem.fitnessCases; i++ )
            if (results[i])
                totalSolvedByPopulation[i]++;
        
    }
}
