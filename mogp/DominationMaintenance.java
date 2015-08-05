package mogp;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Collections;

/**
 * DominantionMaintenance - maintenance approach using domination.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.1
 */
public class DominationMaintenance extends StandardMaintenance
{
    final Set<ArraySolution> nondominatedSet = new HashSet<>();
    final Set<ArraySolution> dominatedSet = new HashSet<>();
     
    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of mimisiation (e.g. standard or parsimonious)
     */
    DominationMaintenance(Problem problem, Parameters parameters, MinimisationType type) {
        super(problem, parameters,type);  
    }

    /**
     * @InheritDoc
     */
    @Override
    public ArraySolution negativeTournament(HashMap<Integer, ArraySolution> pop) {
        //System.out.println(maintainedSetsTotalSize());
        
        if (getParetoSetSize() >= parameters.POPULATION_SIZE){
            ArraySolution i =  super.negativeTournament(pop);
            nondominatedSet.remove(i);
            return i;
        } 
        ArraySolution worst = sampleRandomExcludingElite(), competitor;
        
        for (int i = 1; i < parameters.TOURNAMENT_SIZE; i ++ ) {
            competitor = sampleRandomExcludingElite();
            if ( competitor.getSumOfTestsFailed() > worst.getSumOfTestsFailed()  ) {
                worst = competitor;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (competitor.getSumOfTestsFailed() == worst.getSumOfTestsFailed() ) {
                    if (competitor.size() > worst.size()){
                        worst = competitor;
                    }
                }
            }
        }
        dominatedSet.remove(worst);
        return worst;
    }
    
    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournamentKey(HashMap<Integer, ArraySolution> pop) {
        //System.out.println(maintainedSetsTotalSize());
        if (getParetoSetSize() >= parameters.POPULATION_SIZE){
            int i =  super.negativeTournamentKey(pop);
            nondominatedSet.remove(pop.get(i));
            return i;
        } 
        int worst = sampleRandomIndexExcludingElite(pop), competitor;
        
        for (int i = 1; i < parameters.TOURNAMENT_SIZE; i ++ ) {
            competitor = sampleRandomIndexExcludingElite(pop);
            if ( pop.get(competitor).getSumOfTestsFailed() > pop.get(worst).getSumOfTestsFailed()  ) {
                worst = competitor;
            } else if (type.equals(MinimisationType.PARSIMONIOUS)) {
                if (pop.get(competitor).getSumOfTestsFailed() == pop.get(worst).getSumOfTestsFailed() ) {
                    if (pop.get(competitor).size() > pop.get(worst).size()){
                        worst = competitor;
                    }
                }
            }
        }
        dominatedSet.remove(pop.get(worst));
        return worst;
    }

    /**
     * @InheritDoc
     */
    @Override
    public void evaluateFitness(HashMap<Integer, ArraySolution> pop, ArraySolution s) {
        super.evaluateFitness(pop,s);
        updateParetoSet(pop,s);
        //System.out.println(maintainedSetsTotalSize());
    }
    
    
    /*
     * Ensure estimated pareto set is made up of mutually non-dominating solutions, now that
     * solution at index has changed
     */
    private void updateParetoSet(HashMap<Integer, ArraySolution> pop, ArraySolution s){
        Timing.setStartTime(); // put in to track time spent in update
        if (!setWeakDominates(s)){
            addToParetoSet(s);
        } else {
            dominatedSet.add(s);
        }
        Timing.setEndTime();
        Timing.updateAccruedTime();
        Timing.incrementCalls();
    }

    /*
     * Method returns true if solution at index is wekly dominated by the other set members
     */
    private boolean setWeakDominates(ArraySolution s){
        if (type.equals(MinimisationType.STANDARD)){
            for (ArraySolution i : nondominatedSet) 
                if (weakDominates( i.getTestsPassed(), s.getTestsPassed() ))
                    return true;
        } else {
            for (ArraySolution i : nondominatedSet) 
                if (weakDominates( i.getTestsPassed(), s.getTestsPassed(), i.size(), s.size() ))
                        return true;
        }
        return false;
    }

    private void addToParetoSet(ArraySolution s) {
        Set<ArraySolution> remove = new HashSet<>();
        for (ArraySolution i : nondominatedSet)  // mark any now dominated in set
            if (weakDominates( s.getTestsPassed(),  i.getTestsPassed() ))
                remove.add(i); // mark i for removal
        
        // remove any now dominated members, and move to dominated set
        nondominatedSet.removeAll(remove);
        dominatedSet.addAll(remove);
        // add new entrant to Pareto set
        nondominatedSet.add(s);
    }

    private boolean weakDominates(boolean[] a, boolean[] b, int lengthA, int lengthB){
        for (int i = 0; i<a.length; i++)
            if ((!a[i]) && (b[i])) // if b[i] is true, but a[i] isn't then a[i] can't dom
                return false;
        for (int i = 0; i<a.length; i++)
            if (a[i] != b[i])
                return true; // weak dominates but not equal, so no size check needed
        // solutions are equal on all criteria, so check size    
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

    private int sampleRandomIndexExcludingElite(HashMap<Integer, ArraySolution> pop) {
        int index = RandomNumberGenerator.getRandom().nextInt(dominatedSet.size());
        int i = 0;
        
        ArraySolution solution = null;
        for (ArraySolution a : dominatedSet) {
            if (i == index){
                solution = a;
                break;
            }
            i++;
        }
        int sampleIndex = -1;
        // now find the corresponding index of the solutio in pop
        for (Entry<Integer, ArraySolution> e : pop.entrySet()){
            if (e.getValue() == solution){
                sampleIndex = e.getKey();
                break;
            }
        }
        if (sampleIndex==-1)
            System.out.println("err in samp rand exc elite");
        return sampleIndex;
    }

    private ArraySolution sampleRandomExcludingElite() {
        List<ArraySolution> list  = new ArrayList<>(dominatedSet);
        Collections.shuffle(list);
        return list.get(0);
    }

    /**
     * @InheritDoc
     */
    @Override
    public void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children) {
        Set<ArraySolution> setOfBestSolutions = new HashSet<>();
        HashMap<Integer, ArraySolution> combinedPop = new HashMap<>(pop); 
        combinedPop.putAll(children);
        // preserve nondominated where possible, and remove via negative 
        // tournament selection
        if (nondominatedSet.size() <= pop.size() ){ // can preserve all non dominated
            while (maintainedSetsTotalSize() > pop.size())
                negativeTournament(combinedPop);
            setOfBestSolutions.addAll(dominatedSet);
        } else { // have to remove from dominated
            dominatedSet.clear();
            while (nondominatedSet.size() > pop.size())
                negativeTournament(combinedPop);
        }
        setOfBestSolutions.addAll(nondominatedSet);
            
        // setOfBestSolutions now includes parameters.POPULATION_SIZE solutions to preserve
        int i=0;
        // replace the search population
        for (ArraySolution s : setOfBestSolutions){
            pop.put(i,s);
            i++;
        }
        assert(maintainedSetsTotalSize() == pop.size()) : "Internal maintained sets do not match search population size after truncation";
    }
    
    /**
     * Gets the number of non-dominated members in the search population
     * 
     * @return size of estimated Pareto set
     */
    public int getParetoSetSize() { 
        return nondominatedSet.size();        
    }
    
    private int maintainedSetsTotalSize() {
        return nondominatedSet.size() + dominatedSet.size();
    }
}
