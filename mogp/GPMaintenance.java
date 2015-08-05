package mogp;

import java.util.HashMap;

/**
 * GPMaintenance interface denotes the methods all maintence objects
 * must provide. Note that the object will need to track overall levels
 * of fitness for each population member, however additional internal
 * attributes may be required for the specific fitness approaches the 
 * regime is using (e.g. for multi-objective variants)
 * 
 * @author Jonathan Fieldsend
 * @version 1.1
 */
public interface GPMaintenance
{
    /**
     * Carry out tournament selection and return fittest
     * 
     * @param pop set of solutions
     * @return fittest solution from a tournament
     */
    ArraySolution tournament(HashMap<Integer, ArraySolution> pop);
    
    /**
     * Carry out negative tournament selection least fit - solution with 
     * corresponding index is removed from internal maintence state when 
     * method completes
     * 
     * @param pop set of solutions
     * @return least fit solution from a tournament
     */
    ArraySolution negativeTournament(HashMap<Integer, ArraySolution> pop);
    
    /**
     * Carry out negative tournament selection least fit - returned 
     * solution is removed from internal maintence state when 
     * method completes
     * 
     * @param pop set of solutions
     * @return key of least fit solution from a tournament
     */
    int negativeTournamentKey(HashMap<Integer, ArraySolution> pop);
    
    
    /**
     * Get overall fitness of solution
     * 
     * @param solution to evaluate
     */
    void evaluateFitness(HashMap<Integer, ArraySolution> pop, ArraySolution solution);
    
    /**
     * Select best solutions from combination of pop and children, and replace
     * pop membership with them. There must be no overlap of keys in pop
     * anc children.
     * 
     * @param pop search population
     * @param children child population
     */
    void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children);  
}
