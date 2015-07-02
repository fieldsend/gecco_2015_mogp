package mogp;
/**
 * ComparisonProblem represents a comparison problem of arbitary size.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class ComparisonProblem extends Problem
{
    /**
     * Constructs a comparison problem (test cases and targets) with
     * the specified number of bits, if number of bit argument is odd 
     * will increase value by one to ensure it is even (as required by
     * the problem type).
     * 
     * @param bits number of bits of the problem
     */
    public ComparisonProblem(int bits)
    {
        if (bits%2 == 1)
            bits++;  // must always have an even number of bits for this problem
        super.variableNumber = bits;
        int temp = 2;
        for (int i=1; i<bits; i++)
            temp *= 2 ;
        super.fitnessCases = temp;
        System.out.println("Number of test probelms: " + fitnessCases);
        super.inputs = new boolean[super.fitnessCases][super.variableNumber];
        super.targets = new boolean[super.fitnessCases];
        
        // now fill inputs and outputs
        for (int i=0; i< super.fitnessCases; i++) {
            String binaryRep = Integer.toString(i, 2);
            char[] characterArray = binaryRep.toCharArray();
            int numTrue = 0;
            for (int j=0; j<characterArray.length; j++) {
                if (characterArray[j]=='0') {
                    inputs[i][j] = false; // not required, but clearer
                } else {
                    inputs[i][j] = true;
                    numTrue++;
                }
            }
            // now compare first bits/2 of inputs with last bits/2
            boolean same = true;
            for (int k=0; k<bits/2; k++){
                if (inputs[i][k] != inputs[i][k+bits/2]){
                    same = false;
                    break;
                }
            }
            targets[i] = same;
        }
    }
    
    
    
}

