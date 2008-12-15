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
package velo.reconcilidation.events;

import velo.entity.Account;

/**
 * A class that represents an AccountAnalyerEvent
 * 
 * @author Asaf Shakarchi
 */
abstract public class AccountAnalyzerEvent implements EventInterface {
	
	private Account claimedAccount;
	private Account activeAccount;
	
	/**
	 * Constructor
	 * Initialize the AccountAnalyer event and set the active & claimed accounts
	 * @param claimedAccount The 'claimed' account that resides in the IDM database
	 * @param activeAccount The 'Active' account listed from the resource itself by the Reconcile Process
	 */
	public AccountAnalyzerEvent(Account claimedAccount, Account activeAccount) {
		setClaimedAccount(claimedAccount);
		setActiveAccount(activeAccount);
	}
	
	/**
	 * @param claimedAccount The claimedAccount to set.
	 */
	public void setClaimedAccount(Account claimedAccount) {
		this.claimedAccount = claimedAccount;
	}

	/**
	 * @return Returns the claimedAccount.
	 */
	public Account getClaimedAccount() {
		return claimedAccount;
	}

	/**
	 * @param activeAccount The activeAccount to set.
	 */
	public void setActiveAccount(Account activeAccount) {
		this.activeAccount = activeAccount;
	}

	/**
	 * @return Returns the activeAccount.
	 */
	public Account getActiveAccount() {
		return activeAccount;
	}
	
	public abstract boolean execute();
}
