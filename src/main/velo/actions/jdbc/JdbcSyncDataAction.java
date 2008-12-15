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
package velo.actions.jdbc;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import velo.actions.ResourceActionInterface;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.LoadGroupByMapException;
import velo.exceptions.ObjectsConstructionException;
import velo.query.Query.QueryType;
import velo.resource.SyncDataXmlGenerator;

/**
 * An abstract class of all JDBC 'Sync Data' actions
 * Aims to list accounts of a certain Resource
 *
 * TODO: Currently empty, in the future might create the list query automatically Currently must get implemented by child
 *
 * @author Asaf Shakarchi
 */
//abstract public class JdbcSyncDataAction extends JdbcResourceAction implements JdbcResourceListAction {
public abstract class JdbcSyncDataAction extends JdbcResourceAction implements ResourceActionInterface {
    
    private transient static Logger logger = Logger.getLogger(JdbcSyncDataAction.class.getName());
    
    private List accountListFromQuery;
    private List groupListFromQuery;
    List<Account> activeAccounts = new ArrayList<Account>();
    List<ResourceGroup> activeGroups = new ArrayList<ResourceGroup>();
    
    /**
     * Constructor
     * @param resource The Resource entity retrieve the account list for
     */
    public JdbcSyncDataAction(Resource resource) {
        super(resource);
    }
    
    /**
     * Constructor
     * Empty constructor
     */
    public JdbcSyncDataAction() {
    }
    
    protected boolean buildQuery() {
        return true;
    }
    
    public boolean execute() throws ActionFailureException {
        try {
            getAdapter().connect();
            
        } catch (AdapterException ae) {
            throw new ActionFailureException(ae.getMessage());
        }
        
        try {
            //LIST ACCOUNTS
            logger.info("Build Sync Data has started for Resource: " + getResource().getDisplayName());
            if (!buildListAccountsQuery()) {
                StringBuilder msg = new StringBuilder();
                msg.append("FAILED to build query for list accounts action, failure message: ");
                msg.append(getMsgs().toString());
                throw new ActionFailureException(msg.toString());
            }
            
            getQuery().setQueryType(QueryType.SELECT);
            //boolean bResult = getSpecificAdapter().runQuery(getQuery());
            getAdapter().runQuery(getQuery());
            accountListFromQuery = (List)getActionResult();
            
            
                        /*
                        if (bResult) {
                                accountListFromQuery = (List)getActionResult();
                        }
                        else {
                                throw new ActionFailureException("Couldnt execute 'Account List' query...");
                        }
                         */
            
            //LIST GROUPS
            System.out.println("buildListGroupsQuery started for Resource: " + getResource().getDisplayName());
            
            //Clean the query before getting the query from the next group list method
            getQuery().clear();
            
            if (!buildListGroupsQuery()) {
                StringBuilder msg = new StringBuilder();
                msg.append("FAILED to build query for list groups action, failure message: ");
                msg.append(getMsgs().toString());
                throw new ActionFailureException(msg.toString());
            } else {
                //If system does not support groups
/*JB                if (!getQuery().isQueryEmpty()) {
                    getQuery().setActionType(ActionOptions.SELECT);
                    //boolean bListGroupsResult = getSpecificAdapter().runQuery(getQuery());
                    getAdapter().runQuery(getQuery());
                    groupListFromQuery = (List)getActionResult();
                } else {
                    groupListFromQuery = new ArrayList<String>();
                }
*/
            }
            
            
            
                        /*
                        if (bResult) {
                                groupListFromQuery = (List)getActionResult();
                        }
                        else {
                                throw new ActionFailureException("Couldnt execute 'Group List' query...");
                        }
                         */
            
            try {
                createListFile();
                return true;
            } catch (IOException ioe) {
                return false;
            }
        } catch (AdapterException ae) {
            throw new ActionFailureException(ae);
        }
        
        //If a method was not found
        catch (MissingMethodException mme) {
            throw new ActionFailureException(mme);
        }
        //If a method is called over a null object
        catch (NullPointerException npe) {
            throw new ActionFailureException(npe);
        }
        //When a method is called over a non existend 'object'
        catch (MissingPropertyException mpe) {
            throw new ActionFailureException(mpe);
        } catch (AbstractMethodError ame) {
            throw new ActionFailureException(ame);
        }
    }
    
    public boolean createListFile() throws IOException {
        logger.info("Preparing a MAP(from accounts retrieved by query) with ACCOUNTS amount: '" + accountListFromQuery.size() + "'");
        
        for (int i = 0; i < accountListFromQuery.size(); i++) {
            // Convert a map per object
            Map currUser = (Map) accountListFromQuery.get(i);
            
            logger.finest("Current user map: " + currUser);
            //logger.info("Dumping ActiveAccount that was just read from TS: " + currUser);
            Account account = new Account();
            //(Resource is required by 'loadAccountByMap')
            
            account.setResource(getResource());
            try {
                account.loadAccountByMap(currUser);
                activeAccounts.add(account);
            } catch (ObjectsConstructionException e) {
                //logger.error("Couldnt load account by map, with message: " + e.getMessage());
                logger.warning("Skipping current iterated active account due to exception: " + e.getMessage());
            }
        }
        
        logger.info("Preparing a MAP(from groups retrieved by query) with GROUPS amount: '" + groupListFromQuery.size() + "'");
        for (int i=0;i<groupListFromQuery.size(); i++) {
            logger.finest("Current groups from query iteration is: '" + i + "'");
            //Convert a map per object
            Map currGroup = (Map) groupListFromQuery.get(i);
            
            logger.finest("Current group map: " + currGroup);
            //logger.info("Dumping ActiveAccount that was just read from TS: " + currUser);
            ResourceGroup group = new ResourceGroup();
            //(Resource is required by 'loadAccountByMap')
            
            try {
                //logger.info("*** BEFORE LOAD...");
                group.load(currGroup,getResource());
                //logger.info("*** AFTER LOAD...");
            } catch (LoadGroupByMapException e) {
                logger.warning("skipping group, failed with message: " + e.getMessage());
                continue;
            }
            //logger.info("*** ADDING GROUP!...");
            activeGroups.add(group);
            //logger.info("*** FINISHED ADDING GROUP!...");
        }
        
        logger.info("Created Accounts-Map with amount '" + activeAccounts.size() + "' and Groups-Map containing '" + activeGroups.size() + "' groups");
        
        logger.info("Starting to generate XML file with loaded content...");
        //SyncTargetGenerator stg = new SyncTargetGenerator(getResource());
        SyncDataXmlGenerator sdxg = new SyncDataXmlGenerator(getResource());
        //stg.setAccounts(activeAccounts);
        //stg.setGroups(activeGroups);
        sdxg.setAccounts(activeAccounts);
        sdxg.setGroups(activeGroups);
        
        
        /*STG's lines
        //Get the file name to create the list
        String fileName = getResource().factorySyncFileName();
        //BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        bw.write(stg.dumpAsString());
        bw.close();
         */
        
        
        //Get the file name to create the list
        String fileName = getResource().factorySyncFileName();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        bw.write(sdxg.dumpXmlAsString());
        bw.close();
        
        
        logger.info("Finished to generate XML file with loaded content...");
        return true;
    }
    
    public abstract boolean buildListAccountsQuery();
    
    public boolean buildListGroupsQuery() {
        return true;
    }
    
    public boolean buildListGroupsMembershipQuery() {
        return true;
    }
}
