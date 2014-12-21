
package com.vladium.utils;


// ----------------------------------------------------------------------------
/**
 * This class provides a simple API for measuring and recording CPU usage. See
 * indivual methods for details. The class is abstract because it exposes a
 * completely static API.
 *
 * @author (C) 2002, Vladimir Roubtsov
 */
public abstract class SystemInformation
{
    // public: ................................................................
    
    /**
     * A simple class to represent data snapshots taken by {@link #makeCPUUsageSnapshot}.
     */
    public static final class CPUUsageSnapshot
    {        
        public final long m_time, m_CPUTime;
        
        // constructor is private to ensure that makeCPUUsageSnapshot()
        // is used as the factory method for this class:
        private CPUUsageSnapshot (final long time, final long CPUTime)
        {
            m_time = time;
            m_CPUTime = CPUTime;
        }
        
    } // end of nested class

    
    /**
     * Minimum time difference [in milliseconds] enforced for the inputs into
     * {@link #getProcessCPUUsage(SystemInformation.CPUUsageSnapshot,SystemInformation.CPUUsageSnapshot)}.
     * The motivation for this restriction is the fact that <code>System.currentTimeMillis()</code>
     * on some systems has a low resolution (e.g., 10ms on win32). The current value
     * is 100 ms.
     */
    public static final int MIN_ELAPSED_TIME                        = 100;
    
    
    /**
     * Creates a CPU usage data snapshot by associating CPU time used with system
     * time. The resulting data can be fed into
     * {@link #getProcessCPUUsage(SystemInformation.CPUUsageSnapshot,SystemInformation.CPUUsageSnapshot)}.
     */
    public static CPUUsageSnapshot makeCPUUsageSnapshot ()
    {
        return new CPUUsageSnapshot (System.currentTimeMillis (), getProcessCPUTime ());
    }
    
    /**
     * Computes CPU usage (fraction of 1.0) between <code>start.m_CPUTime</code> and
     * <code>end.m_CPUTime</code> time points [1.0 corresponds to 100% utilization of
     * all processors].
     *
     * @throws IllegalArgumentException if start and end time points are less than
     *         {@link #MIN_ELAPSED_TIME} ms apart.
     * @throws IllegalArgumentException if either argument is null;
     */
    public static double getProcessCPUUsage (final CPUUsageSnapshot start, final CPUUsageSnapshot end)
    {
        if (start == null) throw new IllegalArgumentException ("null input: start");
        if (end == null) throw new IllegalArgumentException ("null input: end");
        if (end.m_time < start.m_time + MIN_ELAPSED_TIME)
            throw new IllegalArgumentException ("end time must be at least " + MIN_ELAPSED_TIME + " ms later than start time");
        
        return ((double)(end.m_CPUTime - start.m_CPUTime)) / (end.m_time - start.m_time);
    }
    
    /**
     * Returns the PID of the current process. The result is useful when you need
     * to integrate a Java app with external tools.
     */
    public static native int    getProcessID ();
    
    /**
     * Returns CPU (kernel + user) time used by the current process [in milliseconds].
     * The returned value is adjusted for the number of processors in the system.
     */
    public static native long   getProcessCPUTime ();
    
    /**
     * Returns CPU usage (fraction of 1.0) so far by the current process. This is a total
     * for all processors since the process creation time.
     */
    public static native double getProcessCPUUsage ();
    
    // protected: .............................................................

    // package: ...............................................................

    // private: ...............................................................
    
    
    private SystemInformation () {} // prevent subclassing
    
    private static final String SILIB = "silib";
    
    static
    {
        // loading a native lib in a static initializer ensures that it is
        // available done before any method in this class is called:
        try
        {
            System.loadLibrary (SILIB);
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println ("native lib '" + SILIB + "' not found in 'java.library.path': " + System.getProperty ("java.library.path"));
            
            throw e; // re-throw
        }
    }

} // end of class
// ----------------------------------------------------------------------------
