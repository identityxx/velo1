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
package velo.actions;

import java.io.File;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;

import velo.common.SysConf;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.entity.Resource;
import velo.entity.TaskDefinition;
import velo.exceptions.ActionFactoryException;
import velo.exceptions.FactoryException;
import velo.exceptions.ResourceTypeDescriptorException;
import velo.exceptions.ScriptLoadingException;
import velo.patterns.Factory;
import velo.resource.resourceTypeDescriptor.ResourceTypeDescriptor;
import velo.scripting.ScriptFactory;
import velo.tools.FileUtils;

/**
 * @author Administrator
 */
@Deprecated
public abstract class ResourceActionFactory extends ActionFactory {
	private static Logger logger = Logger.getLogger(ResourceActionFactory.class.getName());
	
	private Resource resource;
	private ResourceTypeDescriptor resourceTypeDescriptor;

	public ResourceActionFactory() {
		
	}
	
	public ResourceActionFactory(Resource ts) throws ActionFactoryException {
		ts.clearReferences();
		this.resource = ts;
		//Currently, Only if not TS type is not scripted retrieve the TS type descriptor
		if (!ts.getResourceType().isScripted()) {
		try {
			InitialContext ic = new InitialContext();
			ResourceManagerLocal resourceManager = (ResourceManagerLocal) ic.lookup("velo/ResourceBean/local");
			setResourceTypeDescriptor(resourceManager.factoryResourceTypeDescriptor(ts.getResourceType()));
		}
		catch (ResourceTypeDescriptorException tstde) {
			throw new ActionFactoryException(tstde.getMessage());
		}
		catch (NamingException ne) {
			throw new ActionFactoryException(ne.getMessage());
		}
		}
	}
	
	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}
	
	
	
	/**
	 * @param resourceTypeDescriptor the resourceTypeDescriptor to set
	 */
	public void setResourceTypeDescriptor(ResourceTypeDescriptor resourceTypeDescriptor) {
		this.resourceTypeDescriptor = resourceTypeDescriptor;
	}

	/**
	 * @return the resourceTypeDescriptor
	 */
	public ResourceTypeDescriptor getResourceTypeDescriptor() {
		return resourceTypeDescriptor;
	}

	public ResourceAction factoryResourceActionByTaskDefinition(TaskDefinition td) {
		return null;
	}
	
	
	
	
	
	
	/**
	 * Get a resource name constructed from the action name and the target
	 * system to perform the action on
	 * 
	 * @param actionPartialFileName The partial name of the action
	 * @param resource The target-system to perform the action on
	 * @return A full resource(file) name of the action
	 */
	@Deprecated
	public String getResourceActionName(String actionPartialFileName, Resource resource) {
		// Get a propercase of the short target system name
		// String scriptName =
		// StringUtils.capitalize(this.getShortName().toLowerCase());
		// Decided at the end to go all for lowercase
		String scriptName = resource.getUniqueName().toLowerCase() + "_";
		// Now add the actionName to the scriptName
		scriptName += actionPartialFileName.toLowerCase();

		Configuration sysConf = SysConf.getSysConf();
		String resourceScriptsRootDirectory = sysConf.getString("system.directory.user_workspace_dir");
		String scriptResourceName = resourceScriptsRootDirectory
				+ "/targets_scripts/"
				+ resource.getUniqueName().toLowerCase() + "/" + "actions"
				+ "/" + scriptName
				+ sysConf.getString("scripts.file_extension");

		System.out.println("******************: " + scriptResourceName);
		return scriptResourceName;
	}
	
	
	
	
	/**
	 * Factory a Resource scriptable action
	 * 
	 * @param scriptResourceName
	 *            The resource (class) name of the scripted action class
	 * @param resource
	 *            The Resource object the action referes to
	 * @return A scriptable action object returned casted as
	 *         ResourceActionInterface
	 * @throws ScriptLoadingException
	 */
	public ResourceActionInterface factoryScriptedResourceAction(
			String scriptResourceName, Resource resource)
			throws ScriptLoadingException {

		ScriptFactory sf = new ScriptFactory();
		try {
			Object scriptObj = sf.factoryScriptableObjectByResourceName(scriptResourceName);
			
			ResourceActionInterface scriptedAction = (ResourceActionInterface) scriptObj;
			// Set the target system into the scriptedAction
			scriptedAction.setResource(resource);

			// ResourceActionInterface cc = (ResourceActionInterface)
			// new QflowListAccounts();

			return scriptedAction;
			// return cc;
		} catch (ScriptLoadingException sle) {
			throw sle;
		}
	}
	
	
	
	
	
	/**
	 * @param actionClassName
	 *            The className of the action
	 * @param resource
	 *            The Resource object the action referes to
	 * @return An action object returned casted as a ResourceActionInterface
	 * @throws ClassNotFoundException
	 *             thrown when the specified class name is not found.
	 */
	public ResourceActionInterface factoryResourceJavaAction(
			String actionClassName, Resource resource)
			throws FactoryException {
		logger.info("Factoring 'Java Action' for class name: "+ actionClassName);
		
		if (actionClassName == null) {
			throw new FactoryException("Class recieved is null!");
		}
		
		Object javaActionObj = Factory.factoryInstance(actionClassName);
		ResourceActionInterface tsai = (ResourceActionInterface) javaActionObj;
		// Set the target system into the scriptedAction
		tsai.setResource(resource);
		return tsai;
	}
	
	
	
	public String getScriptContent(String actionPartialFileName,Resource ts) throws ScriptLoadingException {
		String fileName = getResourceActionName(actionPartialFileName,ts);
		File f = new File(fileName);
		if (f.isFile()) {
			return FileUtils.getContents(f);			
		}
		else {
			throw new ScriptLoadingException("Cannot find file with expected name: '" + fileName + "'");
		}
		
	}
}
