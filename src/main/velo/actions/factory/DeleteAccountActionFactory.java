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
package velo.actions.factory;

import java.util.logging.Logger;

import velo.actions.AccountActionFactory;
import velo.actions.ResourceAccountActionInterface;
import velo.actions.ResourceActionInterface;
import velo.actions.tools.AccountTools;
import velo.entity.Account;
import velo.exceptions.ActionFactoryException;
import velo.exceptions.FactoryException;
import velo.exceptions.ScriptLoadingException;

public class DeleteAccountActionFactory extends AccountActionFactory {
	private final String actionPartialFileName = "delete_account";
	
	private static Logger logger = Logger.getLogger(DeleteAccountActionFactory.class.getName());
	
	public DeleteAccountActionFactory() {
		
	}
	
	public DeleteAccountActionFactory(Account account) throws ActionFactoryException{ 
		super(account.getResource(),account);
	}
	

	/**
	 * Factory a 'delete account' action
	 * 
	 * @return a 'ResourceAccountActionInterface' object
	 * @throws ActionFactoryException
	 */
	public ResourceAccountActionInterface factory() throws ActionFactoryException {
		if (getAccount().getResource().getResourceType().isScripted()) {
			try {
				String scriptResourceName = getResourceActionName(actionPartialFileName,getAccount().getResource());
				ResourceActionInterface tsai = (factoryScriptedResourceAction(scriptResourceName, getAccount().getResource()));
				// Cast it to the proper interface
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;
				// Set the user into the object
				tsaai.setUser(getAccount().getUser());
				tsaai.setAccount(getAccount());

				//Set the account tools into the action.
				AccountTools atools = new AccountTools(getAccount().getUser(),getAccount().getResource());
				tsaai.setTools(atools);
				
				return tsaai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				String className = (String)getResourceTypeDescriptor().getActionsClasses().get(actionPartialFileName);
				logger.finest("Target system '" + getAccount().getResource().getDisplayName()+"' is NOT scripted, creating new instance of class name: " + className);
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface)factoryResourceJavaAction(className,getAccount().getResource());
				//Set the user into the object
				tsaai.setUser(getAccount().getUser());
				tsaai.setAccount(getAccount());
				AccountTools atools = new AccountTools(getAccount().getUser(),getAccount().getResource());
				tsaai.setTools(atools);
				return tsaai;
			}
			catch (FactoryException fe) {
				throw new ActionFactoryException(fe.getMessage());
			}
		}
	}
}
