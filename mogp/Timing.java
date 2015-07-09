package mogp;


import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

/**
 * Timing class is a helper class to track timings of
 * certain sections of code, and for the total run.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class Timing
{
    private static int calls = 0;
    private static double accruedTime = 0.0;
    private static long startTime = 0L;
    private static long endTime = 0L;
    
    private static double totalAccruedTime = 0.0;
    private static long totalStartTime = 0L;
    private static long totalEndTime = 0L;
    
    private static boolean everStarted = false;
    private static boolean totalEverStarted = false;
    
    // object to get thread clock time
    private static ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
    
    
    /**
     * Track time from now
     */
    static void setStartTime() {
        everStarted = true;
        startTime = mxbean.getCurrentThreadCpuTime();
    }
    
    /**
     * Stop tracking time
     */
    static void setEndTime() {
        endTime = mxbean.getCurrentThreadCpuTime();
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
        totalStartTime = mxbean.getCurrentThreadCpuTime();
    }
    
    
    /**
     * Stop tracking total time
     */
    static void setTotalEndTime() {
        totalEndTime = mxbean.getCurrentThreadCpuTime();
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
