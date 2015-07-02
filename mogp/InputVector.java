package mogp;
import java.util.ArrayList;
/**
 * InputVector is a static level data structure holding 
 * an array of boolean inputs. This gives a single place for
 * the data to be held, rather then replicating it across solutions. 
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class InputVector
{
    private static boolean[] input;
    
    /**
     * Method to replace current contents
     * 
     * @param input array of boolean inputs
     */
    static void setInput(boolean[] input) {
        InputVector.input = input;
    }
    
    /**
     * Method to access input array at index
     * 
     * @param index index of element to return
     * @return value at index
     */
    static boolean getValue(int index) {
        return input[index];
    }
    
    /**
     * Method to get number of element of input
     * 
     * @return number of array elements
     */
    static int getInputSize(){
        return input.length;
    }
}
