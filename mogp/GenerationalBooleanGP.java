package mogp;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

/**
 * GenerationalBooleanGP uses a generational approach when evolving Boolean programs.
 * 
 * @author @author Jonathan Fieldsend 
 * @version 1.0
 */
public class GenerationalBooleanGP extends BooleanGP
{
    HashMap<Integer,ArraySolution> children = new HashMap<>(); // child population
    
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
        int maxTreeElements = 10000;
        if (args.length>=6) { // optional argument of max tree elements
            maxTreeElements = Integer.parseInt(args[5]);
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
            Parameters parameters = new Parameters(maxTreeElements, popSize, 1000000/popSize, 2, 0.05, 0.9);
            
            
            // dynamically construct the GPMaintence object. Would be nice to refactor this to a factory
            // method at some point
            GPMaintenance maintenance = (args[0].equals("R")) ? new StandardMaintenance(problem,parameters) :
                                    (args[0].equals("B")) ? new StandardMaintenance(problem,parameters) :
                                    (args[0].equals("BP")) ? new StandardMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("E")) ? new EliteMaintenance(problem,parameters, MinimisationType.STANDARD) :
                                    (args[0].equals("EP")) ? new EliteMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("F")) ? new FitnessSharingMaintenance(problem,parameters) :
                                    (args[0].equals("FP")) ? new FitnessSharingMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("L")) ? new LexicaseMaintenance(problem,parameters) :
                                    (args[0].equals("LP")) ? new LexicaseMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("S")) ? new BestSolver(problem,parameters) :
                                    (args[0].equals("SP")) ? new BestSolver(problem,parameters, MinimisationType.PARSIMONIOUS) :
                                    (args[0].equals("D")) ? new DominationMaintenance(problem,parameters, MinimisationType.STANDARD) :
                                    new DominationMaintenance(problem,parameters, MinimisationType.PARSIMONIOUS);
                                         
            Results results = new Results("bool_gecco2015_generational_type" + args[0] + "_problem" + args[1] + "_pop" + popSize + "_fold" + i + "_results.txt", (int) 1000000/popSize);
            if (args[0].equals("R")){
                RandomBooleanGP rgp = new RandomBooleanGP((long) i, problem, parameters, maintenance, results);
                evals[i-1] = rgp.generateSolutions();
                rgp.writeResultsFile();
            }
            else {
                GenerationalBooleanGP gp = new GenerationalBooleanGP((long) i, problem, parameters, maintenance, results);
                evals[i-1] = gp.evolve();
                gp.writeResultsFile();
            }
            
        }
        Timing.setTotalEndTime();
        Timing.updateTotalAccruedTime();
        //Results.writeArray(evals, "timing_bool_gecco2015_type" + args[0] + "_problem" + args[1] + "_pop" + popSize + "_evals.txt");
        Timing.printInfo();
        Timing.printTotalInfo();
    }

    
    GenerationalBooleanGP(long seed, Problem problem, Parameters parameters, GPMaintenance maintenance, Results results) {
        super(seed,problem,parameters,maintenance,results);
        System.out.println("Population size is " + parameters.POPULATION_SIZE);
    }
    
    @Override
    int evolve() {
        int evaluationsToSolve=-1, counter =0;
        //System.out.println("Evaluating initial random solutions: " + parameters.POPULATION_SIZE);
        // only evaluate the search population component
        for (int i=0; i<parameters.POPULATION_SIZE; i++) {
            evaluate(searchPopulation.get(i));
            counter++;
            if (searchPopulation.get(i).getSumOfTestsFailed() < bestPopulationFitness){
                bestPopulationFitness = searchPopulation.get(i).getSumOfTestsFailed();
                bestSize = searchPopulation.get(i).size();
            } else if (searchPopulation.get(i).getSumOfTestsFailed() == bestPopulationFitness){ // track smallest solver at best fitness level
                if (searchPopulation.get(i).size() < bestSize)
                    bestSize = searchPopulation.get(i).size();
            } 
            
            if ((bestPopulationFitness == 0) && (evaluationsToSolve == -1))
               evaluationsToSolve = counter; 
            
        }
        //System.out.println("Initialised");
        printStats(parameters.POPULATION_SIZE);

        if (bestPopulationFitness == 0)
            return evaluationsToSolve;
        // object to hold indices of the new search population each generation    
        List<Integer> shuffledParentIndices = new ArrayList<>(parameters.POPULATION_SIZE);
        for (int j=0; j < parameters.POPULATION_SIZE; j++)
            shuffledParentIndices.add(j);
            
        for (int i=1; i<parameters.GENERATIONS; i++) {
            
            // randomise pairings of parents to recombine this generation
            Collections.shuffle(shuffledParentIndices); 
            //System.out.println("generation " + i);
            for (int j=0; j<parameters.POPULATION_SIZE; j++ ) {
                //System.out.println("pop " + j);
            
                int parentIndex = shuffledParentIndices.get(j);
                ArraySolution parent1 = searchPopulation.get(parentIndex);
                ArraySolution child = parent1.clone(); 
                if (RandomNumberGenerator.getRandom().nextDouble() < parameters.CROSSOVER_PROBABILITY ) {
                    parentIndex = shuffledParentIndices.get(parameters.POPULATION_SIZE-j-1);
                    ArraySolution parent2 = searchPopulation.get(parentIndex);
                    child.crossover(parent2);
                } else {
                    child.mutation(parameters.MUTATION_PROBABILITY_PER_NODE);
                }
                children.put(parameters.POPULATION_SIZE+j, child);
                evaluate(child);
                counter++;
                if (child.getSumOfTestsFailed() < bestPopulationFitness){
                    bestPopulationFitness = child.getSumOfTestsFailed(); 
                    bestSize = child.size();
                } 
                else if (child.getSumOfTestsFailed() == bestPopulationFitness){ // track smallest solver at best fitness level
                    if (child.size() < bestSize)
                        bestSize = child.size();
                } 
                if ((bestPopulationFitness == 0) && (evaluationsToSolve==-1))
                    evaluationsToSolve = counter; 
            
            }
            //System.out.println("Truncate");
            // now truncate via selection
            maintenance.generateNextSearchPopulation(searchPopulation,children); 
            //System.out.println("Replace");
            
            
            System.gc(); // can grow a lot, so prompt to the garbage collector
            printStats(i*searchPopulation.size());
            if (bestPopulationFitness == 0)
                return evaluationsToSolve;
        }
        if (evaluationsToSolve==-1)
            evaluationsToSolve = parameters.GENERATIONS*searchPopulation.size() +1;
        
        return evaluationsToSolve;
    }
}
