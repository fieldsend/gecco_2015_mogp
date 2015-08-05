package mogp;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Collection;

/**
 * BestSolver, maintains a solution for each objective if possible.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.2
 */
public class BestSolver extends StandardMaintenance
{
    ArraySolution[] bestSolverForEachObjective; // each best solver for each objective, elements null if none found yet
    HashMap<ArraySolution,TreeSet<Integer>> objectivesMarked = new HashMap<>(); // map of solution to set of criteria it is marked as solver for
    HashMap<Integer,Set<ArraySolution>> solversOfEachTest = new HashMap<>(); // map of test index to set of all solutions which solve it
    
    
    /**
     * Constructor initialises with standard minimisation type (aggregate fitness)
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     */
    BestSolver(Problem problem, Parameters parameters) {
        this(problem, parameters,MinimisationType.STANDARD);
    }

    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of mimisiation (e.g. standard or parsimonious)
     */
    BestSolver(Problem problem, Parameters parameters, MinimisationType type) {
        super(problem, parameters,type);

        bestSolverForEachObjective = new ArraySolution[problem.fitnessCases];
        for (int i=0; i< problem.fitnessCases; i++ ) {
            solversOfEachTest.put(i,new HashSet<ArraySolution>());
        }
    }

    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournamentKey(HashMap<Integer, ArraySolution> pop) {
        int solutionKey, comparisonSolutionKey;
        
        solutionKey = drawMember(pop); 
            
        while(pop.get(solutionKey) == bestFitnessSolution) {
            solutionKey = drawMember(pop);
        }

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            comparisonSolutionKey = drawMember(pop);
            while ((comparisonSolutionKey == solutionKey) || (pop.get(comparisonSolutionKey) == bestFitnessSolution))
                comparisonSolutionKey = drawMember(pop);

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
            
        // if a marked member fit has been portentially removed update map 
        if  (parameters.POPULATION_SIZE <= objectivesMarked.size()+1) { // will only occur if this is the case
            maintainMap(pop.get(solutionKey));
        }
        return solutionKey;
    }
    
    /**
     * @InheritDoc
     */
    @Override
    public ArraySolution negativeTournament(HashMap<Integer, ArraySolution> pop) {
        return pop.get(negativeTournamentKey(pop));
    }
    
    /*
     * Maintains internal maps when a marked solution is removed
     */
    private void maintainMap(ArraySolution solutionToRemove) {
        for (int i=0; i< problem.fitnessCases; i++) {
            if (solutionToRemove.getTestsPassed()[i])
                solversOfEachTest.get(i).remove(solutionToRemove); // remove from tracked overall numbers 
            if (bestSolverForEachObjective[i] == solutionToRemove) {
                TreeSet<Integer> set = objectivesMarked.get(solutionToRemove);
                set.remove(i);
                if (set.size() == 0) { // solution not best on any other criteria, so remove from map
                    objectivesMarked.remove(solutionToRemove);
                }
                bestSolverForEachObjective[i] = null;
                // now see if any others stored can act as best on this objective.
                ArraySolution replacingMarkedSolution = null;
                int replacingFitness = Integer.MAX_VALUE;
                for (int j=0; j< parameters.POPULATION_SIZE; j++){
                    if (bestSolverForEachObjective[j]!=null) { // solver found
                        if (bestSolverForEachObjective[j]!=solutionToRemove) { // and not the one being removed
                            if ((bestSolverForEachObjective[j].getTestsPassed())[i]) { // if it solves the criteria
                                if (bestSolverForEachObjective[j].getSumOfTestsFailed() < replacingFitness) { // if fitter that any allocated
                                    replacingMarkedSolution = bestSolverForEachObjective[j];
                                    replacingFitness = bestSolverForEachObjective[j].getSumOfTestsFailed();
                                }
                            }
                        }
                    }
                }
                if (replacingMarkedSolution != null) { // another stored member can be used
                    bestSolverForEachObjective[i] = replacingMarkedSolution;
                    set = objectivesMarked.get(replacingMarkedSolution);
                    set.add(i);
                }
            }
        }

    }

    /**
     * @InheritDoc
     */
    @Override
    public void evaluateFitness(HashMap<Integer, ArraySolution> pop, ArraySolution s) {
        super.evaluateFitness(pop,s);
        processOutput(pop,s);
    }

    /*
     * Method to process and update arrays which track the individual best solutions
     */
    private void processOutput(HashMap<Integer, ArraySolution> pop,ArraySolution s){
        boolean[] a = s.getTestsPassed();
        for (int i=0; i<problem.fitnessCases; i++){
            boolean update = false; // flag to see if marked has changed
            if (a[i]){ // if particular test is passed
                solversOfEachTest.get(i).add(s); // add to set of solutions tracked which solve
                if (bestSolverForEachObjective[i]==null)  // never been solved before
                    update = true;
                else    {
                    if ((s.getSumOfTestsFailed() < bestSolverForEachObjective[i].getSumOfTestsFailed()) || 
                    ((s.getSumOfTestsFailed() == bestSolverForEachObjective[i].getSumOfTestsFailed()) && (type.equals(MinimisationType.PARSIMONIOUS)) 
                             && (s.size() < bestSolverForEachObjective[i].size()))){// already solved, but 's' has better overall fitness
                        update = true;
                        // now remove previously best solution on this objective from store of marked
                        TreeSet<Integer> set = objectivesMarked.get(bestSolverForEachObjective[i]);
                        set.remove(i);
                        if (set.size() == 0) { // not best of any other criteria, so remove entirely
                            objectivesMarked.remove(bestSolverForEachObjective[i]);
                        }
                    }
                }
                if (update) {    
                    bestSolverForEachObjective[i] = s;
                    TreeSet<Integer> set = objectivesMarked.get(s);
                    if (set == null){
                        set = new TreeSet<Integer>(); 
                        objectivesMarked.put(s,set);
                    }
                    set.add(i);
                }    
            }
        }
    }

    /*
     * Draw method ensuring best on each objective are not replaced if possible
     */
    private int drawMember(HashMap<Integer, ArraySolution> pop){
        // if population size is smaller than the number of marked solutions 
        // (plus one as two must be compared)
        if (parameters.POPULATION_SIZE <= objectivesMarked.size()+1){
            return RandomNumberGenerator.getRandom().nextInt(parameters.POPULATION_SIZE);
        }
        // otherwise protect the fittest on each objective from removal
        int index = RandomNumberGenerator.getRandom().nextInt(parameters.POPULATION_SIZE);
        boolean contained = false;
        for (ArraySolution s : bestSolverForEachObjective){ 
            if (pop.get(index) == s) {
                index = drawMember(pop);    
                break;
            }
        }

        return index;    
    }

    /**
     * @InheritDoc
     */
    @Override
    public void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children) {
        /* 
         * ArraySolution[] bestSolverForEachObjective; // each best solver for each objective, elements null if none found yet
         * HashMap<ArraySolution,TreeSet<Integer>> objectivesMarked = new HashMap<>(); // map of solution to set of criteria it is marked as solver for
         * HashMap<Integer,Set<ArraySolution>> solversOfEachTest = new HashMap<>(); // map of test index to set of all solutions which solve it
         */
        
        HashMap<Integer,ArraySolution> combinedPopulation = new HashMap<>(pop);
        combinedPopulation.putAll(children);

        Set<ArraySolution> setOfBestSolutions = new HashSet<>();
        if (objectivesMarked.size() > pop.size()){ // more marked solutions than capacity
            setOfBestSolutions.add(bestFitnessSolution);
            
            while (setOfBestSolutions.size() < pop.size()) {
                setOfBestSolutions.add(tournamentWithParsimony(combinedPopulation));
            }
            // remove from marked any no longer tracked
            Set<ArraySolution> excluded = objectivesMarked.keySet();
            excluded.removeAll(setOfBestSolutions);
            for (ArraySolution s : excluded){
                maintainMap(s);
            }
        } else { // can fill population will marked solutions, and potentially extra
            // add marked keys
            setOfBestSolutions.addAll(objectivesMarked.keySet());
            while (setOfBestSolutions.size() < pop.size()) {
                setOfBestSolutions.add(tournamentWithParsimony(combinedPopulation));
            }
        }
        
        // setOfBestSolutions now includes parameters.POPULATION_SIZE solutions to preserve
        for (int i=0; i< problem.fitnessCases; i++ ) {
            solversOfEachTest.put(i,new HashSet<ArraySolution>());
        }
        int i=0;
        // replace the search population
        for (ArraySolution s : setOfBestSolutions){
            pop.put(i,s);
            i++; 
            // track which solutions have been solved by new population members
            boolean[] a = s.getTestsPassed();
            for (int j=0; j<a.length; j++){
                if (a[j]) {
                   solversOfEachTest.get(j).add(s);
                }
            }
        }
    }
    

    /**
     * Method tracking solvers of each test problem
     * 
     * @return number of solvers
     */
    int getMapSize(){
        return objectivesMarked.size();
    }
}
