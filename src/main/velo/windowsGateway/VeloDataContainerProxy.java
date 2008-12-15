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
package velo.windowsGateway;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.velo_wingw.InvokePhases;
import org.datacontract.schemas._2004._07.velo_wingw.ResourceAction;
import org.datacontract.schemas._2004._07.velo_wingw.ResourceActions;
import org.datacontract.schemas._2004._07.velo_wingw.VeloDataContainer;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;
import org.openspml.v2.profiles.dsml.DSMLAttr;

import sun.misc.BASE64Encoder;
import velo.action.ResourceOperation;
import velo.entity.AttributeValue;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.exceptions.ObjectsConstructionException;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import velo.spml2.SpmlUtils;
import velo.tools.FileUtils;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfKeyValueOfstringArrayOfstringty7Ep6D1;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfKeyValueOfstringstring;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfKeyValueOfstringArrayOfstringty7Ep6D1.KeyValueOfstringArrayOfstringty7Ep6D1;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfKeyValueOfstringstring.KeyValueOfstringstring;

public class VeloDataContainerProxy {
	private static Logger log = Logger.getLogger(VeloDataContainerProxy.class.getName());
	
	private VeloDataContainer veloDataContainer;
	ArrayOfKeyValueOfstringstring resourceConfParams = new ArrayOfKeyValueOfstringstring();
	ArrayOfKeyValueOfstringArrayOfstringty7Ep6D1 attributes = new ArrayOfKeyValueOfstringArrayOfstringty7Ep6D1();
	ArrayOfstring groupsToAdd = new ArrayOfstring();
	ArrayOfstring groupsToRemove = new ArrayOfstring();
	ResourceActions resourceActions = new ResourceActions();
	
	
	org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory factoryVDC = new org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory();
	
	private VeloDataContainer getInstance() {
		if (veloDataContainer == null) {
			veloDataContainer = new VeloDataContainer();
			return veloDataContainer;
		} else {
			return veloDataContainer;
		}
	}
	
	public VeloDataContainer factoryVeloDataContainer() {
		JAXBElement<ArrayOfKeyValueOfstringstring> preparedParams = factoryVDC.createVeloDataContainerResourceConfParams(resourceConfParams);
		getInstance().setResourceConfParams(preparedParams);
		
		JAXBElement<ArrayOfKeyValueOfstringArrayOfstringty7Ep6D1> attrs = factoryVDC.createVeloDataContainerAttributes(attributes);
		getInstance().setAttributes(attrs);
		
		JAXBElement<ArrayOfstring> jaxGroupsToAdd = factoryVDC.createVeloDataContainerGroupsToAdd(groupsToAdd);
		getInstance().setGroupsToAdd(jaxGroupsToAdd);
		
		JAXBElement<ArrayOfstring> jaxGroupsToRemove = factoryVDC.createVeloDataContainerGroupsToRemove(groupsToRemove);
		getInstance().setGroupsToRemove(jaxGroupsToRemove);
	
		JAXBElement<ResourceActions> jaxResourceActions = factoryVDC.createResourceActions(resourceActions);
		getInstance().setResourceActions(jaxResourceActions);
		return getInstance();
	}
	
	public void addResourceConfParam(String name, String value) {
		KeyValueOfstringstring newEntry = new KeyValueOfstringstring();
		newEntry.setKey(name);
		newEntry.setValue(value);
		
		/*
		String content = FileUtils.getContents(new File("c:/1.txt"));
		//newEntry.setValue(content);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!: ORIGINAL ENCODING OF VALUE!" + value);
		
		String bla = new BASE64Encoder().encodeBuffer(content.getBytes());
		newEntry.setValue(bla);
		*/
		/*
		try {
			//String utf8Value = new String(value.getBytes(), "ASCII");
			//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!: UTF ENCODING OF VALUE!" + utf8Value);
			//newEntry.setValue(utf8Value);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
		
		/*
		try {
		String myUTF8 = new String(value.getBytes("UTF-8"),"ISO-8859-8");
		newEntry.setValue(myUTF8);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
		
		//newEntry.setValue(value);
		
		
		//newEntry.setValue(value);
		//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!: WAAAAAAAAAAAAAAAAAAAAAA!" + value);
		
		/*
		try {
			newEntry.setValue(new String(value.getBytes(),"ISO-8859-1"));
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			}
			*/
		/*
		try {
			newEntry.setValue(new String(value.getBytes(),"UTF-8"));
		}catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		}
		*/
		
		resourceConfParams.getKeyValueOfstringstring().add(newEntry);
	}
	
	public void addAttribute(String attrName, List<String> values) {
		KeyValueOfstringArrayOfstringty7Ep6D1 kval = new KeyValueOfstringArrayOfstringty7Ep6D1();
		//all keys are stored uppercase (hopefully this is going to be less confusing)
		kval.setKey(attrName.toUpperCase());
		
		ArrayOfstring arrVals = new ArrayOfstring();
		arrVals.getString().addAll(values);
		
		kval.setValue(arrVals);
		
		attributes.getKeyValueOfstringArrayOfstringty7Ep6D1().add(kval);
	}
	
	public void addAttribute(String attrName, String attrValue) {
		List<String> a = new ArrayList<String>();
		a.add(attrValue);
		
		addAttribute(attrName, a);
	}
	
	public void addGroupToGroupsToAdd(String groupName) {
		log.trace("Importing group named '" + groupName + "'");
		groupsToAdd.getString().add(groupName);
	}
	
	public void addGroupToGroupsToRemove(String groupName) {
		log.trace("Importing group named '" + groupName + "'");
		groupsToRemove.getString().add(groupName);
	}
	
	public void importAttributes(Resource resource, AddRequest addRequest) {
		Map<String,ResourceAttribute> attrsMap = resource.getManagedAttributesAsMap(addRequest.getData().getOpenContentElements(DSMLAttr.class));

		log.debug("Importing attributes from Add Request...");
		log.debug("Constructed attributes size: '" + attrsMap.size() + ", importing attributes...");
		
		for (Map.Entry<String,ResourceAttribute> currAttr : attrsMap.entrySet()) {
			 String keyName = currAttr.getKey();

			 ArrayList<String> arrOfVals = new ArrayList<String>();
			 for (AttributeValue currVal : currAttr.getValue().getValues()) {
				 if (!currVal.isNull()) {
					 arrOfVals.add(currVal.getAsString());
				 }
			 }
			 
			 if (arrOfVals.size() > 0) {
				 addAttribute(keyName,arrOfVals);
			 }
		}
	}
	
	public void importGroupsToAdd(Resource resource, AddRequest addRequest) {
		List<String> groups = SpmlUtils.getGroupsToAssign(addRequest, resource.getUniqueName());
		log.debug("Importing group membership with amount '" + groups.size() + "'");
		
		for (String currGroup : groups) {
			addGroupToGroupsToAdd(currGroup);
		}
	}
	
	public void importGroupsForModifyAccount(Resource resource, ModifyRequest modifyRequest) {
		Map<String,List<String>> groups = SpmlUtils.getGroupMembershipToModify(modifyRequest, resource.getUniqueName());
		log.debug("Importing group membership with amount '" + groups.size() + "'");
		
		for (String currGroup : groups.get("groupsToAssign")) {
			addGroupToGroupsToAdd(currGroup);
		}
		
		for (String currGroup : groups.get("groupsToRevoke")) {
			addGroupToGroupsToRemove(currGroup);
		}
	}
	
	public void importAttributesToModify(Resource resource, ModifyRequest modifyRequest) {
		Map<String,List<String>> attrs = SpmlUtils.getAttributesToModify(modifyRequest, null);
		
		for (Map.Entry<String,List<String>> currEntry : attrs.entrySet()) {
			addAttribute(currEntry.getKey(),currEntry.getValue());
		}
	}
	
	
	public void importResourceConfParams(ResourceDescriptor resourceDescriptor) {
		//TODO: Add adapters conf attributes too
		log.debug("Importing resource configuration parameters...");
		for (Map.Entry<String,String> entry : resourceDescriptor.getAllTags("specific").entrySet()) {
			log.trace("Importing resource param: '" + entry.getKey() + "', value: '" + entry.getValue() + "'");
			addResourceConfParam(entry.getKey(), entry.getValue());
		}
	}
	
	/*
	public VeloDataContainer factoryVeloDataContainer() {
		getInstance().setResourceConfParams(factoryVDC.createVeloDataContainerResourceConfParams(resourceConfParams));
		getInstance().setAttributes(factoryVDC.createVeloDataContainerAttributes(attributes));
		
		return getInstance();
	}
	*/
	
	public void addResourceAction(velo.entity.ResourceAction resourceAction) {
		org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory factory = new org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory();
		ResourceAction gwRA = factory.createResourceAction();

		gwRA.setActive(resourceAction.isActive());
		gwRA.setActionCode(resourceAction.getContent());
		gwRA.setPhase(InvokePhases.fromValue(resourceAction.getInvokePhase().name()));
		gwRA.setShowStopper(resourceAction.isShowStopper());
		gwRA.setSequence(resourceAction.getSequence());
		
		resourceActions.getResourceActions().getResourceAction().add(gwRA);
	}
	
	public void importDataFromResourceOperation(ResourceOperation ro) {
		for (velo.entity.ResourceAction currAction : ro.getResource().getResourceActions()) {
			
		}
	}
	
	
	public void setAccountName(String accountName) {
		org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory factory = new org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory();
		getInstance().setAccountName(factory.createVeloDataContainerAccountName(accountName));
	}
	
	public void setPassword(String password) {
		org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory factory = new org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory();
		getInstance().setPassword(factory.createVeloDataContainerPassword(password));
	}
	
	public void setResourceUniqueName(String uniqueName) {
		org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory factory = new org.datacontract.schemas._2004._07.velo_wingw.ObjectFactory();
		getInstance().setResourceUniqueName(factory.createVeloDataContainerResourceUniqueName(uniqueName));
	}
	
	//public void constructVeloDataContainer(Request request) throws ObjectsConstructionException {
	public void importDataFromRequest(Resource resource, Request request ) throws ObjectsConstructionException {
		if (request instanceof SuspendRequest) {
			log.debug("Importing data from SPML Suspend Request.");
			SuspendRequest suspendRequest = (SuspendRequest)request; 
			
			log.debug("Account name: '" + suspendRequest.getPsoID().getID() + "'");
			log.debug("Resource unique name: '" + suspendRequest.getPsoID().getTargetID() + "'");
			
			setResourceUniqueName(suspendRequest.getPsoID().getTargetID());
			setAccountName(suspendRequest.getPsoID().getID());
		}
		else if (request instanceof ResumeRequest) {
			log.debug("Importing data from SPML Resume Request.");
			ResumeRequest resumeRequest = (ResumeRequest)request;
			
			setResourceUniqueName(resumeRequest.getPsoID().getTargetID());
			setAccountName(resumeRequest.getPsoID().getID());
		}
		
		else if (request instanceof DeleteRequest) {
			log.debug("Importing data from SPML Delete Request.");
			DeleteRequest deleteRequest = (DeleteRequest)request;
			
			setResourceUniqueName(deleteRequest.getPsoID().getTargetID());
			setAccountName(deleteRequest.getPsoID().getID());
		}
		else if (request instanceof AddRequest) {
			log.debug("Importing data from SPML Add Request.");
			AddRequest addRequest = (AddRequest)request;
			
			setResourceUniqueName(addRequest.getPsoID().getTargetID());
			importAttributes(resource, addRequest);
			importGroupsToAdd(resource, addRequest);
		}
		else if (request instanceof ModifyRequest) {
			log.debug("Importing data from SPML Modify Request.");
			ModifyRequest modifyRequest = (ModifyRequest)request;
			
			setResourceUniqueName(modifyRequest.getPsoID().getTargetID());
			setAccountName(modifyRequest.getPsoID().getID());
			
			log.debug("Modified account as represented in SPML is: '" + modifyRequest.getPsoID().getID() + "'");
			//TODO: Support account attributes modifications in the future(here? or in separated operation?)
			importGroupsForModifyAccount(resource, modifyRequest);
			
			importAttributesToModify(resource,modifyRequest);
		}
		else {
			throw new ObjectsConstructionException("The specified operation is not supported by VeloDataContainer construction method!");
		}
	}
	
	public static void main(String[] args) throws Exception {
		String content = FileUtils.getContents(new File("c:/1.txt"));
		
		File f = new File("c:/2.txt");
		f.createNewFile();
		FileUtils.setContents(f, content);
	}
}
