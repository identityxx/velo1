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
package velo.adapters;

import java.io.Serializable;
import java.util.Collection;

import velo.common.EdmMessages;
import velo.common.UniqueIdGenerator;
import velo.exceptions.AdapterException;

/**
 * An abstract class for all Adapters.
 *
 * Adapters are the communication objects that most of the actions communicating with external resources works with
 *
 * @author Asaf Shakarchi
 */
public abstract class Adapter implements AdapterInterface, Serializable {
    
    /**
     * A unique ID of the adapter
     */
    private String uniqueId;
    
    private boolean passivated;
    
    //Whether or not the adapter was initiated as stand alone (used mostly for testing!)
    private boolean initiatedStandalone = false;
    
    
    /**
     * A logger object to log messages
     */
    //private static Logger logger = Logger.getLogger(Adapter.class.getName());
    
    /**
     * Constructor
     */
    public Adapter() {
        initUniqueId();
    }
    
    /**
     * @param uniqueId the uniqueId to set
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    /**
     * @return the uniqueId
     */
    public String getUniqueId() {
        return uniqueId;
    }
    
    /**
     * @param passivated the passivated to set
     */
    public void setPassivated(boolean passivated) {
        this.passivated = passivated;
    }
    
    /**
     * @return the passivated
     */
    public boolean isPassivated() {
        return passivated;
    }
    
    public void initUniqueId() {
        setUniqueId("ADAPTER:"+UniqueIdGenerator.generateUniqueIdByUID());
    }
    
    /**
     * Whether the connection was established to the resource or not
     */
    private boolean connected;
    
    /**
     * Logs messages of the adapter (if any)
     * Ususally these messages are make a good use for tracing by the adapter executer [probably actions]
     */
    private EdmMessages msgs;
    
    /**
     * Attempts to connect to the target system.
     * @return true if connection attempt was successfull false if it failed.
     * @throws AdapterException
     */
    public abstract boolean connect() throws AdapterException;
    
    /**
     * Attempts to disconnect from the target system.
     *
     * @return true if successfully disconnected,
     *         false if failed.
     */
    public abstract boolean disconnect();
    
    
    
    /**
     * @param connected The connected to set.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    /**
     * @return Returns the connected.
     */
    public boolean isConnected() {
        return connected;
    }
    
    public EdmMessages getMsgs() {
        if (msgs == null) {
            msgs = new EdmMessages();
        }
        
        return msgs;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //Wished to make a GENERIC adapter methods but this is too tough since targets are very different from each other :/
    /**
     * Holds the results of the last commands.
     * If command failed or did not retun
     * results, will contain an empty list
     *
     */
    private Collection adapterResult;
    
    
    /**
     *  Holds a reference to the object that holds
     *  the data required to run the action.
     *
     *  (for example , the data of the new user to create)
     */
    //TODO: NEEDED? CollectionDataObject activeObject;
    
    /**
     * If whether or not the last command run
     * on this conenction was succesfull
     * @deprecated
     */
    boolean bLastCommandSuccessful;
    
        /*
         * Constructor
         * @param targetSystem The Target-System entity the action is executed on
         * @throws TargetSystemDescriptorException
         */
        /*
        public Adapter(TargetSystem targetSystem) throws TargetSystemDescriptorException {
                try {
                        //TODO: Replace to local interface, no reason to use remote
                        Context ic = new InitialContext();
                        tsm = (TargetSystemManagerRemote) ic
                                        .lookup(TargetSystemManagerRemote.class.getName());
                } catch (NamingException ne) {
                        ne.printStackTrace();
                }
         
                setTargetSystem(targetSystem);
                try {
                        setTargetSystemDescriptor(tsm
                                .factoryTargetSystemDescriptor(getTargetSystem()));
                }
                catch (TargetSystemDescriptorException tsde) {
                        throw tsde;
                }
        }
         */
    
    
    
        /*
         * Attempts to replace the active object of
         * the connector with a new active object
         * reference.
         *
         * @param newActiveObject the active object we want to set
         *                        the referance to
         public void setActiveObject(CollectionDataObject newActiveObject) {
         this.activeObject = newActiveObject;
         }
         */
    
        /*
         * Returns the active object set in
         * this connectorwrapper.
         *
         * @return A CollectioDataObject referance to
         *         the colelctino data object thats the
         *         active object in this connector wrapper.
         public CollectionDataObject getActiveObject()
         {
         return this.activeObject;
         }
         */
    
    /**
     * Attempts to create a new adapter instance
     * of this adaper class, and store in the connector field.
     * (defiend in appropriate subclasses).
     *
     */
    //public abstract void createAdapter();
    
    /**
     *  Attempts to run a command given by
     *  a form of a string on the target system it
     *  is connected to.
     *
     *  Sets bWasSuccessfull , sLastErrorMassage,
     *  and DataObjectList with the proper
     *  values after the command was run.
     *
     *  @param sCmdString The command we want to run in the
     *                    form of a string. A query based
     *                    connector will get a query, other
     *                    connectors will get something else
     *                    (undefined yet).
     *
     *  @return true if the run attempt was successfull,
     *          false if not.
     *          If the string is empty or invalid,
     *          will return false, the last action run will
     *          be considered a failiur, and last error
     *          string will be "invalid or empty string".
     *
     */
    @Deprecated
    public boolean runString(String sCmdString) {
        System.out.println("RUN STRING IS NOT SUPPORTED!");
        //logger.info("Executing command: " + sCmdString);
        /*TODO: check if string is valid*/
        
        /*TODO :clear result list*/
        
        /*TODO :clear last error string*/
        
        /* TODO :log log log*/
        
        //runCommandString(sCmdString);
        
        if (getLastCommandWasSuccessfull()) {
            /*  TODO: we might want to set a string result anyway.*/
            /* setSLastErrorMassageFromConnector(); */
        }
        
        return getLastCommandWasSuccessfull();
    }
    
        /*
         * DOES NOT WORK SO GOOD, EACH ADAPTER HAS ITS OWN COMMAND EXECUTION MECHANISMS. CANNOT MAKE IT SO GENERIC :-/
         * Runs a command on the connector.
         * The command must me given in a text format
         * the connector can understand, or the
         * connectorwrapper can turn it into a format
         * the connector can understand.
         *
         * @param sCmdString The strign containing the command
         *                   that should run on the connector
         * @return True if we were able to run the command
         *         on the connector, false if it failed.
         */
    //public abstract boolean runCommandString(String sCmdString);
    
        /*
         * Returns the last error massage of
         * the last command run on this connector
         *
         * @return The last error massage, a empty string
         *         if the connector did not return an error
         *         when running the command.
         *
         * @deprecated
         *
         public String getLastErrorMassge()
         {
         return sLastErrorMassage;
         }
         */
    
    /**
     * Returns a boolean variable indicating if the
     * last action run attempt of this connector
     * was successfull or not.
     *
     * @return true if the last action run attempt
     *         passed ok , false if it failed
     */
    public boolean getLastCommandWasSuccessfull() {
        return bLastCommandSuccessful;
    }
    
        /*
         public CollectionDataObjectList getLastCommandResultSet()
         {
         
         }
         */
    
        /*
         * Sets the last error massage on the conenctor.
         *
         * @param sErrorMassage The new error massage we
         *                      want to set
         *
         * @retrun True if the set was successfull
         *         false if failed
         *
         public boolean setSLastErrorMassge(String sErrorMassage) {
         sLastErrorMassage=sErrorMassage;
         return true;
         }
         */
    
    /**
     * Sets the flag that indicates if the last
     * action as successfull or not.
     * @param bCommandResult The result we was to set, true/false
     * @return true if the set was successful / false if failed
     */
    public boolean setBLastCommandSuccessful(boolean bCommandResult) {
        bLastCommandSuccessful = bCommandResult;
        return true;
    }
    
    /**
     * Sets the result from the last executed command as a List
     *
     * @param resultList The result from the last command execution as a LIST
     */
    public void setResult(Collection resultList) {
        this.adapterResult = resultList;
    }
    
    
    /**
     * Get the adapter Result(from the last command execution) as a LIST
     *
     * @return The result from the last command exectuion as a LIST
     */
    public Collection getResult() {
        return adapterResult;
    }
    
    
    
    public boolean isInitiatedStandalone() {
        return initiatedStandalone;
    }
    
    public void setInitiatedStandalone(boolean initiatedStandalone) {
        this.initiatedStandalone = initiatedStandalone;
    }
}
