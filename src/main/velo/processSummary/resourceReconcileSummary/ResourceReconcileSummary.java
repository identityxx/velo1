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
package velo.processSummary.resourceReconcileSummary;

import java.util.ArrayList;
import velo.processSummary.ProcessSummary;

public class ResourceReconcileSummary extends ProcessSummary {
	public ResourceReconcileSummary(boolean create) {
		if (create) {
			accountsInsertedToRepository = new ArrayList<SummaryAccount>();
			accountsRemovedFromRepository = new ArrayList<SummaryAccount>();
			groupsInsertedToRepository = new ArrayList<SummaryGroup>();
			groupsRemovedFromRepository = new ArrayList<SummaryGroup>();
		}
	}

	public ResourceReconcileSummary() {
		
	}
	
	public ArrayList<SummaryAccount> accountsInsertedToRepository;// = new ArrayList<SummaryAccount>();
	public ArrayList<SummaryAccount> accountsRemovedFromRepository;// = new ArrayList<SummaryAccount>();
	public ArrayList<SummaryGroup> groupsInsertedToRepository;  //= new ArrayList<SummaryAccount>();
	public ArrayList<SummaryGroup> groupsRemovedFromRepository;  //= new ArrayList<SummaryAccount>();
	public long accountsAmountInResource;
	public long groupsAmountInResource;
	/**
	 * @return the accountsInsertedToRepository
	 */
	public ArrayList<SummaryAccount> getAccountsInsertedToRepository() {
		return accountsInsertedToRepository;
	}

	/**
	 * @param accountsInsertedToRepository the accountsInsertedToRepository to set
	 */
	public void setAccountsInsertedToRepository(
			ArrayList<SummaryAccount> accountsInsertedToRepository) {
		this.accountsInsertedToRepository = accountsInsertedToRepository;
	}

	/**
	 * @return the accountsRemovedFromRepository
	 */
	public ArrayList<SummaryAccount> getAccountsRemovedFromRepository() {
		return accountsRemovedFromRepository;
	}

	/**
	 * @param accountsRemovedFromRepository the accountsRemovedFromRepository to set
	 */
	public void setAccountsRemovedFromRepository(
			ArrayList<SummaryAccount> accountsRemovedFromRepository) {
		this.accountsRemovedFromRepository = accountsRemovedFromRepository;
	}
	
	
	public int getAccountsRemovedFromRepositoryAmount() {
		return getAccountsRemovedFromRepository().size();
	}
	
	public int getAccountsInsertedToRepositoryAmount() {
		return getAccountsInsertedToRepository().size();
	}

	/**
	 * @return the accountsAmountInResource
	 */
	public long getAccountsAmountInResource() {
		return accountsAmountInResource;
	}

	/**
	 * @param accountsAmountInResource the accountsAmountInResource to set
	 */
	public void setAccountsAmountInResource(long accountsAmountInResource) {
		this.accountsAmountInResource = accountsAmountInResource;
	}

	/**
	 * @return the groupsAmountInResource
	 */
	public long getGroupsAmountInResource() {
		return groupsAmountInResource;
	}

	/**
	 * @param groupsAmountInResource the groupsAmountInResource to set
	 */
	public void setGroupsAmountInResource(long groupsAmountInResource) {
		this.groupsAmountInResource = groupsAmountInResource;
	}

	/**
	 * @return the groupsInsertedToRepository
	 */
	public ArrayList<SummaryGroup> getGroupsInsertedToRepository() {
		return groupsInsertedToRepository;
	}

	/**
	 * @param groupsInsertedToRepository the groupsInsertedToRepository to set
	 */
	public void setGroupsInsertedToRepository(
			ArrayList<SummaryGroup> groupsInsertedToRepository) {
		this.groupsInsertedToRepository = groupsInsertedToRepository;
	}

	/**
	 * @return the groupsRemovedFromRepository
	 */
	public ArrayList<SummaryGroup> getGroupsRemovedFromRepository() {
		return groupsRemovedFromRepository;
	}

	/**
	 * @param groupsRemovedFromRepository the groupsRemovedFromRepository to set
	 */
	public void setGroupsRemovedFromRepository(
			ArrayList<SummaryGroup> groupsRemovedFromRepository) {
		this.groupsRemovedFromRepository = groupsRemovedFromRepository;
	}
}
