package mogp;
import java.util.ArrayList;
import java.io.FileWriter;
/**
 * Object to hold and help print out results of GP runs
 * 
 * @author Jonathan Fieldsend
 * @version 1.0
 */
class Results
{
    final ArrayList<Integer> bestFitness;
    final ArrayList<Double> averageFitness;
    final ArrayList<Integer> bestSize;
    final ArrayList<Double> averageSize;
    final ArrayList<Integer> paretoSetSize;
    private FileWriter out;
    
    /**
     * Constructor to set up Results object to track GP results
     * 
     * @param filename  name of file to write to
     */
    Results(String fileName) throws java.io.IOException {
        out = new FileWriter(fileName);
        int preallocationSize = 0;
        bestFitness = new ArrayList<>(preallocationSize);
        averageFitness = new ArrayList<>(preallocationSize);
        bestSize = new ArrayList<>(preallocationSize);
        averageSize = new ArrayList<>(preallocationSize);
        paretoSetSize = new ArrayList<>(preallocationSize);
    }
    
    /**
     * Constructor allows efficiency gains to be leveraged if preallocation of
     * array lists can be exploited
     * 
     * @param filename  name of file to write to
     * @param preallocationSize number of elements to preallocate to arraylist attributes
     */
    Results(String fileName, int preallocationSize) throws java.io.IOException {
        out = new FileWriter(fileName);
        bestFitness = new ArrayList<>(preallocationSize);
        averageFitness = new ArrayList<>(preallocationSize);
        bestSize = new ArrayList<>(preallocationSize);
        averageSize = new ArrayList<>(preallocationSize);
        paretoSetSize = new ArrayList<>(preallocationSize);
    }
    
    /**
     * Helper method to write out an arbitary array to a file
     * 
     * @param array array to write out
     * @param fileName name of file to write to
     */
    static void writeArray(int[] array, String fileName) throws java.io.IOException {
        FileWriter out = new FileWriter(fileName);
        for (int i = 0; i< array.length; i++){ 
            if (i== array.length-1)
                out.write(array[i] + "\n");
            else
                out.write(array[i] + ", ");
        }
        out.close();
    }
    
    /**
     * Method writes out each of the arrays of statistics to a file
     */
    void writeOut() throws java.io.IOException {
        write(bestFitness);
        write(averageFitness);
        write(bestSize);
        write(averageSize);
        write(paretoSetSize);
        out.close();
    }
    
    /*
     * write out comma seperated numerical results to file
     */
    private void write(ArrayList<? extends Number> list) throws java.io.IOException {
        for (int i=0; i< list.size(); i++){
            if (i==list.size()-1)
                out.write(list.get(i).toString() + "\n");
            else
                out.write(list.get(i).toString() + ", ");
        }
    }
    
}
