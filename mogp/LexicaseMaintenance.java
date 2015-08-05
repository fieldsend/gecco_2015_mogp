package mogp;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/**
 * LexicaseMaintenance, uses Lexicase approach for selection in
 * maintenance.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.2
 */
public class LexicaseMaintenance extends FitnessSharingMaintenance
{
    private List<Integer> listOfObjectives; // list of objective indices to shuffle
    //private Set<Integer> populationIndices;

    /**
     * Constructor initialises with standard minimisation type (aggregate fitness)
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     */
    LexicaseMaintenance(Problem problem, Parameters parameters) {
        this(problem,parameters,MinimisationType.STANDARD);
    }

    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of minimisiation (e.g. standard or parsimonious)
     */
    LexicaseMaintenance(Problem problem, Parameters parameters, MinimisationType type ) {
        super(problem, parameters, type);
        listOfObjectives = new ArrayList<>(problem.fitnessCases);
        //populationIndices = new HashSet<>(parameters.POPULATION_SIZE);
        for (int i=0; i< problem.fitnessCases; i++)
            listOfObjectives.add(i); // propagate the list 
        //for (int i=0; i< parameters.POPULATION_SIZE; i++)
        //    populationIndices.add(i);
    } 

    /*
     * method returns the subset of set which does not pass the test at objectiveIndex
     */
    private Set<Integer> notSolving(HashMap<Integer, ArraySolution> pop, Set<Integer> set, int objectiveIndex) {
        Set<Integer> notSolving = new HashSet<>();
        for (Integer i : set) {
            if (pop.get(i).getTestsPassed()[objectiveIndex]==false) {
                notSolving.add(i);
            }
        }
        return notSolving;
    }

    /*
     * method returns the subset of set which pass the test at objectiveIndex
     */
    private Set<ArraySolution> solving(Set<ArraySolution> set, int objectiveIndex) {
        Set<ArraySolution> solving = new HashSet<>();
        for (ArraySolution i : set) {
            if (i.getTestsPassed()[objectiveIndex]) {
                solving.add(i);
            }
        }
        return solving;
    }
    
    /**
     * @InheritDoc
     */
    @Override
    public ArraySolution negativeTournament(HashMap<Integer, ArraySolution> pop) {
        return pop.get(negativeTournamentKey(pop));
    }
    
    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournamentKey(HashMap<Integer, ArraySolution> pop) {
        //System.out.println(listOfObjectives.size());
        Collections.shuffle(listOfObjectives); // reorder objective list
        Set<Integer> populationSubset = new HashSet<>(pop.keySet());
        
        for (Integer i : listOfObjectives) { // process shuffled objectives in turn
            // only bother with objectives that are not solved by everyone already
            // otherwise wasted computation
            //if (totalSolvedByPopulation[i] != parameters.POPULATION_SIZE) {
                Set<Integer> reduced = notSolving(pop,populationSubset,i);
                //System.out.print(i+ ":" +reduced.size() + " ");
                if (reduced.size() > 0) {
                    populationSubset = reduced;
                }
            //}
        }
        //System.out.println();
        //System.out.println(populationSubset.size());
        // now reduced set has 1 to many members, compare up to
        // parameters.TOURNAMENT_SIZE random of these, and return one of them based on 
        // aggregate fitness
        List<Integer> randomReducedList = new ArrayList<>(populationSubset);
        Collections.shuffle(randomReducedList);
        int worst = randomReducedList.get(0);

        for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
            if (i>randomReducedList.size()){
                int comparison = randomReducedList.get(i);
                if (pop.get(comparison).getSumOfTestsFailed() < pop.get(worst).getSumOfTestsFailed()){ // if comparison is less fit
                    worst = comparison;
                } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                    if (pop.get(comparison).getSumOfTestsFailed() == pop.get(worst).getSumOfTestsFailed()) {
                        if (pop.get(comparison).size() > pop.get(worst).size()) {
                            worst = comparison;
                        }
                    }
                }
            }
        }

        // keep totals up to date
        for (int i=0; i<problem.fitnessCases; i++ )
            if (pop.get(worst).getTestsPassed()[i])
                totalSolvedByPopulation[i]--;

        return worst;
    }
    
    @Override
    public void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children) {
        Set<ArraySolution> setOfBest = new HashSet<>();
        HashMap<Integer,ArraySolution> combinedPopulation = new HashMap<>(pop);
        combinedPopulation.putAll(children);
        HashSet<ArraySolution> toConsider = new HashSet<>(combinedPopulation.values());
        while (setOfBest.size() < parameters.POPULATION_SIZE) {
            setOfBest.add(lexicaseSelection(toConsider, setOfBest));
        }
        
        // setOfBest now includes parameters.POPULATION_SIZE solutions to preserve
        for (int i=0; i<problem.fitnessCases; i++ )
            totalSolvedByPopulation[i] = 0; // reset tracked totals
        int j=0;
        // replace the search population
        for (ArraySolution s : setOfBest){
            pop.put(j,s);
            boolean[] a = s.getTestsPassed();
            // update tracked totals
            for (int i=0; i<problem.fitnessCases; i++ )
                if (a[i])
                    totalSolvedByPopulation[i]++;
            j++;
        }
    }

    public ArraySolution lexicaseSelection(Set<ArraySolution> pop, Set<ArraySolution> toIgnore) {
        //System.out.println(listOfObjectives.size());
        Collections.shuffle(listOfObjectives); // reorder objective list
        Set<ArraySolution> populationSubset = new HashSet<>(pop);
        populationSubset.removeAll(toIgnore); // don't consider those already preserved
        for (Integer i : listOfObjectives) { // process shuffled objectives in turn
            Set<ArraySolution> reduced = solving(populationSubset,i);
            //System.out.print(i+ ":" +reduced.size() + " ");
            if (reduced.size() > 0) {
                populationSubset = reduced;
            }
        }
        
        List<ArraySolution> randomReducedList = new ArrayList<>(populationSubset);
        Collections.shuffle(randomReducedList);
        ArraySolution best = randomReducedList.get(0);
        if (type.equals(MinimisationType.PARSIMONIOUS)){
            for (int i=1; i<parameters.TOURNAMENT_SIZE; i++){
                if (i>randomReducedList.size()){
                    ArraySolution comparison = randomReducedList.get(i);
                    if (comparison.size() < best.size()) {
                        best = comparison;
                    }
                }
            } 
        } 
        return best;
    }

}
