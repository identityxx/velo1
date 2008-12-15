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
package velo.spml2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.OpenContentElement;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLValue;

import velo.windowsGateway.VeloDataContainerProxy;

public class SpmlUtils {
	private static Logger log = Logger.getLogger(SpmlUtils.class.getName());
	
	public static List<String> getGroupsToAssign(AddRequest addRequest, String resourceUniqueName) {
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
	
	
	
	
	
	
	public static Map<String,List<String>> getGroupMembershipToModify(ModifyRequest modRequest, String resourceUniqueName) {
		Map<String,List<String>> lists = new HashMap<String,List<String>>();
		List<String> groupsToAdd = new ArrayList<String>();
		List<String> groupsToRevoke = new ArrayList<String>();
		
		lists.put("groupsToAssign", groupsToAdd);
		lists.put("groupsToRevoke", groupsToRevoke);
		
		
		
		Modification[] modifications = modRequest.getModifications();
		if (modifications.length < 1) {
			log.debug("Could not consruct group membership to assign/revoke since no modification tags exists within the SPML Modify Request!");
			//return empty lists
			return lists;
		}
		
		for (int i=0; i<modifications.length;i++) {
			//recognize whether this is a capabilitydata modify or not
			if ( (modifications[i].getCapabilityData() != null) && (modifications[i].getCapabilityData().length > 0) ) {
				CapabilityData[] cDatas = modifications[i].getCapabilityData();
				CapabilityData cData = cDatas[0];
				
				if (cData.getReference().length > 0) {
					Reference[] refs = cData.getReference();
					for (int l=0;l<refs.length;l++) {
						//TODO: Currently only supported group-membership of the same system
						if ( (refs[l].getTypeOfReference().equals("memberOf")) && (refs[l].getToPsoID().getTargetID().equals(resourceUniqueName)) ) {
							if (modifications[i].getModificationMode() == ModificationMode.ADD) {
								groupsToAdd.add(refs[l].getToPsoID().getID());
							} else if (modifications[i].getModificationMode() == ModificationMode.DELETE) {
								groupsToRevoke.add(refs[l].getToPsoID().getID());
							} else {
								log.info("This type of modification mode is not supported: " + modifications[i].getModificationMode());
								continue;
							}
						} else {
							log.info("Skipping!, type of reference is not 'memberOf' (value is: '" + refs[l].getTypeOfReference() + "') or resource name is not '" + resourceUniqueName + "' (value is '" + refs[l].getToPsoID().getTargetID() +"')");
						}
						
					}
				}
			}
		}
			
		log.debug("Successfully created groups to assign/revoke lists for Modify Request...");
		return lists;
	}
	
	
	/*Rewritten after adding support for attributes modifications via ModifyRequest
	public static Map<String,List<String>> getGroupMembershipToModify(ModifyRequest modRequest, String resourceUniqueName) {
		Map<String,List<String>> lists = new HashMap<String,List<String>>();
		List<String> groupsToAdd = new ArrayList<String>();
		List<String> groupsToRevoke = new ArrayList<String>();
		
		Modification[] modifications = modRequest.getModifications();
		if (modifications.length < 1) {
			log.warn("Could not consruct group membership to assign/revoke since no modification tags exists within the SPML Modify Request!");
			//return empty lists
			return lists;
		}
		
		for (int i=0; i<modifications.length;i++) {
			boolean isInsertion = false;
			
			if (modifications[i].getModificationMode() == ModificationMode.ADD) {
				isInsertion = true;
			} else if (modifications[i].getModificationMode() == ModificationMode.DELETE) {
				isInsertion = false;
			} else {
				//log.error("Modification type is not supported!");
				//return null;
				//not supported here, then do nothing. (might be of REPLACE type that is relevant for 
				continue;
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
	*/
	
	
	public static Map<String,List<String>> getAttributesToModify(ModifyRequest modReq, ModificationMode modificationMode) {
		Map<String,List<String>> attrsMap = new HashMap<String,List<String>>();
		Modification[] modifications = modReq.getModifications();
		
		//find all modifications that are of 'REPLACE' type
		for (int i=0; i< modifications.length;i++) {
			Modification currMod = modifications[i];
			if ( (modificationMode == null) || (modifications[i].getModificationMode() == modificationMode) ){
				//make sure there's a data (for references like group membership there is no data)
				if ( (currMod.getData() != null) ) {
					if (currMod.getData().getOpenContentElements().length > 0) {
						OpenContentElement[] elements = currMod.getData().getOpenContentElements();
						
						for (int j=0;j<elements.length;j++) {
							//make sure each element is an DSMLAttr.
							if (elements[j] instanceof DSMLAttr) {
								DSMLAttr currAttr = (DSMLAttr)elements[j];
								DSMLValue[] vals = currAttr.getValues();
								
								List<String> valsAsList = new ArrayList<String>();
								for (int q=0;q<vals.length;q++) {
									DSMLValue currVal = vals[q];
									//System.out.println("Value: " + currVal.getValue());
									valsAsList.add(currVal.getValue());
								}
								
								attrsMap.put(currAttr.getName(), valsAsList);
							}
						}
					} else {
						//an empty attributes modifications?
					}
				} else {
					//irrelevant.
				}
			}
		}
		
		
		return attrsMap;
	}
}
