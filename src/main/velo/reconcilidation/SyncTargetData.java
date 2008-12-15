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
package velo.reconcilidation;

import java.util.LinkedList;

import velo.entity.Account;

public class SyncTargetData {
	private LinkedList<Account> accounts = new LinkedList<Account>();
	private LinkedList<ActiveGroup> groups = new LinkedList<ActiveGroup>();
	private LinkedList<ActiveGroup> groupsMembership = new LinkedList<ActiveGroup>();

	/**
	 * @param accounts The accounts to set.
	 */
	public void setAccounts(LinkedList<Account> accounts) {
		this.accounts = accounts;
	}

	/**
	 * @return Returns the accounts.
	 */
	public LinkedList<Account> getAccounts() {
		return accounts;
	}
	
	public void addAccount(Account ac) {
		getAccounts().add(ac);
	}

	/**
	 * @param groups The groups to set.
	 */
	public void setGroups(LinkedList<ActiveGroup> groups) {
		this.groups = groups;
	}

	/**
	 * @return Returns the groups.
	 */
	public LinkedList<ActiveGroup> getGroups() {
		return groups;
	}
	
	/**
	 * @param groupsMembership the groupsMembership to set
	 */
	public void setGroupsMembership(LinkedList<ActiveGroup> groupsMembership) {
		this.groupsMembership = groupsMembership;
	}

	/**
	 * @return the groupsMembership
	 */
	public LinkedList<ActiveGroup> getGroupsMembership() {
		return groupsMembership;
	}
	

	public void addGroup(ActiveGroup ac) {
		ac.setUniqueId(ac.getUniqueId().toUpperCase());
		getGroups().add(ac);
	}
	
	public void addGroupForMembership(ActiveGroup ac) {
		System.out.println("Found!");
		getGroupsMembership().add(ac);
	} 
}
