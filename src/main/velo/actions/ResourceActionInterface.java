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

import velo.actions.tools.ResourceActionTools;
import velo.entity.Resource;

/**
 * The Action interface for all typed target system actions
 * Required by Groovy Class Loader in order to easily manage scriptable actions with one interface
 * 
 * 
 * @author Asaf Shakarchi
 */
@Deprecated
public interface ResourceActionInterface extends ActionInterface {
	
	/**
	 * @param targetSystem A target system object to set to execute the action on
	 */
	public void setResource(Resource targetSystem);
	
	/**
	 * @return A set Resource object the action is execution on
	 */
	public Resource getResource();

	/*
	 * Get the Adapter object
	 * @return Adapter
	 * @throws FactoryException
	 */
	//public Adapter getAdapter() throws AdapterException;
	
	/**
	 * Get the ResourceActionTools object
	 * @return ResourceActionTools object
	 */
	public ResourceActionTools getTools();
	
	/**
	 * Accessors for the ActionTools object, short method names are used for easy access by scripts
	 * @param targetSystemActionTools The targetSystemActionTools to set.
	 */
	public void setTools(ResourceActionTools targetSystemActionTools);
}