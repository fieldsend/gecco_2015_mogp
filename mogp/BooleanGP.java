package mogp;
import java.util.ArrayList;
import java.io.IOException;

/**
 * BooleanGP class, class to set up and run experiments from GECCO 2015 paper
 * Strength Through Diversity: Disaggregation and Multi-Objectivisation Approaches 
 * for Genetic Programming, by Fieldsend and Moraglio.
 * 
 * Evolves a population of basic GPs using various diversity preservation approaches. 
 * 
 * Note that the original paper contains many details describing the optimiser design,
 * and the GP functionality is closely modelled on the TinyGP.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class BooleanGP
{
    // attributes used by the optimiser to store and compare solutions
    ArrayList<ArraySolution> searchPopulation = new ArrayList<>(); //search population
    private long seed; // random seed used
    final Problem problem; // problem to be solved
    final Parameters parameters; // parameters object (meta parameters of run)
    private double averageLength; // holder for average tree size
    private int bestPopulationFitness = Integer.MAX_VALUE; // initial value for best fitness (initialise as worst possible)
    private int bestSize = Integer.MAX_VALUE; // initial value for best size (initialise as worst possible)
    private double averagePopulationFitness, averagePopulationLength; // quality tracking attributes 
    private GPMaintenance maintenance; // maintance regime used
    private Results results; // results object
    private NodeSet nodeSet; // node set object
    /**
     * Method to run sets of GP experiments.
     * <p>
     * Five arguments are required: 
     * <p>
     * a string for the search type (R, B, BP, F, FP, S, SP, D or DP);
     * <p>
     * a string for the problem number. 2, 4 and 8 denote the multiplexer problems of those values, 16, 
     * 17 and 18 denote the 6, 7 and 8-parity problems, 106, 107 and 108 denote the 6, 7 and 8-majority 
     * problems and 206, 208 and 210 denote the 6, 8 and 10-comparitor problems;  
     * <p>
     * a string containing the integer population size (minimum value 1);
     * <p>
     * a string containing the integer value for the number to start the folds from (minumum value 1);
     * <p>
     * a string containing the integer value for the end number of folds from (minumum value is the fold start).
     * <p>
     * Not all exceptions caught -- ensure that the strings contain the correct format (e.g. integers).
     * 
     * @param args array of string elements containing experiement set up values
     */
    public static void main(String[] args) throws IOException {
        if (args.length<5){
            System.out.println("Insufficient arguments, requires: maintenence type (R, B, BP, F, FP, S, SP, D or DP) problem type (2, 4 or 8) population size (postive integer) fold start number fold end number");
            System.exit(1);
        }
        int fold_start = Integer.parseInt(args[3]);
        int fold_end = Integer.parseInt(args[4]);
        if (fold_start <1){
            System.out.println("minimum fold start number is 1: " + fold_start);
            System.exit(1);
        }
        if (fold_start > fold_end) {
            System.out.println("fold end number must be higher or equal to fold end number: " + fold_start + " " + fold_end);
            System.exit(1);
        }
        int[] evals = new int[50];
        int popSize = Integer.parseInt(args[2]);
        if (popSize < 1) {
            System.out.println("Population size must be at least 1, has been set to" + popSize);
            System.exit(1);
        }
        Timing.setTotalStartTime();
        for (int i=fold_start; i<=fold_end; i++)   {
            System.out.println("FOLD: " + i);
            Problem problem = (args[1].equals("2")) ? new TwoToOneMultiplexer() :
                          (args[1].equals("4")) ? new FourToOneMultiplexer() :
                          (args[1].equals("8")) ? new EightToOneMultiplexer() :
                          (Integer.parseInt(args[1])<=100) ? new EvenNParity(Integer.parseInt(args[1])-10) :
                          (Integer.parseInt(args[1])<=200) ? new MajorityProblem(Integer.parseInt(args[1])-100) :
                          new ComparisonProblem(Integer.parseInt(args[1])-200);
                          
            // Meta-parameters used in the GECCO paper           
            Parameters parameters = new Parameters(10000, popSize, 1000000/popSize, 2, 0.05, 0.9);
            
            // dynamically construct the GPMaintence object. Would be nice to refactor this to a factory
            // method at some point
            GPMaintenance maintenance = (args[0].equals("R")) ? new StandardMaintenance(problem,parameters) :
                                    (args[0].equals("B")) ? new StandardMaintenance(problem,parameters) :
                                    (args[0].equals("BP")) ? new StandardMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("F")) ? new FitnessSharingMaintenance(problem,parameters) :
                                    (args[0].equals("FP")) ? new FitnessSharingMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("S")) ? new BestSolver(problem,parameters) :
                                    (args[0].equals("SP")) ? new BestSolver(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("D")) ? new DominationMaintenance(problem,parameters, MinimisationType.STANDARD) :
                                    new DominationMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS);
                                         
            Results results = new Results("bool_gecco2015_type" + args[0] + "_problem" + args[1] + "_pop" + popSize + "_fold" + i + "_results.txt", (int) 1000000/popSize);
            if (args[0].equals("R")){
                RandomBooleanGP rgp = new RandomBooleanGP((long) i, problem, parameters, maintenance, results);
                evals[i-1] = rgp.generateSolutions();
                rgp.writeResultsFile();
            }
            else {
                BooleanGP gp = new BooleanGP((long) i, problem, parameters, maintenance, results);
                evals[i-1] = gp.evolve();
                gp.writeResultsFile();
            }
            
        }
        Timing.setTotalEndTime();
        Timing.updateTotalAccruedTime();
        Results.writeArray(evals, "timing_bool_gecco2015_type" + args[0] + "_problem" + args[1] + "_pop" + popSize + "_evals.txt");
        Timing.printInfo();
        Timing.printTotalInfo();
    }

    /*
     * constructor to set up optimiser prior to running
     */
    private BooleanGP(long seed, Problem problem, Parameters parameters, GPMaintenance maintenance, Results results) {
        if (seed>=0)
            RandomNumberGenerator.setSeed(seed);
        this.problem = problem;
        this.parameters = parameters;
        this.maintenance = maintenance;
        this.results = results;
        nodeSet = new NodeSet(problem.variableNumber);
        for (int i=0; i<parameters.POPULATION_SIZE; i++){
            ArraySolution s = new ArraySolution(parameters,problem, nodeSet);
            while(s.size() > parameters.MAX_LENGTH)
                s = new ArraySolution(parameters, problem, nodeSet);
            searchPopulation.add(s);
        }
    }
    
    /*
     * Method runs the GP till all generations are exhausted
     */
    private int evolve() {
        int worstSolutionIndex, tempFitness, evaluationsToSolve=-1, counter =0;
        for (int i=0; i<parameters.POPULATION_SIZE; i++) {
            tempFitness = evaluate(searchPopulation.get(i),i);
            counter++;
            if (tempFitness < bestPopulationFitness){
                bestPopulationFitness = tempFitness;
                bestSize = searchPopulation.get(i).size();
            } else if (tempFitness == bestPopulationFitness){ // track smallest solver at best fitness level
                if (searchPopulation.get(i).size() < bestSize)
                    bestSize = searchPopulation.get(i).size();
            } 
            
            if ((bestPopulationFitness == 0) && (evaluationsToSolve==-1))
               evaluationsToSolve = counter; 
            
        }

        printStats(0,searchPopulation.size());

        if (bestPopulationFitness == 0)
                return evaluationsToSolve;
                
        for (int i=1; i<parameters.GENERATIONS; i++) {
                
            for (int j=0; j<searchPopulation.size(); j++ ) {
                int parentIndex = maintenance.tournament();
                ArraySolution parent1 = searchPopulation.get(parentIndex);
                ArraySolution child = parent1.clone(); 
                if (RandomNumberGenerator.getRandom().nextDouble() < parameters.CROSSOVER_PROBABILITY ) {
                    parentIndex = maintenance.tournament();
                    ArraySolution parent2 = searchPopulation.get(parentIndex);
                    while (parent1==parent2){
                        parentIndex = maintenance.tournament();
                        parent2 = searchPopulation.get(parentIndex);
                    }
                    child.crossover(parent2);
                } else {
                    child.mutation(parameters.MUTATION_PROBABILITY_PER_NODE);
                }
                worstSolutionIndex = maintenance.negativeTournament();
                searchPopulation.get(worstSolutionIndex).clean();// gives less work to the garbage collector, which can sometimes complain if lots of time is spent dereferencing maps
                searchPopulation.set(worstSolutionIndex, child);
                tempFitness = evaluate(child, worstSolutionIndex);
                counter++;
                if (tempFitness < bestPopulationFitness){
                    bestPopulationFitness = tempFitness; 
                    bestSize = searchPopulation.get(worstSolutionIndex).size();
                } 
                else if (tempFitness == bestPopulationFitness){ // track smallest solver at best fitness level
                    if (searchPopulation.get(worstSolutionIndex).size() < bestSize)
                        bestSize = searchPopulation.get(worstSolutionIndex).size();
                } 
                if ((bestPopulationFitness == 0) && (evaluationsToSolve==-1))
                    evaluationsToSolve = counter; 
            
            }
            System.gc(); // can grow a lot, so prompt to the garbage collector
            printStats(i,searchPopulation.size());
            if (bestPopulationFitness == 0)
                return evaluationsToSolve;
        }
        if (evaluationsToSolve==-1)
            evaluationsToSolve = parameters.GENERATIONS*searchPopulation.size() +1;
        return evaluationsToSolve;
    }

    /*
     * Method writes out the results to a file using the results attribute
     */
    private void writeResultsFile() throws java.io.IOException {
        results.writeOut();
    }
    
    /*
     * Method calculates the various tracked statistics of the optimisation run
     */
    private void calculateAverages() {
        averagePopulationFitness=0.0;
        averagePopulationLength=0.0;
        for (int k=0; k<parameters.POPULATION_SIZE; k++ ){
            averagePopulationFitness += maintenance.getFitness(k);
            averagePopulationLength += searchPopulation.get(k).size();
        }
        averagePopulationFitness /= parameters.POPULATION_SIZE; 
        averagePopulationLength /= parameters.POPULATION_SIZE;
    }

    /*
     * Method evaluates the fitness of a population
     */
    private int evaluate(ArraySolution s, int index) {
        return maintenance.fitnessFunction(s, index);
    }

    /*
     * Method prints out various statistics to the terminal window
     */
    private void printStats(int generation, int numberPerGeneration){
        calculateAverages();
        
        if ( ((generation+1) * numberPerGeneration)%10000 == 0 ) {
            System.out.println("Generation: " + generation + ", " +
            "Av. tree size: " + averagePopulationLength + ", " +
            "Av. population fitness: " + averagePopulationFitness + ", " +
            "Size of best: " + bestSize  + ", " +
            "Best fitness: " + bestPopulationFitness);
        
            System.out.println("Memory used: " + (Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory())/1048576 + "M");    
        }
        
        if (maintenance instanceof DominationMaintenance){
            int setSize = ((DominationMaintenance) maintenance).getParetoSetSize();
            System.out.println("Pareto set size: " + setSize);
            results.paretoSetSize.add(setSize);
        }
        
        if (maintenance instanceof BestSolver){
            int setSize = ((BestSolver) maintenance).getMapSize();
            System.out.println("Map size: " + setSize);
            results.paretoSetSize.add(setSize);
        }
        
        results.bestFitness.add(bestPopulationFitness);
        results.averageFitness.add(averagePopulationFitness);
        results.averageSize.add(averagePopulationLength);
        results.bestSize.add(bestSize);
    }

}

