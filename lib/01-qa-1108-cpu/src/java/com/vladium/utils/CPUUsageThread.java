
package com.vladium.utils;

import java.util.ArrayList;

// ----------------------------------------------------------------------------
/**
 * This class shows a sample API for recording and reporting periodic CPU usage
 * shapshots. See <code>CPUmon</code> class for a usage example.
 *
 * @author (C) 2002, Vladimir Roubtsov
 */
public class CPUUsageThread extends Thread
{
    // public: ................................................................
    
    /**
     * Any client interested in receiving CPU usage events should implement
     * this interface and call {@link #addUsageEventListener} to add itself
     * as the event listener.
     */
    public static interface IUsageEventListener
    {
        void accept (SystemInformation.CPUUsageSnapshot event);
        
    } // end of nested interface
    
    
    /**
     * Default value for the data sampling interval [in milliseconds]. Currently
     * the value is 500 ms.
     */
    public static final int DEFAULT_SAMPLING_INTERVAL   = 500;   
    
    
    /**
     * Factory method for obtaining the CPU usage profiling thread singleton.
     * The first call constructs the thread, whose sampling interval will
     * default to {@link #DEFAULT_SAMPLING_INTERVAL} and can be adjusted via
     * {@link #setSamplingInterval}.
     */
    public static synchronized CPUUsageThread getCPUThreadUsageThread ()
    {
        if (s_singleton == null)
        {
            s_singleton = new CPUUsageThread (DEFAULT_SAMPLING_INTERVAL);
        }
        
        return s_singleton;
    }
    
    /**
     * Sets the CPU usage sampling interval.
     *
     * @param samplingInterval new sampling interval [in milliseconds].
     * @return previous value of the sampling interval.
     *
     * @throws IllegalArgumentException if 'samplingInterval' is not positive.
     */
    public synchronized long setSamplingInterval (final long samplingInterval)
    {
        if (samplingInterval <= 0)
            throw new IllegalArgumentException ("must be positive: samplingInterval");
 
        final long old = m_samplingInterval;
        m_samplingInterval = samplingInterval;
        
        return old;
    }
    
    /**
     * Adds a new CPU usage event listener. No uniqueness check is performed.
     */
    public synchronized void addUsageEventListener (final IUsageEventListener listener)
    {
        if (listener != null) m_listeners.add (listener);
    }
    
    /**
     * Removes a CPU usage event listener [previously added via {@link addUsageEventListener}].
     */
    public synchronized void removeUsageEventListener (final IUsageEventListener listener)
    {
        if (listener != null) m_listeners.remove (listener);
    }
    
    /**
     * Records and broadcasts periodic CPU usage events. Follows the standard interruptible
     * thread termination model.
     */
    public void run ()
    {
        while (! isInterrupted ())
        {
            final SystemInformation.CPUUsageSnapshot snapshot = SystemInformation.makeCPUUsageSnapshot ();            
            notifyListeners (snapshot);
            
            final long sleepTime;
            synchronized (this)
            {
                sleepTime = m_samplingInterval;
            }
            
            // for simplicity, this assumes that all listeners take a short time to process
            // their accept()s; if that is not the case, you might want to compensate for
            // that by adjusting the value of sleepTime:
            try
            {
                sleep (sleepTime);
            }
            catch (InterruptedException e)
            {
                return;
            }
        }
        
        // reset the singleton field [Threads are not restartable]:
        synchronized (CPUUsageThread.class)
        {
            s_singleton = null;
        }
    }

    // protected: .............................................................
    
    
    /**
     * Protected constructor used by {@link getCPUThreadUsageThread} singleton
     * factory method. The created thread will be a daemon thread.
     */
    protected CPUUsageThread (final long samplingInterval)
    {
        setName (getClass ().getName () + " [interval: " + samplingInterval + " ms]");
        setDaemon (true);
        
        setSamplingInterval (samplingInterval);
        
        m_listeners = new ArrayList ();
    }

    // package: ...............................................................

    // private: ...............................................................
    
    
    /**
     * Effects the listener notification.
     */
    private void notifyListeners (final SystemInformation.CPUUsageSnapshot event)
    {
        final ArrayList /* <IUsageEventListener> */ listeners;
        synchronized (this)
        {
            listeners = (ArrayList) m_listeners.clone ();
        }
        
        for (int i = 0; i < listeners.size (); ++ i)
        {
            ((IUsageEventListener) listeners.get (i)).accept (event);
        }
    }
    
    
    private long m_samplingInterval; // assertion: non-negative
    private final ArrayList /* <IUsageEventListener> */ m_listeners;
    
    private static CPUUsageThread s_singleton;

} // end of class
// ----------------------------------------------------------------------------

