package mogp;

/**
 * Timing class is a helper class to track timings of
 * certain sections of code, and for the total run.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class Timing
{
    static int calls = 0;
    static double accruedTime = 0.0;
    static long startTime = 0L;
    static long endTime = 0L;
    
    static double totalAccruedTime = 0.0;
    static long totalStartTime = 0L;
    static long totalEndTime = 0L;
    
    static boolean everStarted = false;
    static boolean totalEverStarted = false;
    
    /**
     * Track time from now
     */
    static void setStartTime() {
        everStarted = true;
        startTime = System.nanoTime();
    }
    
    /**
     * Stop tracking time
     */
    static void setEndTime() {
        endTime = System.nanoTime();
        if (everStarted==false) // if never started then want difference to be zero
            startTime = endTime;
    }
    
    /**
     * Calculate difference between last start and end times tracked, and update accrued time    
     */
    static void updateAccruedTime() {
        double difference = (endTime - startTime)/1e6;
        accruedTime += difference;
    }
    
    /**
     * Track total time from now
     */
    static void setTotalStartTime() {
        totalEverStarted = true;
        totalStartTime = System.nanoTime();
    }
    
    
    /**
     * Stop tracking total time
     */
    static void setTotalEndTime() {
        totalEndTime = System.nanoTime();
        if (totalEverStarted==false) // if never started then want difference to be zero
            startTime = endTime;
    }
    
    /**
     * Calculate difference between total start and end times, and update total accrued time    
     */
    static void updateTotalAccruedTime() {
        double difference = (totalEndTime - totalStartTime)/1e6;
        totalAccruedTime += difference;
    }
    
    /**
     * Update calls tracker by one
     */
    static void incrementCalls() {
        calls++;
    }
    
    /**
     * Display timing info to window for time variable and calls
     */
    static void printInfo() {
        System.out.println("total time: " + accruedTime + " milli seconds");
        System.out.println("calls: " + calls + " times");
        System.out.println("Av time per gen: " + accruedTime/calls + " milli seconds");
    }
    
    /**
     * Display timing info to window for total time variable
     */
    static void printTotalInfo() {
        System.out.println("total time: " + totalAccruedTime + " milli seconds");
        System.out.println("Percentage total: " + 100*accruedTime/totalAccruedTime + "%");
    }
    
}
