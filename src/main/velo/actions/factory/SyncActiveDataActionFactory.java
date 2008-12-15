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

import velo.actions.ResourceActionFactory;
import velo.actions.ResourceActionInterface;
import velo.actions.tools.SyncGroupMembershipTools;
import velo.entity.Resource;
import velo.exceptions.ActionFactoryException;
import velo.exceptions.FactoryException;
import velo.exceptions.ScriptLoadingException;

public class SyncActiveDataActionFactory extends ResourceActionFactory {
	private final String actionPartialFileName = "sync_active_data";
	
	private static Logger logger = Logger.getLogger(SyncActiveDataActionFactory.class.getName());
	
	public SyncActiveDataActionFactory() {
		
	}
	
	public SyncActiveDataActionFactory(Resource ts) throws ActionFactoryException { 
		super(ts);
	}
	

	/**
	 * Factory a 'sync active data' action
	 * 
	 * @return a 'resourceActionInterface' object
	 * @throws ActionFactoryException
	 */
	public ResourceActionInterface factory() throws ActionFactoryException {
		logger.fine("Factoring sync action for Target name: " + getResource().getDisplayName());
		
		if (getResource().getResourceType().isScripted()) {
			try {
				String scriptResourceName = getResourceActionName(actionPartialFileName,getResource());
				ResourceActionInterface tsai = (factoryScriptedResourceAction(scriptResourceName, getResource()));
				
				//TODO: why GroupMembershipTools? if needed, create a special tools for sync, even if they are equal.
				tsai.setTools(new SyncGroupMembershipTools(null,getResource()));
				
				return tsai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				String className = (String)getResourceTypeDescriptor().getActionsClasses().get(actionPartialFileName);
				logger.finest("Target system '" + getResource().getDisplayName()+"' is NOT scripted, creating new instance of class name: " + className);
				ResourceActionInterface tsai = (ResourceActionInterface)factoryResourceJavaAction(className,getResource());
				
				//TODO: why GroupMembershipTools? if needed, create a special tools for sync, even if they are equal.
				tsai.setTools(new SyncGroupMembershipTools(null,getResource()));

				return tsai;
			}
			catch (FactoryException fe) {
				throw new ActionFactoryException(fe.getMessage());
			}
		}
	}
	
	public String getScriptContent(Resource ts) throws ScriptLoadingException {
		return super.getScriptContent(actionPartialFileName, ts);
	}
}
