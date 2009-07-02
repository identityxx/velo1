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
import velo.ejb.interfaces.AccountManagerRemote;
import velo.entity.Account;
import velo.entity.Task;

public class DeleteAccountTaskExecuter extends DefaultTaskExecuter {
	AccountManagerRemote am;
	
	public DeleteAccountTaskExecuter() {
		try {
			Context ic = new InitialContext();
			AccountManagerRemote amRemote = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
			
			am = amRemote;
		} catch (NamingException ne) {
			ne.printStackTrace();
		}
	}
	
	/*
	public boolean execute(Task task) {
		ActionInterface action = getActionFromTask(task);
		if (super.execute(task)) {
			if (action != null) {
				ResourceAccountActionInterface accountAction = (ResourceAccountActionInterface)action;
				Account delAccount = accountAction.getAccount();
				if (delAccount != null) {
					System.out.println("Account name '"+delAccount.getName()+"' on resource: " + delAccount.getResource().getDisplayName() + " was DELETED successfully from resource, deleting the account entity too.");
					am.removeAccountEntity(accountAction.getAccount());
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
		else {
			return false;
		}
	}*/
}
