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
package velo.reconcilidation.events.reconcileAccounts;

import java.util.logging.Logger;

import velo.entity.Account;
import velo.reconcilidation.events.AccountEvent;


/**
 * An event that occures when a Deleted account was found while Reconcile Account process executed
 *
 * (A 'Deleted' account event occures when the account exist in the IDM database but does not exist on the resource anymore...)
 * 
 * @author Asaf Shakarchi
 */
public class DeletedAccountEvent extends AccountEvent {
	private Account claimedAccount;
	private static Logger logger = Logger.getLogger(DeletedAccountEvent.class.getName());
	
	public boolean execute() {
		logger.finest("-DELETED- account event has occured for account name: '" + getClaimedAccount().getName() + "', in target system name: '" + getResource().getDisplayName() + "'" + ", policy action choosed is: " + getReconcilePolicy().getDeletedAccountEventAction().toUpperCase());
		
		/*
		switch (getReconcilePolicy().getDeletedAccountEventAction()) {
		case NOTHING:
			logger.info("Nothing was choosed to do...");
			break;
		case UNLINK_RESOURCE_ACCOUNT_FROM_USER:
			logger.fine("Unlinking the IDM account account name: " + getAccount().getName() + ", from  from user: " + getAccount().getUser().getName());
			//NOTE: it is a must to remove the last iterator's element (the current element that is going to get deleted), otherwise, cascade merge will re-merge the account when persist(account) would be called
			getResourceIterator().remove();
			unlinkAccount();
		}
		*/
		
		if (getReconcilePolicy().getDeletedAccountEventAction().equals("NOTHING")) {
			///logger.info("Nothing was choosed to do...");
		}
		else if (getReconcilePolicy().getDeletedAccountEventAction().equals("REMOVE_ACCOUNT_FROM_IDM_REPOSITORY")) {
			//logger.fine("Unlinking the IDM account account name: " + getAccount().getName() + ", from  from user: " + getAccount().getUser().getName());
			//getResourceIterator().remove();
			
			
			//07-02-07-> No more DB actions per event, moving to a bulk removal
			//removeAccount();
                    
                        String claimedAccountNameToRemove = getClaimedAccount().getName();
                        if (!getResource().isCaseSensitive()) {
                            claimedAccountNameToRemove.toUpperCase();
                        }
                        
			if (!getAccountsToRemove().containsKey(claimedAccountNameToRemove)) {
                                
                		getAccountsToRemove().put(claimedAccountNameToRemove,getClaimedAccount());
			}
			else {
				logger.warning("Skipping account removal of claimed account named: '" + getClaimedAccount().getName() + "',  since this account name already flagged to be removed from repository!");
			}
		}
		else {
			///logger.warning("Unknown option for -DELETE- event has occured for target system: " + getResource().getDisplayName());
		}
		
		return true;
	}
	
	
	

	/**
	 * @param claimedAccount the claimedAccount to set
	 */
	public void setClaimedAccount(Account claimedAccount) {
		this.claimedAccount = claimedAccount;
	}

	/**
	 * @return the claimedAccount
	 */
	public Account getClaimedAccount() {
		return claimedAccount;
	}
}
