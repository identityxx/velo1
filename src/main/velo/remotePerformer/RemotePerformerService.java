/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.remotePerformer;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 *
 * @author Shakarchi Asaf
 */
public class RemotePerformerService implements WrapperListener,Runnable {
    
    private final boolean debug = true;
    
    private boolean isMainThreadStarted;
    private boolean isMainThreadComplete;
    
    private Integer mainExitCode;
    
    //Must have an empty constructor
    public RemotePerformerService() {
        System.out.println("INITIALIZING CONSTRUCTOR...");
        // Initialize the WrapperManager class on startup by referencing it.
        Class wmClass = WrapperManager.class;

	Class mainClass;
	try
        {
            mainClass = Class.forName(RemotePerformer.class.getName());
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
            WrapperManager.stop( 1 );
            return;  // Will not get here
        }



        //WrapperManager.start(this, new String[0]);
        
        // This thread ends, the WrapperManager will start the application after the Wrapper has
        //  been properly initialized by calling the start method above.
    }
    
    
    
    
    
     /*---------------------------------------------------------------
      * Runnable Methods
      *-------------------------------------------------------------*/
    /**
     * Used to launch the application in a separate thread.
     */
    public void run() {
        // Notify the start method that the thread has been started by the JVM.
        synchronized( this ) {
            isMainThreadStarted = true;
            notifyAll();
        }
        
        Throwable t = null;
        try {
            if ( WrapperManager.isDebugEnabled() ) {
                System.out.println( "WrapperSimpleApp: invoking main method" );
            }
            
            try {
                //Invoke the app!
                RemotePerformer.start();
            }
            catch (Exception e) {
                t = e;
            }
            
            if ( debug ) {
                System.out.println( "RemotePerformerService: main method completed its execution!" );
            }
            
            synchronized(this) {
                // Let the start() method know that the main method returned, in case it is
                //  still waiting.
                isMainThreadComplete = true;
                this.notifyAll();
            }
            
            return;
        } catch ( IllegalArgumentException e ) {
            t = e;
        }
        
        // If we get here, then an error was thrown.  If this happened quickly
        // enough, the start method should be allowed to shut things down.
        System.out.println();
        System.out.println( "RemotePerformerService: Encountered an error running main: " + t );
        
        // We should print a stack trace here, because in the case of an
        // InvocationTargetException, the user needs to know what exception
        // their app threw.
        t.printStackTrace();
        
        synchronized(this) {
            if ( isMainThreadComplete ) {
                // Shut down here.
                WrapperManager.stop( 1 );
                return; // Will not get here.
            } else {
                // Let start method handle shutdown.
                isMainThreadComplete = true;
                mainExitCode = new Integer( 1 );
                this.notifyAll();
                return;
            }
        }
    }
    
    
    
    
    
    
    
    
    
    /*---------------------------------------------------------------
     * WrapperListener Methods
     *-------------------------------------------------------------*/
    
    /**
     * The start method is called when the WrapperManager is signaled by the
     *	native wrapper code that it can start its application.  This
     *	method call is expected to return, so a new thread should be launched
     *	as necessary since the application starts an endless loop
     *
     * @param args List of arguments used to initialize the application.
     *
     * @return Any error code if the application should exit on completion
     *         of the start method.  If there were no problems then this
     *         method should return null.
     */
    public Integer start( String[] args ) {
        System.out.println("Starting application service...! (args: " + args + ")");

        // Decide whether or not to wait for the start main method to complete before returning.
        boolean waitForStartMain = false;
        //In seconds
        int maxStartMainWait = 7;
        
        
        
        // Decide the maximum number of times to loop waiting for the main start method.
        int maxLoops;
        if ( waitForStartMain ) {
            maxLoops = Integer.MAX_VALUE;
            if ( WrapperManager.isDebugEnabled() ) {
                System.out.println( "RemotePerformerService: start(args) Will wait indefinitely "
                        + "for the main method to complete." );
            }
        } else {
            maxLoops = maxStartMainWait; // 1s loops.
            if ( debug ) {
                System.out.println( "RemotePerformerService: start(args) Will wait up to " + maxLoops
                        + " seconds for the main method to complete." );
            }
        }
        
        Thread mainThread = new Thread( this, RemotePerformerService.class.getName() );
        
        synchronized(this) {
            
            //m_appArgs = args;
            mainThread.start();
            
            // To avoid problems with the main thread starting slowly on heavily loaded systems,
            //  do not continue until the thread has actually started.
            while ( !isMainThreadStarted ) {
                try {
                    this.wait( 1000 );
                } catch ( InterruptedException e ) {
                    // Continue.
                }
            }
            
            // Wait for startup main method to complete.
            int loops = 0;
            while ( ( loops < maxLoops ) && ( !isMainThreadComplete ) ) {
                try {
                    this.wait( 1000 );
                } catch ( InterruptedException e ) {
                    // Continue.
                }
                
                if ( !isMainThreadComplete ) {
                    // If maxLoops is large then this could take a while.  Notify the
                    //  WrapperManager that we are still starting so it doesn't give up.
                    WrapperManager.signalStarting( 5000 );
                }
                
                loops++;
            }
            
            // Always set the flag stating that the start method completed.  This is needed
            //  so the run method can decide whether or not it needs to be responsible for
            //  shutting down the JVM in the event of an exception thrown by the start main
            //  method.
            isMainThreadComplete = true;
            
            // The main exit code will be null unless an error was thrown by the start
            //  main method.
            if ( debug ) {
                System.out.println( "RemotePerformerSeervice: start(args) end.  Main Completed="
                        + isMainThreadComplete + ", exitCode=" + mainExitCode );
            }
            
            return mainExitCode;
        }
    }
    
    
    
    
    /**
     * Called when the application is shutting down.  The Wrapper assumes that
     *  this method will return fairly quickly.  If the shutdown code code
     *  could potentially take a long time, then WrapperManager.signalStopping()
     *  should be called to extend the timeout period.  If for some reason,
     *  the stop method can not return, then it must call
     *  WrapperManager.stopped() to avoid warning messages from the Wrapper.
     *
     * @param exitCode The suggested exit code that will be returned to the OS
     *                 when the JVM exits.
     *
     * @return The exit code to actually return to the OS.  In most cases, this
     *         should just be the value of exitCode, however the user code has
     *         the option of changing the exit code if there are any problems
     *         during shutdown.
     **/
    public int stop(int exitCode) {
        if ( debug ) {
            System.out.println( "RemotePerformerService: stop(" + exitCode + ")" );
        }
        
        RemotePerformer.stop();
        
        return exitCode;
    }
    
    
    
    /**
     * Called whenever the native wrapper code traps a system control signal
     *  against the Java process.  It is up to the callback to take any actions
     *  necessary.  Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT,
     *    WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or
     *    WRAPPER_CTRL_SHUTDOWN_EVENT
     *
     * @param event The system control signal.
     */
    public void controlEvent( int event ) {
        if (WrapperManager.isControlledByNativeWrapper()) {
            // The Wrapper will take care of this event
        } else {
            // We are not being controlled by the Wrapper, so
            //  handle the event ourselves.
            if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT) ||
                    (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT) ||
                    (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT)){
                WrapperManager.stop(0);
            }
        }
    }
    
    
    /*---------------------------------------------------------------
     * Main Method
     *-------------------------------------------------------------*/
    public static void main( String[] args ) {
        //Unneccessary as WrapperManager already started by the constructor!
        
        // Start the application.  If the JVM was launched from the native
        //  Wrapper then the application will wait for the native Wrapper to
        //  call the application's start method.  Otherwise the start method
        //  will be called immediately.
        WrapperManager.start( new RemotePerformerService(), args );
    }
}