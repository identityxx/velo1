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
package velo.resource.operationControllers;

import velo.action.ResourceOperation;
import velo.contexts.OperationContext;
import velo.entity.Resource;
import velo.entity.ResourceTask;
import velo.exceptions.AuthenticationFailureException;
import velo.exceptions.OperationException;

public abstract class ResourceOperationController {
	
	public ResourceOperationController() {
		
	}
	
	public ResourceOperationController(Resource resource) {
		setResource(resource);
	}
	
	public abstract void init(OperationContext context);
	
	/**
	 * The resource object the operation is performed against
	 */
	Resource resource;

	
	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	

	public abstract void resourceFetchActiveDataOffline(ResourceOperation ro, ResourceTask resourceTask) throws OperationException;
	
	public boolean authenticate(String userName, String password) throws AuthenticationFailureException {
		throw new AuthenticationFailureException("Authentication for this resource is not supported!");
	}
}
