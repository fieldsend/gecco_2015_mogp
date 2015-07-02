package mogp;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Collection;

/**
 * Write a description of class MajorityBasic here.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class BestSolver extends StandardMaintenance
{
    int[] indicesOfBestSolverForEachObjective;
    int[] fitnessOfBestSolver;
    boolean[][] populationObjectives;
    int bestFitness = Integer.MAX_VALUE;
    HashMap<Integer,TreeSet<Integer>> objectivesAchieved = new HashMap<>(); // map of population index to list of solved criteria
    
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
        
        populationObjectives = new boolean[parameters.POPULATION_SIZE][];
        fitnessOfBestSolver = new int[problem.fitnessCases];
        indicesOfBestSolverForEachObjective = new int[problem.fitnessCases];
        for (int i=0; i< problem.fitnessCases; i++ ) {
            indicesOfBestSolverForEachObjective[i] = -1;
        }
    }

    /**
     * @InheritDoc
     */
    @Override
    public int negativeTournament() {
        int worstIndex = drawMember(), competitor;
        double fworst = fitness[worstIndex], scaledWorst=0.0;
        
        for (int i = 1; i < parameters.TOURNAMENT_SIZE; i ++ ) {
            competitor = drawMember(); 
            while (competitor == worstIndex)
               competitor =  drawMember();// ensure doesn't compare to itself
             
            if (fworst == bestFitness){
                fworst = fitness[competitor]; // don't remove best overall
                worstIndex = competitor;
            } else if (fitness[competitor] > bestFitness){ // only compare to competitor if competitor not best itself
                if ( fitness[competitor] > fitness[worstIndex] ){
                    fworst = fitness[competitor];
                    worstIndex = competitor;
                } else if (type.equals(MinimisationType.PARSIMONIOUS)){
                    if (fitness[competitor] == bestFitness ) {
                        if (lengths[competitor] > lengths[worstIndex]) {
                            fworst = fitness[competitor];
                            worstIndex = competitor;
                        }
                    }
                }
            }
        }
        
        // if a individual fit has been removed update map 
        fitLoop : if (fitness.length <= objectivesAchieved.size()+1) { // will only occur if this is the case
            for (int i=0; i< fitness.length; i++) {
                if (indicesOfBestSolverForEachObjective[i]==worstIndex) {
                    TreeSet<Integer> set = objectivesAchieved.get(indicesOfBestSolverForEachObjective[i]);
                    set.remove(i);
                    if (set.size() == 0) { // worstIndex solution not best on any other criteria, so remove from map
                        objectivesAchieved.remove(indicesOfBestSolverForEachObjective[i]);
                    }
                    indicesOfBestSolverForEachObjective[i]=-1;
                    // now see if any others stored can act as best on this objective.
                    int replacingIndex = -1;
                    int replacingFitness = Integer.MAX_VALUE;
                    for (int j=0; j< fitness.length; j++){
                        if (indicesOfBestSolverForEachObjective[j]!=-1) { // solver found
                            if (indicesOfBestSolverForEachObjective[j]!=worstIndex) { // and not the one being removed
                                if (populationObjectives[indicesOfBestSolverForEachObjective[j]][i]) { // if it solves the criteria
                                    if (fitness[indicesOfBestSolverForEachObjective[j]] < replacingFitness) {
                                        replacingIndex = indicesOfBestSolverForEachObjective[j];
                                        replacingFitness = fitness[indicesOfBestSolverForEachObjective[j]];
                                    }
                                }
                            }
                        }
                    }
                    if (replacingIndex!=-1) { // another stored member can be used
                        indicesOfBestSolverForEachObjective[i] = replacingIndex;
                        fitnessOfBestSolver[i] = replacingFitness;
                        set = objectivesAchieved.get(replacingIndex);
                        set.add(i);
                    }
                }
            }
        }
        return worstIndex;
    }

    
    /**
     * @InheritDoc
     */
    @Override
    public int fitnessFunction(ArraySolution s, int index) {
        //System.out.println("in ff");
        int f = 0;
        boolean[] output = new boolean[problem.fitnessCases];
        
        for (int i=0; i<problem.fitnessCases; i++ ){
            // evalute new design
            InputVector.setInput(problem.inputs[i]);
            //System.out.println(objectivesSolvedByPopulation.length);
            if (s.process() != problem.targets[i]){
                f++;
                output[i] = false;
            } else {
                output[i] = true;
                //objectivesSolvedByPopulationTotalled[i]++;
            }
        }
        if (type.equals(MinimisationType.PARSIMONIOUS))
            lengths[index] = s.size();
        return processOutput(f,output, index);
    }
    
    /*
     * Method to process and update arrays which track the individual best solutions
     */
    private int processOutput(int f, boolean[] output, int index){
        populationObjectives[index] = output;
        fitness[index] = f;
        if (f < bestFitness)
            bestFitness = f;
            
        // now update arrays tracking individual objective bests
        for (int i=0; i<problem.fitnessCases; i++ ){
            boolean update = false;
            if (output[i]){
                if (indicesOfBestSolverForEachObjective[i]==-1)  // never been solved before
                    update = true;
                else    {
                    if ((f < fitnessOfBestSolver[i]) || ((f == fitnessOfBestSolver[i]) && (type.equals(MinimisationType.PARSIMONIOUS)) && (lengths[index] < lengths[indicesOfBestSolverForEachObjective[i]]))){// solved, but 's' has better overall fitness
                        update = true;
                        // now remove previously best solution on this objective from store
                        TreeSet<Integer> set = objectivesAchieved.get(indicesOfBestSolverForEachObjective[i]);
                        set.remove(i);
                        if (set.size() == 0) { // not best of any other criteria, so remove
                            objectivesAchieved.remove(indicesOfBestSolverForEachObjective[i]);
                        }
                    }
                }
                if (update) {    
                    indicesOfBestSolverForEachObjective[i] = index;
                    fitnessOfBestSolver[i] = f;
                    TreeSet<Integer> set = objectivesAchieved.get(index);
                    if (set==null){
                        set = new TreeSet<Integer>(); 
                        objectivesAchieved.put(index,set);
                    }
                    set.add(i);
                }
            }
        }
        
        return f;
    }
    
    /*
     * Draw method ensuring best on each objective are not replaced
     */
    private int drawMember(){
        // if population size is smaller than the number of objectives 
        // already achieved (plus one as two must be compared)
        if (fitness.length <= objectivesAchieved.size()+1){
            return RandomNumberGenerator.getRandom().nextInt(fitness.length);
        }
        // otherwise protect the fittest on each objective from removal
        int index = RandomNumberGenerator.getRandom().nextInt(fitness.length);
        boolean contained = false;
        for (int i : indicesOfBestSolverForEachObjective){ 
            if (index == i) {
                index = drawMember();    
                break;
            }
        }
        
        return index;    
    }
    
    /**
     * Method tracking solvers of each test problem
     * 
     * @return number of solvers
     */
    int getMapSize(){
        return objectivesAchieved.size();
    }
}
