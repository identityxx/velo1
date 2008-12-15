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

import java.util.List;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.Request;

import velo.action.ResourceOperation;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.SpmlTask;
import velo.exceptions.OperationException;

public abstract class GroupMembershipSpmlResourceOpreationController extends SpmlResourceOperationController {
	private static Logger log = Logger.getLogger(GroupMembershipSpmlResourceOpreationController.class.getName());
	
	
	public GroupMembershipSpmlResourceOpreationController() {
		super();
	}
	
	public GroupMembershipSpmlResourceOpreationController(Resource resource) {
		super(resource);
	}
	
	public abstract void performOperationAddGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToAdd) throws OperationException; 
	public abstract void performOperationRemoveGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToRemove) throws OperationException;
	//public abstract void performOperationModifyGroupMembership(SpmlTask spmlTask, ResourceOperation ro, AddRequest addRequest, List<String> groupsToAdd, List<String> groupsToRemove) throws OperationException;
	
}
