package mogp;

import java.util.HashMap;

/**
 * Version of the Boolean GP that acts like random
 * to be a baseline comparison, i.e. generates putative solutions
 * at random to match the number of overall function evaluations
 * of other approaches.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class RandomBooleanGP
{
    private long seed;
    final Problem problem;
    final Parameters parameters;
    private int bestPopulationFitness = Integer.MAX_VALUE;
    private int bestSize = Integer.MAX_VALUE;
    private Results results;
    private NodeSet nodeSet;
    
    private GPMaintenance maintenance;
    /**
     * Constructor to set up random search optimiser prior to running
     * 
     * @param seed seed to be used in the random number generator
     * @param problem problem to be optimised
     * @param parameters meta-parameters the optimiser should use
     * @param maintenance GPMaintenance object -- note that only the fitness function evaluation in it will be used
     * @param results object to track performance through a run
     */
    RandomBooleanGP(long seed, Problem problem, Parameters parameters, GPMaintenance maintenance, Results results) {
        if (seed>=0)
            RandomNumberGenerator.setSeed(seed);
        this.problem = problem;
        this.parameters = parameters;
        this.results = results;
        this.maintenance = maintenance;
        
        nodeSet = new NodeSet(problem.variableNumber);
    }

    /**
     * Generates and evaluates the random solutions in the run
     * 
     * @return returns the total nunmber of evaluations required to solve the problem, if not solved, returns
     * the number of evaluations set in the meta-parameters plus 1 
     */
    int generateSolutions() {
        int worstSolutionIndex, tempFitness, evaluationsToSolve=-1, counter =0;
        for (int i=0; i<parameters.POPULATION_SIZE*parameters.GENERATIONS; i++) {
            ArraySolution s = new ArraySolution(parameters,problem, nodeSet);
            while(s.size() > parameters.MAX_LENGTH)
                s = new ArraySolution(parameters, problem, nodeSet);
            evaluate(s);
            tempFitness = s.getSumOfTestsFailed();
            counter++;
            
            if (tempFitness < bestPopulationFitness){
                bestPopulationFitness = tempFitness;
                bestSize = s.size();
            } else if (tempFitness == bestPopulationFitness){ // track smallest solver at best fitness level
                if (s.size() < bestSize)
                    bestSize = s.size();
            } 
            
            if ((bestPopulationFitness == 0) && (evaluationsToSolve==-1)){
               evaluationsToSolve = counter; 
               return evaluationsToSolve;
            }
            if (counter%parameters.POPULATION_SIZE==0)
                printStats(counter);
        }
        printStats(counter);
        if (evaluationsToSolve==-1) // if not solved, return the total expended plus one
            evaluationsToSolve = parameters.GENERATIONS*parameters.POPULATION_SIZE +1;
        
        return evaluationsToSolve;
    }

    /**
     * Method write results to a file
     */
    public void writeResultsFile() throws java.io.IOException {
        results.writeOut();
    }
    
    /*
     * Method evaluates the fitness of a population
     */
    void evaluate(ArraySolution s) {
        maintenance.evaluateFitness(new HashMap<Integer, ArraySolution>(),s);
    }


    /*
     * Helper method to print statistics
     */
    private void printStats(int evals){
        System.out.println("evals: " + evals + ", " +
            "Size of best: " + bestSize  + ", " +
            "Best fitness: " + bestPopulationFitness);
            
        System.out.println("Memory used: " + (Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory())/1048576 + "M");    
            
        results.bestFitness.add(bestPopulationFitness);
        results.averageFitness.add(-1.0); // not sensible to track this, so -1.0 added
        results.averageSize.add(-1.0); // not sensible to track this, so -1.0 added
        results.bestSize.add(bestSize);
    }
}
