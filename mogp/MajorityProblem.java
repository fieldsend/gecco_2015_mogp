package mogp;

/**
 * MajorityProblem represents a majority of arbitary size.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class MajorityProblem extends Problem
{
    /**
     * Constructs a majority problem (test cases and targets) with
     * the specified number of bits
     * 
     * @param bits number of bits of the problem
     */
    public MajorityProblem(int bits)
    {
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
            
            if (numTrue > bits-numTrue) // if majority true
                targets[i] = true;
            else
                targets[i] = false; // not required, but clearer
        }
    }
}

