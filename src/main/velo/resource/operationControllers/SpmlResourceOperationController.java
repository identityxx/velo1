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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.OpenContentElement;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLValue;

import velo.action.ResourceOperation;
import velo.entity.Resource;
import velo.entity.SpmlTask;
import velo.exceptions.OperationException;

public abstract class SpmlResourceOperationController extends ResourceOperationController {
	private static Logger log = Logger.getLogger(SpmlResourceOperationController.class.getName());
	
	/**
	 * The resource object the operation is performed against
	 */
	Resource resource;

	//note: resourceType factoring resourceOperationControllers and expecting empty construct.
	public SpmlResourceOperationController() {
		
	}
	
	
	public SpmlResourceOperationController(Resource resource) {
		super(resource);
	}
	
	
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
	
	
	
	//all controllers must override these basic provisioning methods methods
	public abstract void performOperation(SpmlTask spmlTask, ResourceOperation ro, SuspendRequest suspendRequest) throws OperationException ;
	public abstract void performOperation(SpmlTask spmlTask, ResourceOperation ro, ResumeRequest resumeRequest) throws OperationException;
	public abstract void performOperation(SpmlTask spmlTask, ResourceOperation ro, DeleteRequest deleteRequest) throws OperationException;
	public abstract void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest addRequest) throws OperationException;
	public abstract void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException;
	
	
	
	
	//public abstract void perform(SpmlTask spmlTask, org.openspml.v2.msg.spml.Request request) throws OperationException ;
	//Every resource operations controller must inherit these minimal actions
	//public abstract void perform(SpmlSuspendRequest request);

	
	
	
	///helper
	//helper/tools
	//TODO: Do not use this method anymore! instead use SpmlUtils.getGroupsToAssign()
	@Deprecated
	public List<String> getGroupsToAssign(AddRequest addRequest, String resourceUniqueName) {
		List<String> groups = new ArrayList<String>();
		
		CapabilityData[] cDatas = addRequest.getCapabilityData();
		if (cDatas.length < 1) {
			return null;
		}
		
		//expecting one capability data...
		CapabilityData firstCapData = cDatas[0];
		Reference[] refsInFirstCapData = firstCapData.getReference();

		for (int i=0;i<refsInFirstCapData.length;i++) {
			if ( (refsInFirstCapData[i].getTypeOfReference().equals("memberOf")) && (refsInFirstCapData[i].getToPsoID().getTargetID().equals(resourceUniqueName)) ) {
				groups.add(refsInFirstCapData[i].getToPsoID().getID());
			}
		}
		
		return groups;
	}
	
	
	public Map<String,List<String>> getGroupMembershipToModify(ModifyRequest modRequest, String resourceUniqueName) {
		Map<String,List<String>> lists = new HashMap<String,List<String>>();
		List<String> groupsToAdd = new ArrayList<String>();
		List<String> groupsToRevoke = new ArrayList<String>();
		
		Modification[] modifications = modRequest.getModifications();
		if (modifications.length < 1) {
			log.warn("Could not consruct group membership to assign/revoke since no modification tags exists within the SPML Modify Request!");
			return null;
		}
		
		for (int i=0; i<modifications.length;i++) {
			boolean isInsertion = false;
			
			if (modifications[i].getModificationMode() == ModificationMode.ADD) {
				isInsertion = true;
			} else if (modifications[i].getModificationMode() == ModificationMode.DELETE) {
				isInsertion = false;
			} else {
				log.error("Modification type is not supported!");
				return null;
			}
			
			//expecting one capabilityData per modification
			CapabilityData[] cDatas = modifications[i].getCapabilityData();
			CapabilityData cData = cDatas[0];
			Reference[] refs = cData.getReference();
			for (int l=0;l<refs.length;l++) {
				//TODO: Currently only supported group-membership of the same system
				if ( (refs[l].getTypeOfReference().equals("memberOf")) && (refs[l].getToPsoID().getTargetID().equals(resourceUniqueName)) ) {
					if (isInsertion) {
						groupsToAdd.add(refs[l].getToPsoID().getID());
					} else {
						groupsToRevoke.add(refs[l].getToPsoID().getID());
					}
				}
			}
		}
		
		lists.put("groupsToAssign", groupsToAdd);
		lists.put("groupsToRevoke", groupsToRevoke);
		
		log.debug("Successfully created groups to assign/revoke lists for Modify Request...");
		return lists;
	}
	
}
