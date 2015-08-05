package mogp;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * EliteMaintenance approach ensures that only best solutions are 
 * maintained
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class EliteMaintenance extends StandardMaintenance
{
    List<ArraySolution> orderedSolutions = new ArrayList<>();
    boolean needsSorting = true;
    /**
     * Constructor of maintenance object
     * 
     * @param problem problem to be optimised
     * @param parameters algorithm parameters
     * @param type of mimisiation (e.g. standard or parsimonious)
     */
    EliteMaintenance(Problem problem, Parameters parameters, MinimisationType type ) {
        super(problem, parameters, type);
    } 
    
    @Override
    public ArraySolution negativeTournament(HashMap<Integer, ArraySolution> pop) {
        sortIfRequired();
        if (type.equals(MinimisationType.PARSIMONIOUS)){
            // possiblity of duplicate fitnesses, so get rid of largest tree
            // in terms of total nodes and leaves
            int startIndex = orderedSolutions.size()-1;
            int worstFitness = orderedSolutions.get(orderedSolutions.size()-1).getSumOfTestsFailed();
            for (int i = startIndex-1; i>=0; i--) {
                if (orderedSolutions.get(i).getSumOfTestsFailed()!=worstFitness){
                    startIndex = i;
                    break;
                }
            }
            int worstLength = orderedSolutions.get(startIndex).size();
            for (int i=startIndex; i< orderedSolutions.size()-1; i++){
                if (orderedSolutions.get(i).size() > worstLength){
                    worstLength = orderedSolutions.get(i).size();
                    startIndex = i;
                }
            }
            return orderedSolutions.remove(startIndex);
        }
        // if not parsimonious type
        return orderedSolutions.remove(orderedSolutions.size()-1); // return and remove worst element
    }
    
    private void sortIfRequired(){
        if (needsSorting){
            Collections.sort(orderedSolutions);
            needsSorting = false;
        }
    }
    
    @Override
    public int negativeTournamentKey(HashMap<Integer, ArraySolution> pop) {
        sortIfRequired();
        ArraySolution toRemove = negativeTournament(pop);
        for (Entry<Integer, ArraySolution> e : pop.entrySet())
            if (e.getValue() == toRemove)
                return e.getKey();
        return -1; // should never reach this
    }
    
    @Override
    public void evaluateFitness(HashMap<Integer, ArraySolution> pop, ArraySolution s) {
        super.evaluateFitness(pop,s);
        orderedSolutions.add(s);
        needsSorting = true;
    }
    
    @Override
    public void generateNextSearchPopulation(HashMap<Integer, ArraySolution> pop, HashMap<Integer, ArraySolution> children) {
        // HashMap<Integer, ArraySolution> combined = new HashMap<>(pop);
        // combined.putAll(children);
        sortIfRequired();
        if (type.equals(MinimisationType.PARSIMONIOUS)){
            // get fitness of last preserved member
            int fitnessOfLast = orderedSolutions.get(pop.size()-1).getSumOfTestsFailed();
            int rangeMin = -1;
            int rangeMax = -1;
            for (int i = pop.size()-2; i>=0; i--){
                if (orderedSolutions.get(i).getSumOfTestsFailed()!=fitnessOfLast){
                    rangeMin = i;
                    break;
                }
            }
            if (rangeMin == -1)
                rangeMin = 0;
            for (int i = pop.size(); i<orderedSolutions.size(); i++){
                if (orderedSolutions.get(i).getSumOfTestsFailed()!=fitnessOfLast){
                    rangeMax = i;
                    break;
                }
            }
            if (rangeMax == -1)
                rangeMax = orderedSolutions.size()-1;
            // rangeMax and rangeMin now cover the sorted solutions with the same
            // fitness which straddle the truncation point.
            List<LengthRepresentation> lengths = new ArrayList<>();
            for (int i = rangeMin; i<=rangeMax; i++) {
                lengths.add(new LengthRepresentation(orderedSolutions.get(i)));
            }
            Collections.sort(lengths);
            orderedSolutions.subList(rangeMin, orderedSolutions.size()).clear();
            for (int i = rangeMin; i<pop.size(); i++)
                orderedSolutions.add(lengths.get(i-rangeMin).getWrappedSolution());
            //System.out.println(1);
        } else {
            orderedSolutions.subList(pop.size(), orderedSolutions.size()).clear();
            //System.out.println(2);
        }
        assert(orderedSolutions.size() == pop.size()) : "sorted size is "+ orderedSolutions.size() + " should be " +pop.size();
        for (int i=0; i<pop.size(); i++)
            pop.put(i,orderedSolutions.get(i));
        assert(sanityCheck());
    }
    private boolean sanityCheck(){
        for (int i=0; i<orderedSolutions.size(); i++)
            for (int j=0; j<orderedSolutions.size(); j++)
                if (i!=j)
                    if (orderedSolutions.get(i) == orderedSolutions.get(j))
                        return false;
        return true;                
    }
    
    
    private class LengthRepresentation implements Comparable<LengthRepresentation>{
        private ArraySolution s;
        LengthRepresentation(ArraySolution s){
            this.s = s;
        }
        
        ArraySolution getWrappedSolution(){
            return s;
        }
        
        @Override
        public int compareTo(LengthRepresentation obj){
            if (this.s.size() < obj.s.size())
                return -1;
            if (this.s.size() == obj.s.size())
                return 0;
            return 1;
        }
    }
    
}
