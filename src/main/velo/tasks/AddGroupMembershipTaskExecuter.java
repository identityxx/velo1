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
package velo.tasks;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.NonUniqueResultException;
import velo.actions.tools.AddGroupMembershipTools;
import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.ResourceGroupManagerRemote;
import velo.entity.Account;
import velo.entity.Task;

public class AddGroupMembershipTaskExecuter extends DefaultTaskExecuter {
	AccountManagerRemote am;
	
	ResourceGroupManagerRemote tsgm;
	
	public AddGroupMembershipTaskExecuter() {
		try {
			Context ic = new InitialContext();
			AccountManagerRemote amRemote = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
			am = amRemote;
			
			
			ResourceGroupManagerRemote tsgmRemote = (ResourceGroupManagerRemote) ic.lookup("velo/ResourceGroupBean/remote");
			tsgm = tsgmRemote;
			
		} catch (NamingException ne) {
			ne.printStackTrace();
		}
	}
	
	/*
	public boolean execute(Task task) {
		try {
			System.out.println("Executing Add Group Membership Task Started...");
			ActionInterface action = getActionFromTask(task);
			if (action != null) {
				ResourceAccountActionInterface accountAction = (ResourceAccountActionInterface)action;
				AddGroupMembershipTools agmTools = (AddGroupMembershipTools)accountAction.getTools();
			
				//Get the account name from the tools and load the entity and set it into the tools before executing the script
				//(This is not done in prior stage since the account might be created by another task in the same bulk task (such as in role insertion bulk task)
				Account accountEntity = am.findAccount(agmTools.getAccountName(),agmTools.getTsg().getResource().getUniqueName());
				accountAction.setAccount(accountEntity);
				if (super.execute(task)) {
					//If action was successfully finished, then add the account entity as a member of the target system group entity
					System.out.println("Suceessfully added group membership, associating account as a member of the group!");
					tsgm.addMemberToGroup(agmTools.getTsg(), accountEntity);
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		catch (java.lang.NullPointerException npe) {
			String errMsg = "Null pointer exception has occured, canceling task execution, message: " + npe.getMessage();
			System.out.println(errMsg);
			task.addLog("ERROR", "NullPointerException", errMsg);
			tm.indicateTaskExecutionFailure(task,null);
			return false;
		}
		/*JB
		catch (NoResultFoundException nrfe) {
			String errMsg = "No account found in DB, detailed failure message is: " + nrfe.getMessage();
			System.out.println(errMsg);
			task.addLog("ERROR", "NoResultFoundException", errMsg);
			tm.indicateTaskExecutionFailure(task,null);
			return false;
		}
		*/
		//TODO: Improve error handling! these errors goes to the logs and thats it!
	/*
		catch (NonUniqueResultException nure) {
			String errMsg ="Multiple accounts were returned for the same account name on the same target!!!, detailed failure message is: " + nure.getMessage();
			System.out.println(errMsg);
			task.addLog("ERROR", "NoResultFoundException", errMsg);
			tm.indicateTaskExecutionFailure(task,null);
			return false;
		}
		catch (Exception e) {
			System.out.println("*****************************: " + e.getClass().getName());
			e.printStackTrace(System.out);
			tm.indicateTaskExecutionFailure(task,null);
			return false;
		}
		
	}
*/
}
