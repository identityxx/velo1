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
package velo.actions.tools;

import velo.entity.Account;
import velo.entity.ResourceGroup;

public class RemoveGroupMembershipTools extends AccountTools {
	
	/**
	 * The account to remove the membership from
	 */
	private Account account;
	
	/**
	 * The target system group to remove the group membership from
	 */
	private ResourceGroup tsg;
	
	
	public RemoveGroupMembershipTools(Account account, ResourceGroup tsg) {
		super(account.getUser(),tsg.getResource());
		setTsg(tsg);
		setAccount(account);
	}


	/**
	 * @param tsg The tsg to set.
	 */
	public void setTsg(ResourceGroup tsg) {
		this.tsg = tsg;
	}

	/**
	 * @return Returns the tsg.
	 */
	public ResourceGroup getTsg() {
		return tsg;
	}


	/**
	 * @param account The account to set.
	 */
	public void setAccount(Account account) {
		this.account = account;
	}


	/**
	 * @return Returns the account.
	 */
	public Account getAccount() {
		return account;
	}
}
