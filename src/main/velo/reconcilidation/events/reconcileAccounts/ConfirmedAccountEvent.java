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
 * An event that occures when a Confirmed account was found while Reconcile Account process executed
 *
 * (A 'Confirmed' account event occures when the found account already exist in the Database and already assigned to a user)
 * 
 * @author Asaf Shakarchi
 */
public class ConfirmedAccountEvent extends AccountEvent {

	private static Logger logger = Logger.getLogger(ConfirmedAccountEvent.class.getName());
	private Account claimedAccount;
	
	public boolean execute() {
		logger.finest("-CONFIRMED- account event has occured for account name: '" + getClaimedAccount().getName() + "', in target system name: '" + getResource().getDisplayName() + "'");
		logger.finest("Action to take is: " + getReconcilePolicy().getConfirmedAccountEventAction());
		
		/*
		switch (getReconcilePolicy().getConfirmedAccountEventAction()) {
			case NOTHING:
				break;
		}
		*/
		
		if (getReconcilePolicy().getConfirmedAccountEventAction().equals("NOTHING")) {
			logger.finest("'NOTHING' was choosed, nothing left to do for account confirm event for account name: " + getClaimedAccount().getName());
		}
		else {
			logger.warning("Skipping 'CONFIRMED ACCOUNT' event, could not find the event to execute for value: " + getReconcilePolicy().getConfirmedAccountEventAction());
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
