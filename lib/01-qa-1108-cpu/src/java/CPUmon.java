
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import com.vladium.utils.CPUUsageThread;
import com.vladium.utils.SystemInformation;

// ----------------------------------------------------------------------------
/**
 * A demo of {@link SystemInformation} and {@link CPUUsageThread} functionality.
 * This class starts an instance of CPU usage monitoring thread, registers itself
 * as a CPU usage event listener, and launches another application.
 * Usage:
 * <PRE>
 *   java -Djava.library.path=(silib native lib dir) CPUmon AnotherApp ...args...
 * </PRE>
 *
 * @author (C) 2002, Vladimir Roubtsov
 */
public class CPUmon implements CPUUsageThread.IUsageEventListener
{
    // public: ................................................................
    
    
    public static void main (final String [] args) throws Exception
    {
        if (args.length == 0)
        {
            throw new IllegalArgumentException ("usage: CPUmon <app_main_class> <app_main_args...>");
        }
        
        final CPUUsageThread monitor = CPUUsageThread.getCPUThreadUsageThread ();
        final CPUmon _this = new CPUmon ();
        
        final Class app = Class.forName (args [0]);
        final Method appmain = app.getMethod ("main", new Class [] {String[].class});
        final String [] appargs = new String [args.length - 1];
        System.arraycopy (args, 1, appargs, 0, appargs.length);
        
        monitor.addUsageEventListener (_this);
        monitor.start ();
        appmain.invoke (null, new Object [] {appargs});
    }
        
    public CPUmon ()
    {
        m_PID = SystemInformation.getProcessID ();
        
        m_format = new DecimalFormat ();
        m_format.setMaximumFractionDigits (1);
    }
    
    /**
     * Implements {@link CPUUsageThread.IUsageEventListener}. Simply
     * prints the current process PID and CPU usage since last snapshot
     * to System.out.
     */
    public void accept (final SystemInformation.CPUUsageSnapshot event)
    {
        if (m_prevSnapshot != null)
        {
            System.out.println ("[PID: " + m_PID + "] CPU usage: " +
                m_format.format (100.0 * SystemInformation.getProcessCPUUsage (m_prevSnapshot, event)) + "%");
        }
       
        m_prevSnapshot = event;
    }

    // protected: .............................................................

    // package: ...............................................................

    // private: ...............................................................
    
    
    private final int m_PID; // process ID
    private final DecimalFormat m_format;
    private SystemInformation.CPUUsageSnapshot m_prevSnapshot;

} // end of class
// ----------------------------------------------------------------------------
