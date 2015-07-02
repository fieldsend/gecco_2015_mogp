package mogp;
import java.util.TreeSet;
import java.util.ArrayList;
/**
 * Write a description of class DominantionMaintenance here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class DominationMaintenance extends StandardMaintenance
{
    boolean[][] populationObjectives;
    final boolean inSet[];
    final TreeSet<Integer> outSet = new TreeSet<>();
    private int numberInSet = 0;
    private boolean first = true;
    
    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of mimisiation (e.g. standard or parsimonious)
     */
    DominationMaintenance(Problem problem, Parameters parameters, MinimisationType type) {
        super(problem, parameters,type);
        populationObjectives = new boolean[parameters.POPULATION_SIZE][];
        inSet = new boolean[parameters.POPULATION_SIZE];
        for (int i=0; i < parameters.POPULATION_SIZE; i++) {
            outSet.add(i);
            inSet[i] = false;
        }    
    }

    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournament() {
        if (getParetoSetSize() == parameters.POPULATION_SIZE){
            int i =  super.negativeTournament();
            inSet[i] = false;
            outSet.add(i);
            numberInSet--;
            return i;
        } 
        int worst = sampleRandomIndexExcludingElite(fitness.length), i, competitor;
        double fworst = fitness[worst];

        for ( i = 1; i < parameters.TOURNAMENT_SIZE; i ++ ) {
            competitor = sampleRandomIndexExcludingElite(fitness.length);
            if ( fitness[competitor] > fworst ) {
                fworst = fitness[competitor];
                worst = competitor;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (fitness[competitor] == fworst) {
                    if (lengths[competitor] > lengths[worst]) {
                        fworst = fitness[competitor];
                        worst = competitor;
                    }
                }
            }
        }
        return worst;
    }

    /**
     * @InheritDoc
     */
    @Override
    public int fitnessFunction(ArraySolution s, int index) {
        int f = 0;
        boolean[] output = new boolean[problem.fitnessCases];
        for (int i=0; i<problem.fitnessCases; i++ ){
            InputVector.setInput(problem.inputs[i]);

            if (s.process() != problem.targets[i]){
                f++;
                output[i] = false;
            } else {
                output[i] = true;
            }
        }
        populationUpdate(f,output,index,s.size());
        return f;
    }
    
    /*
     * update population with changed solution at index and insure all
     * statistics appropriate are tracked and relationships maintained
     */
    private void populationUpdate(int f, boolean[] output, int index, int size){
        populationObjectives[index] = output;
        if (type.equals(MinimisationType.PARSIMONIOUS))
            lengths[index] = size;
            
        updateParetoSet(index);
        fitness[index] = f;
    }
    
    /*
     * Ensure estimated pareto set is made up of mutually non-dominating solutions, now that
     * solution at index has changed
     */
    private void updateParetoSet(int index){
        Timing.setStartTime(); // put in to track time spent in update
        if (first){ // only at start
            inSet[index] = true;
            outSet.remove(index);
            first = false;
            numberInSet++;
        } else {
            if (inSet[index]) { // new solution at index may no longer be nondominated
                inSet[index] = false;
                outSet.add(index);
                numberInSet--;
            }
            
            if (!setWeakDominates(index)){
                addToParetoSet(index);
            } 
        }
        Timing.setEndTime();
        Timing.updateAccruedTime();
        Timing.incrementCalls();
    }

    /*
     * Method returns true if solution at index is wekly dominated by the other set members
     */
    private boolean setWeakDominates(int index){
        if (type.equals(MinimisationType.STANDARD)){
            for (int i=0; i<inSet.length; i++) 
                if (inSet[i]) 
                    if (weakDominates( populationObjectives[i], populationObjectives[index] ))
                        return true;
        } else {
            for (int i=0; i<inSet.length; i++) 
                if (inSet[i]) 
                    if (weakDominates( populationObjectives[i], populationObjectives[index], lengths[i], lengths[index] ))
                        return true;
        }
        return false;
    }

    private void addToParetoSet(int index) {
        inSet[index] = true;
        outSet.remove(index);
        numberInSet++;
        ArrayList<Integer> remove = new ArrayList<>();
        for (int i=0; i<inSet.length; i++)  // mark any now dominated in set
            if (inSet[i])
                if (i!=index) // will always weak dominate itself
                    if (weakDominates( populationObjectives[index], populationObjectives[i] ))
                        remove.add(i); // mark i for removal
        
        // remove any now dominated members
        for (Integer i : remove) {
            inSet[i] = false;
            outSet.add(i);
            numberInSet--;
        }
    }

    private boolean weakDominates(boolean[] a, boolean[] b, int lengthA, int lengthB){
        for (int i = 0; i<a.length; i++)
            if ((!a[i]) && (b[i]))
                return false;
        for (int i = 0; i<a.length; i++)
            if (a[i] != b[i])
                return true; // weak dominates but not equal
        // solutions are equal on criteria, so check size    
        if (lengthA > lengthB)
            return false;
        return true;
    }
    
    private boolean weakDominates(boolean[] a, boolean[] b){
        for (int i = 0; i<a.length; i++)
            if ((!a[i]) && (b[i]))
                return false;
        
        return true;
    }

    private int sampleRandomIndexExcludingElite(int length) {
        if (outSet.size() == 0)
            System.out.println("Out set empty, inSet size = " + getParetoSetSize() );
        Integer index;
        index = RandomNumberGenerator.getRandom().nextInt(length);
        if (outSet.contains(index)){
            return index;
        } else {
            Integer indexLow = outSet.lower(index);
            if (indexLow != null)
                return indexLow;
            return outSet.higher(index);    
        }
    }

    /**
     * Gets the number of non-dominated members in the search population
     * 
     * @return size of estimated Pareto set
     */
    public int getParetoSetSize() { 
        return numberInSet;        
    }
}
