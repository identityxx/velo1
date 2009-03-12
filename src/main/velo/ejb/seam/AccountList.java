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
package velo.ejb.seam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.Account;

@Name("accountList")
public class AccountList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(account.name) like concat(lower(#{accountList.account.name}),'%')",
			"account.resource.uniqueName = #{accountList.resourceUniqueName}"};

	private Account account = new Account();

	private Map<Account, Boolean> accountSelection = new HashMap<Account, Boolean>();
	
	private String resourceUniqueName;

	private static final String EJBQL = "select account from Account account";

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public Account getAccount() {
		return account;
	}

	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
	
	
	
	
	
	public Map<Account, Boolean> getAccountSelection() {
		return accountSelection;
	}

	/**
	 * @return the resourceUniqueName
	 */
	public String getResourceUniqueName() {
		return resourceUniqueName;
	}

	/**
	 * @param resourceUniqueName the resourceUniqueName to set
	 */
	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}

}
