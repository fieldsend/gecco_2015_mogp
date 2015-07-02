package mogp;

/**
 * Problem class is an abstract compound data structure that
 * concrete problems extend and add behaviour to.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public abstract class Problem
{
    int variableNumber, fitnessCases; // holders for number of inputs and test cases
    boolean[][] inputs; // matrix of inputs
    boolean[] targets; // array of targets (one target for each input vector in the matrix of inputs)
}