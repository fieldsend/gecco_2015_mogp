package mogp;


/**
 * ArraySolution class represents GP tree solutions as arrays, and
 * allows their processing.
 * 
 * Note this class has a natural ordering which is inconsistent with 
 * equals.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.1
 */
public class ArraySolution implements Comparable<ArraySolution>
{
    private int[] program; // program represented as an array with elements refering to terminals or operators
    private int pointer; // reference to current element in program being processed
    private Parameters parameters; // object representing meta parameters of optimiser
    private Problem problem; // object representing problem to be solved
    private NodeSet nodeSet; // legal set of nodes used in this optimisation
    private int usedLength; // effective number of tree elements in this solution
    
    private int sumOfTestsFailed = -1;
    private boolean[] testsPassed;
    
    /**
     * Constructs an initial solution with the corresponding algorithm parameters, 
     * problem to solve and node set
     * 
     * @param parameters parameters used by the optimiser, which uses the solution 
     * @param problem problem to be solved by the solution
     * @param nodeSet node set to be employed by the solution
     */
    ArraySolution(Parameters parameters, Problem problem, NodeSet nodeSet) {
        this.parameters = parameters;
        this.problem = problem;
        this.nodeSet = nodeSet;
        program = new int[parameters.MAX_LENGTH];
        usedLength = growProgram(0,0);
    }
 
    /**
     * Copy constructer of a solution 
     * 
     * @param s an existing solution whose state to copy in the constructed 
     * ArraySolution
     */
    ArraySolution(ArraySolution s) {
        // initialise by copying state of s
        parameters = s.parameters;
        problem = s.problem;
        nodeSet = s.nodeSet;
        usedLength = s.usedLength;
        program = new int[s.program.length];
        for (int i=0; i<program.length; i++) // only need to copy the elements in use
            program[i] = s.program[i];
    }

    /**
     * Remove the program represented by the current solution
     */
    void clean() {
        program = null;
    }
    
    /**
     * Returns the number of elements in this program tree
     * 
     * @return number of tree elements
     */
    public int size(){
        return usedLength;
    }
   
    /*
     * Helper method to grow initial tree
     */
    private int growProgram(int position, int depth) {
        // 50/50 terminal or operator
        depth++;
        int nodeType = RandomNumberGenerator.getRandom().nextInt(2);
        if (position == 0) // first node always an operator
            nodeType = 1; 
        else if (position > parameters.MAX_LENGTH){
            System.out.println("Growing too LONG!!!!");
            return -1;
        }
        else if ( depth > parameters.MAX_DEPTH ) // do not grow beyond max depth initially
            nodeType = 0;
        if (nodeType == 0) {
            program[position] = nodeSet.getRandomTerminalValue();   
            return ++position;
        } else {
            program[position] = nodeSet.getRandomOperatorValue();
            int positionAfterSubTreeGrown = growProgram(++position,depth);
            if (positionAfterSubTreeGrown < 0){
                System.out.println("Growing too LONG!!!!");
                return -1;
            }
            return growProgram(positionAfterSubTreeGrown,depth);
        }
    }

    /**
     * Run the tree program and get the output
     * 
     * @return output of the tree program stored in this ArraySolution
     */
    public boolean process() {
        pointer = 0;
        return runProgram();
    }

    /*
     * Helper method to recursively process tree branches represented in the array
     */
    private boolean runProgram() {
        int nodeValue = program[pointer++];
        if (nodeSet.isOperator(nodeValue)){
            return nodeSet.processOperator(nodeValue, runProgram(), runProgram());
        }
        return nodeSet.processTerminal(nodeValue);
    }
    
    /**
     * Clones this ArraySolution
     * 
     * @return a clone of this ArraySolution
     */
    public ArraySolution clone() {
        return new ArraySolution(this);
    }
    
    /**
     * Mutate the members of this ArraySolution, given the probability to mutate each element
     * 
     * @param probabilityToMutate probability of element mutation
     */
    void mutation(double probabilityToMutate) {
        int mutations = 0;
        for (int i=0; i<usedLength; i++)  {
            if (RandomNumberGenerator.getRandom().nextDouble() < probabilityToMutate ) {
                mutate(i);
                mutations++;
            }
        }
        // ensure at least one mutation occurs always
        if (mutations == 0 )  
            mutate(RandomNumberGenerator.getRandom().nextInt(size()));
    }

    /*
     * Helper method which mutates the index element of the array
     */
    private void mutate(int index) {
        if (nodeSet.isOperator(program[index])) {
            program[index] = nodeSet.mutateToOtherOperator(program[index]);
        } else {
            int val = program[index];
            while (val == program[index]) // ensure not mutated into itself
                val = nodeSet.getRandomTerminalValue();
            program[index] = val;
        }
    }
    
    /**
     * Performs crossover between this solution and the breedingPartner solution, 
     * with the state of this solution replaced with that of the child
     * 
     * @param breedingPartner the solution to crossover this solution with
     */
    void crossover(ArraySolution breedingPartner) {
        int subTreeStart, subTreeEnd, partnerSubTreeStart, partnerSubTreeEnd, childLength, oldSubTreeLength, newSubTreeLength;
        
        do {
            subTreeStart =  RandomNumberGenerator.getRandom().nextInt(size());
            subTreeEnd = traverse(subTreeStart);

            partnerSubTreeStart =  RandomNumberGenerator.getRandom().nextInt(breedingPartner.size());
            partnerSubTreeEnd = breedingPartner.traverse(partnerSubTreeStart);
            oldSubTreeLength = subTreeEnd - subTreeStart;
            newSubTreeLength = partnerSubTreeEnd - partnerSubTreeStart;
            // child length is original length + length of added tree - length of removed tree
            childLength = size() + newSubTreeLength - oldSubTreeLength;
            
        } while (childLength > parameters.MAX_LENGTH);
        
        // until the subtree can remain unchanged
        if (oldSubTreeLength > newSubTreeLength) {
            int diff = oldSubTreeLength - newSubTreeLength;
            // copy in subtree
            for (int i = 0; i< newSubTreeLength; i++)
                program[subTreeStart+i] = breedingPartner.program[partnerSubTreeStart+i];
            // shift other node values down
            for (int i = subTreeEnd ; i<usedLength; i++)
                program[i-diff] = program[i]; 
            usedLength -= diff;
        } else if (oldSubTreeLength < newSubTreeLength) {
            int diff = newSubTreeLength - oldSubTreeLength;
            // shift other node values up
            for (int i = usedLength-1; i>=subTreeEnd; i--)
                program[i+diff] = program[i]; 
            // copy in subtree
            for (int i = 0; i< newSubTreeLength; i++)
                program[subTreeStart+i] = breedingPartner.program[partnerSubTreeStart+i];
            usedLength += diff;
        } else { // subtrees same length
            for (int i = 0; i< oldSubTreeLength; i++)
                program[subTreeStart+i] = breedingPartner.program[partnerSubTreeStart+i];
        }
    }
    
    /*
     * Helper method to traverse the tree from the element at index to the 
     * next element in the tree representation given the value of the element 
     * at index, finally returning the value of the terminal (leaf) reached
     */
    private int traverse(int index ) {
        if ( nodeSet.isOperator(program[index]))
            return traverse( traverse( ++index ) );
            
        return ++index;
    }
    
    /**
     * Returns the sum of tests failed by this ArraySolution. If this
     * solution has not been evaluated, will return -1
     * 
     * @returns total number of tests failed
     */
    public int getSumOfTestsFailed() {
        return sumOfTestsFailed;
    }
    
    /**
     * Returns an array of booleans indicating if each test has been passed
     * of not. If this solution has not been evaluated, will return null
     * 
     * @returns array of booleans, the ith element indicating if the ith
     * test has been passed (true) or failed (false)
     */
    public boolean[] getTestsPassed() {
        return testsPassed;
    }
    
    
    /**
     * Sets tests passed by a solution once it has been evaluated
     * 
     * @param array of booleans, the ith element indicating if the ith
     * test has been passed (true) or failed (false)
     */
    public void setTestsPassed(boolean[] testsPassed){
        this.testsPassed = testsPassed;
        // now calaulated the total number of failed tests
        this.sumOfTestsFailed = 0;
        for (boolean b : testsPassed)
            if (b == false)
                sumOfTestsFailed++;
    }
    
    @Override
    public int compareTo(ArraySolution a) {
        if (this.sumOfTestsFailed < a.sumOfTestsFailed)
            return -1;
        if (this.sumOfTestsFailed == a.sumOfTestsFailed)
            return 0;
        return 1;    
    }
}
