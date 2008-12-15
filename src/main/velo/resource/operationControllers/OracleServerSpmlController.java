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
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.entity.ResourceTask;
import velo.entity.SpmlTask;
import velo.exceptions.AdapterException;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.OperationException;
import velo.query.Query;
import velo.query.Query.QueryType;
import velo.resource.SyncDataXmlGenerator;

public class OracleServerSpmlController extends GroupMembershipSpmlResourceOpreationController {
	private static Logger log = Logger.getLogger(OracleServerSpmlController.class.getName());

	private JdbcAdapter adapter;
	
	private String createUserQueryTemplate = "CREATE USER %USER% IDENTIFIED BY %PASSWORD%";
	private String deleteUserQueryTemplate = "DROP USER %USER% CASCADE";
	private String lockUserQueryTemplate = "ALTER USER %USER% ACCOUNT LOCK";
	private String unlockUserQueryTemplate = "ALTER USER %USER% ACCOUNT UNLOCK";
	private String fetchActiveUsersQuery = "SELECT USERNAME FROM ALL_USERS";
	private String grantRoleToUserQuery = "GRANT %ROLE_NAME% TO %USER%";
	private String revokeRoleToUserQuery = "REVOKE %ROLE_NAME% FROM %USER%";
	
	public OracleServerSpmlController() {
		
	}
	
	public void init(OperationContext context) {
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, SuspendRequest request) throws OperationException {
		log.debug("Performing Suspend Request operation has started");

		String accountName = request.getPsoID().getID();
		
		Query q = new Query(QueryType.UPDATE);
		String query = lockUserQueryTemplate.replace("%USER%", request.getPsoID().getID());
		log.debug("Query to execute: " + query);
		q.add(query);
		
		try {
			getAdapter().runQuery(q);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}

		log.debug("Finished suspend request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ResumeRequest request) throws OperationException {
		log.debug("Performing Resume Request operation has started");


		String accountName = request.getPsoID().getID();
		
		Query q = new Query(QueryType.UPDATE);
		String query = unlockUserQueryTemplate.replace("%USER%", request.getPsoID().getID());
		log.debug("Query to execute: " + query);
		q.add(query);
		
		try {
			getAdapter().runQuery(q);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}


		log.debug("Finished Resume request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, DeleteRequest request) throws OperationException {
		log.debug("Performing Delete Request operation has started");


		String accountName = request.getPsoID().getID();
		
		Query q = new Query(QueryType.UPDATE);
		String query = deleteUserQueryTemplate.replace("%USER%", request.getPsoID().getID());
		log.debug("Query to execute: " + query);
		q.add(query);
		
		try {
			getAdapter().runQuery(q);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}


		log.debug("Finished Delete request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest request) throws OperationException {
		log.debug("Performing Add Request operation has started");

		
		String accountName = request.getPsoID().getID();
		Map<String,ResourceAttribute> attrs = (Map<String,ResourceAttribute>)ro.getContext().get("attrs");
		
		String password = attrs.get("PASSWORD").getFirstValue().getAsString();
		
		
		Query q = new Query(QueryType.UPDATE);
		String query = createUserQueryTemplate.replace("%USER%", request.getPsoID().getID()).replace("%PASSWORD%", password);
		log.debug("Query to execute: " + query);
		q.add(query);
		
		try {
			getAdapter().runQuery(q);
		} catch (AdapterException e) {
			//indicate failure
			throw new OperationException(e.toString());
		}
		

		log.debug("Finished Add request operation invocation");

	}
	
	public void performOperationAddGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToAdd) throws OperationException {
		log.debug("Granting roles (velo access groups) with amount: '" + groupsToAdd.size() + "'");
		
		String accountName = (String)ro.getContext().get("accountName");
		Query queryManager = (Query)ro.getContext().get("queryManager");
		queryManager.setQueryType(QueryType.UPDATE);
		//Iterate over all groups, per group, perform the whole life cycle of the group membership operation
		for (ResourceGroup currGroupGroup : groupsToAdd) {
			String currQuery = grantRoleToUserQuery.replace("%USER%", accountName).replace("%ROLE_NAME%", currGroupGroup.getUniqueId());
			queryManager.add(currQuery);
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
		
		log.debug("Sucessfully ended invocation of AddGroupMembership Operation...");
	}

	public void performOperationRemoveGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToRemove) throws OperationException {
		//TODO: Implement!
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException {
		log.debug("Performing Modify Request operation has started");


		//TODO: Support grants
		throw new OperationException("This operation is currently not supported!");


		//log.debug("Finished Modify request operation invocation");
	}
	
	
	
	
	public void resourceFetchActiveDataOffline(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		Query accountsQueryManager = new Query(QueryType.SELECT);
		accountsQueryManager.add(fetchActiveUsersQuery);
		
		
		if (log.isTraceEnabled()) {
			log.trace("Dumping fetch active data query: " + fetchActiveUsersQuery);
		}

		
		//quering...
		try {
			log.debug("quering accounts...");
			getAdapter().runQuery(accountsQueryManager);
			List accountsQueryResult = (List)getAdapter().getResult();
			List groupsQueryResult = null;
			
			/*
			if (groupsQueryManager != null) {
				//LIST GROUPS
				log.debug("quering access groups...");
				getAdapter().runQuery(groupsQueryManager);
				groupsQueryResult = (List)getAdapter().getResult();
			}
			else {
				groupsQueryResult = new ArrayList();
			}
			*/
				
				
			//create sync file
			createFetchActiveDataOfflineFile(accountsQueryResult, null, resource);
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