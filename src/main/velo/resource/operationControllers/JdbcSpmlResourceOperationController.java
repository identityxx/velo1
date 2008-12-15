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
package velo.resource.operationControllers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;

import velo.action.ResourceOperation;
import velo.adapters.JdbcAdapter;
import velo.contexts.OperationContext;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceTask;
import velo.entity.SpmlTask;
import velo.exceptions.AdapterException;
import velo.exceptions.LoadGroupByMapException;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.OperationException;
import velo.query.Query;
import velo.query.Query.QueryType;
import velo.resource.SyncDataXmlGenerator;

//All methods in this class are invoked synchrony against the resources 
public class JdbcSpmlResourceOperationController extends GroupMembershipSpmlResourceOpreationController {
	private static Logger log = Logger.getLogger(JdbcSpmlResourceOperationController.class.getName());
	//TODO: Move to somewhere better, in one place with all others located at 'ResourceOperationsBean'.
	
	JdbcAdapter adapter;
	
	public JdbcSpmlResourceOperationController() {
		
	}
	
	public JdbcSpmlResourceOperationController(Resource resource) {
		super(resource);
	}
	
	public void init(OperationContext context) {
		context.addVar("queryManager", new Query());
	}
	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, SuspendRequest request) throws OperationException {
		//Invoke the action itself...
		//TODO: Perform!
		//expecting a query object to be in context
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		
		log.trace("Recieved a queryManager from context with queries amount '" + queryManager.queryAmount() + "'");
		if (log.isTraceEnabled()) {
			log.trace("Dumping all queries in the right order...");
			log.trace(queryManager);
		}
		
		try {
			getAdapter().runQuery(queryManager);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}
	
		
		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}
	
	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ResumeRequest request) throws OperationException {
		//Invoke the action itself...
		//TODO: Perform!
		//expecting a queryManager to be in context
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		
		log.trace("Recieved a queryManager from context with queries amount '" + queryManager.queryAmount() + "'");
		if (log.isTraceEnabled()) {
			log.trace("Dumping all queries in the right order...");
			log.trace(queryManager);
		}

		try {
			getAdapter().runQuery(queryManager);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}
		
		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}
	
	
	
	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, DeleteRequest request) throws OperationException {
		//Invoke the action itself...
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		
		log.trace("Recieved a queryManager from context with queries amount '" + queryManager.queryAmount() + "'");
		if (log.isTraceEnabled()) {
			log.trace("Dumping all queries in the right order...");
			log.trace(queryManager);
		}
		
		try {
			getAdapter().runQuery(queryManager);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}
		
		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}

	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest request) throws OperationException {
		//Invoke the action itself...
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		
		log.trace("Recieved a queryManager from context with queries amount '" + queryManager.queryAmount() + "'");
		if (log.isTraceEnabled()) {
			log.trace("Dumping all queries in the right order...");
			log.trace(queryManager);
		}
		
		try {
			getAdapter().runQuery(queryManager);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}
		
		//very important to clean the querymanager for the group membership opreations
		queryManager.clear();
		
		log.debug("Sucessfully created account in resource.");
	}
	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException {
		log.info("JDBC Modify request has started...");
		
		//nothing to be performed directly as ResourceOperationBean->ModifyAccount will take care of invoking add/remove groups directly.
	}
	
	
	
	
	
	
	public void performOperationAddGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToAdd) throws OperationException {
		log.debug("Getting a query per group membership to assign...");
		
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		//Iterate over all groups, per group, perform the whole life cycle of the group membership operation
		for (ResourceGroup currGroup : groupsToAdd) {
			//override the groupID within the context
			ro.getContext().set("groupUniqueId", currGroup.getUniqueId());
			ro.getContext().set("groupType", currGroup.getType());
			
			//PRE
			boolean invocationStatus = ro.preActionOperation();
			if (!invocationStatus) {
				log.error("PreExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
			}
		
			//invocation will add a new query if set within the PRE action, pass by reference, no need to do anything
			
			log.trace("Recieved a queryManager from context for 'Add Group Membership!' of group named '" + currGroup.getUniqueId() + "', adding query.");
			log.trace("Current amount of group membership queries: '" +  queryManager.queryAmount() + "'");
			
			//Perform POST operation actions
			invocationStatus = ro.postActionOperation();
			if (!invocationStatus) {
				log.error("PostExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
			}
		}
		
		
		//Invoking queries
		if (log.isTraceEnabled()) {
			log.trace("Dumping all queries in the right order...");
			log.trace(queryManager);
		}
		log.debug("Invoking the 'Add Group Membership' queries within QueryManager, which contains '" + queryManager.queryAmount() + "' queries!");
		try {
			getAdapter().runQuery(queryManager);
			//clean the query for the next iteration
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
		
		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}
	
	
	public void performOperationRemoveGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToRemove) throws OperationException {
		log.debug("Getting a query per group membership to remove...");
		
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		//Iterate over all groups, per group, perform the whole life cycle of the group membership operation
		for (ResourceGroup currGroup : groupsToRemove) {
			//override the groupID within the context
			ro.getContext().set("groupUniqueId", currGroup.getUniqueId());
			ro.getContext().set("groupType", currGroup.getType());
			
			//PRE
			boolean invocationStatus = ro.preActionOperation();
			if (!invocationStatus) {
				log.error("PreExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
			}
		
			//invocation will add a new query if set within the PRE action, pass by reference, no need to do anything
			
			log.trace("Recieved a queryManager from context for 'Remove Group Membership!' of group named '" + currGroup.getUniqueId() + "', adding query.");
			log.trace("Current amount of group membership queries: '" +  queryManager.queryAmount() + "'");
			
			//Perform POST operation actions
			invocationStatus = ro.postActionOperation();
			if (!invocationStatus) {
				log.error("PostExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
			}
		}
		
		
		//Invoking queries
		if (log.isTraceEnabled()) {
			log.trace("Dumping all queries in the right order...");
			log.trace(queryManager);
		}
		log.debug("Invoking the 'Remove Group Membership' queries within QueryManager, which contains '" + queryManager.queryAmount() + "' queries!");
		try {
			getAdapter().runQuery(queryManager);
			//clean the query for the next iteration
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
		
		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}
	
	
	

	
	
	
	
	
	public void resourceFetchActiveDataOffline(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		//expecting a query to be in context
		//String accountsQuery = (String)ro.getContext().get("accountsQuery");
		//String groupsQuery = (String)ro.getContext().get("groupsQuery");
		
		Query accountsQueryManager = (Query)ro.getContext().get("accountsQueryManager");
		Query groupsQueryManager = (Query)ro.getContext().get("groupsQueryManager");
		
		if (log.isTraceEnabled()) {
			log.trace("Recieved a 'accounts query' from, dump of query: " + accountsQueryManager);
			log.trace("Recieved a 'groups query' from, dump of query: " + groupsQueryManager);
		}

		if (accountsQueryManager == null) {
			throw new OperationException("Could not find 'accountQuery' object in context, please set this variable with the relevant query!");
		}
		
		//quering...
		try {
			log.debug("quering accounts...");
			getAdapter().runQuery(accountsQueryManager);
			List accountsQueryResult = (List)getAdapter().getResult();
			List groupsQueryResult = null;
			if (groupsQueryManager != null) {
				//LIST GROUPS
				log.debug("quering access groups...");
				getAdapter().runQuery(groupsQueryManager);
				groupsQueryResult = (List)getAdapter().getResult();
			}
			else {
				groupsQueryResult = new ArrayList();
			}
				
				
			//create sync file
			createFetchActiveDataOfflineFile(accountsQueryResult, groupsQueryResult, resource);
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
		catch (IOException e) {
			throw new OperationException(e.toString());
		}
			
		log.trace("Sucessfully generated XML with all active data");
	}
	
	
	
	
	
	
	
	
	
	
	private boolean createFetchActiveDataOfflineFile(List accountsQueryResult, List groupsQueryResult, Resource resource) throws IOException {
		List<Account> activeAccounts = new ArrayList<Account>();
	    List<ResourceGroup> activeGroups = new ArrayList<ResourceGroup>();
	    
	    if (accountsQueryResult != null) {
        log.debug("Preparing a MAP(from accounts retrieved by query) with ACCOUNTS amount: '" + accountsQueryResult.size() + "'");
        for (int i = 0; i < accountsQueryResult.size(); i++) {
            // Convert a map per object
            Map currUser = (Map) accountsQueryResult.get(i);
            
            log.trace("Current user map: " + currUser);
            //logger.info("Dumping ActiveAccount that was just read from TS: " + currUser);
            Account account = new Account();
            //(Resource is required by 'loadAccountByMap')
            
            account.setResource(resource);
            try {
                account.loadAccountByMap(currUser);
                activeAccounts.add(account);
            } catch (ObjectsConstructionException e) {
                //logger.error("Couldnt load account by map, with message: " + e.getMessage());
                log.warn("Skipping current iterated active account due to exception: " + e.getMessage());
            }
        }
	    }
        
        log.info("Preparing a MAP(from groups retrieved by query) with GROUPS amount: '" + groupsQueryResult.size() + "'");
        for (int i=0;i<groupsQueryResult.size(); i++) {
        	log.trace("Current groups from query iteration is: '" + i + "'");
            //Convert a map per object
            Map currGroup = (Map) groupsQueryResult.get(i);
            
            log.trace("Current group map: " + currGroup);
            //logger.info("Dumping ActiveAccount that was just read from TS: " + currUser);
            ResourceGroup group = new ResourceGroup();
            //(Resource is required by 'loadAccountByMap')
            
            try {
                //logger.info("*** BEFORE LOAD...");
                group.load(currGroup,resource);
                //logger.info("*** AFTER LOAD...");
            } catch (LoadGroupByMapException e) {
                log.warn("skipping group, failed with message: " + e.getMessage());
                continue;
            }
            //logger.info("*** ADDING GROUP!...");
            activeGroups.add(group);
            //logger.info("*** FINISHED ADDING GROUP!...");
        }
        
        log.debug("Created Accounts-Map with amount '" + activeAccounts.size() + "' and Groups-Map containing '" + activeGroups.size() + "' groups");
        
        
        
        
        log.debug("Starting to generate XML file with loaded content...");
        //SyncTargetGenerator stg = new SyncTargetGenerator(getResource());
        SyncDataXmlGenerator sdxg = new SyncDataXmlGenerator(resource);
        //stg.setAccounts(activeAccounts);
        //stg.setGroups(activeGroups);
        sdxg.setAccounts(activeAccounts);
        sdxg.setGroups(activeGroups);
        
        
        //Get the file name to create the list
        String fileName = getResource().factorySyncFileName();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        bw.write(sdxg.dumpXmlAsString());
        bw.close();
        
        
        log.info("Finished to generate XML file with loaded content...");
        return true;
    }
	
	

	
	//TODO: Support adapters via pools	
	private JdbcAdapter getAdapter() throws AdapterException {
		if (this.adapter == null) {
			adapter = new JdbcAdapter(getResource());
		}
		
		if (!adapter.isConnected()) {
			adapter.connect();
		}
		return this.adapter;
	}
}
