package mogp;

/**
 * GPMaintenance interface denotes the methods all maintence objects
 * must provide. Note that the object will need to track overall levels
 * of fitness for each population member, however additional internal
 * attributes may be required for the specific fitness approaches the 
 * regime is using (e.g. for multi-objective variants)
 * 
 * @author Jonathan Fieldsend
 * @version 1.0
 */
public interface GPMaintenance
{
    /**
     * Get fitness of s, and update data at index
     * 
     * @param s solution to evaluate
     * @param index index to track fitness with
     * @return overall fitness value
     */
    int fitnessFunction(ArraySolution s, int index);
    
    /**
     * Carry out tournament selection and return index of fittest
     * 
     * @return index of fittest
     */
    int tournament();
    
    /**
     * Carry out negative tournament selection and return index of least fit
     * 
     * @param index of fittest member in tournament
     */
    int negativeTournament();
    
    /**
     * Get overall fitness of solution at index
     * 
     * @return overall fitness value
     */
    int getFitness(int index);
}
