package mogp;
import java.util.HashMap;

/**
 * Class representing the legal set of nodes in the GP
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class NodeSet
{
    private int numberOfBinaryGates = 16;
    private int numberOfTerminals;
    private HashMap<boolean[],Integer> booleanArrayToIntMap = new HashMap<>(); // map from boolean array to gate value
    private HashMap<Integer,boolean[]> intToBooleanArrayMap = new HashMap<>(); // map from gate value to boolean array
    private HashMap<Integer,int[]> closeOperators = new HashMap<>(); // map from gate to array containing neighbouring gate values
    
    
    NodeSet(int numberOfTerminals){
        this.numberOfTerminals = numberOfTerminals;
        for (int i=0; i<16; i++) {
            int v=i;
            int index = 0;
            boolean[] temp = new boolean[4];
            // fill temp array
            for (int c = 8; c>=1; c/=2 ){
                v = binaryStore(temp, v, c, index);
                index++;
            }
            // use maps for efficiency, as saves recalculating mapping each time it is required
            // in later method for the binary (boolean) array to gate value or vice versa
            booleanArrayToIntMap.put(temp,i); 
            intToBooleanArrayMap.put(i,temp);
        }
        // propogate map from gate value to its neighbours
        for (int i=0; i<16; i++) {
           int[] temp = new int[4];
           int index=0;
           for (int j=0; j<16; j++) {
               if (arrayDifference(intToBooleanArrayMap.get(i),intToBooleanArrayMap.get(j))==1){
                   temp[index] = j;
                   index++;
                } 
           }
           closeOperators.put(i,temp);
        }
    }

    /*
     * Method counts bit different between boolean arrays
     */
    private static int arrayDifference(boolean[] a, boolean[] b){
        int v = 0;
        for (int i=0; i<4; i++)
            if (!a[i]==b[i])
                v++;
        return v;
    }
    
    /*
     * Method to build up bit string (boolean array) representation of a value
     */
    private static int binaryStore(boolean[] a, int v, int c, int index){
        if (v>=c){
            v-=c;
            a[index] = true;
        } else {
            a[index] = false;
        }
        return v;
    }

    /**
     * Method returns a (uniform) random terminal value
     * 
     * @return random terminal value
     */
    int getRandomTerminalValue() {
        return RandomNumberGenerator.getRandom().nextInt(numberOfTerminals);
    }
    
    /**
     * Method returns a (uniform) random operator value
     * 
     * @return random terminal value
     */
    int getRandomOperatorValue() {
        return RandomNumberGenerator.getRandom().nextInt(numberOfBinaryGates)+numberOfTerminals;
    }
    
    /**
     * Method returns true if argument is an operator value, false otherwise
     * 
     * @param i node/leaf value
     * @return true if argument i is an operator value, false otherwise
     */
    boolean isOperator(int i){
        return i >= numberOfTerminals;
    }

    /**
     * Method returns value of terminal element at index (from bit string, i.e. boolean array, representation) 
     * 
     * @param i index of array
     * @return boolean vale of array at index
     */
    boolean processTerminal(int i){
        return InputVector.getValue(i);
    }

    /**
     * Method gives output of gate with operator value i given the two inputs a and b
     * 
     * @param i operator value (includes number of terminals)
     * @param a first gate input
     * @param b second gate input
     * @return gate output
     */
    boolean processOperator(int i, boolean a, boolean b){
        i -= numberOfTerminals;
        boolean[] v = intToBooleanArrayMap.get(i);
        return (a && b) ? v[0] :
               (a && !b) ? v[1] :
               (!a && b) ? v[2] :
               v[3];
    }

    /**
     * Returns operator close (neighbour) to argument operator value. Returns -1 if the argument 
     * is not an operator value
     * 
     * @param operatorValue value of operator
     * @return a random neighbour operator value
     */
    int mutateToClose(int operatorValue){
        if (!isOperator(operatorValue))
            return -1;
        
        operatorValue -= numberOfTerminals;
        int[] close =  closeOperators.get(operatorValue);  
        return close[RandomNumberGenerator.getRandom().nextInt(4)]+numberOfTerminals;
    }
    
    /**
     * Returns (uniform) random operator excluding the argument operator value. 
     * Returns -1 if the argument is not an operator value
     * 
     * @param operatorValue value of operator
     * @return a random operator value
     */
    int mutateToOtherOperator(int operatorValue){
        if (!isOperator(operatorValue))
            return -1;
            
        int val = operatorValue;
        while (val == operatorValue)
            val = RandomNumberGenerator.getRandom().nextInt(numberOfBinaryGates)+numberOfTerminals;
        
        return val;
    }
}
