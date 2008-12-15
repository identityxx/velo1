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

package velo.importer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import velo.entity.Resource;
import velo.entity.Role;
import velo.entity.RoleResourceAttribute;
import velo.entity.RoleResourceAttributeAsTextual;
import velo.entity.RoleResourceAttributePK;




public class RoleCreationUnitList extends ArrayList<RoleCreationUnit> {
	
	public RoleCreationUnitList() {
    }
	
	
	
	
	public void parseImportedXmlFile (ByteArrayInputStream xmlFileContent) {
		
		System.out.println("Starting to parce imported xml file containing roles to be create based on resource attributes");
		//xml parsing logic
		try{
		
	       	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse(xmlFileContent);
	        NodeList itemList = doc.getElementsByTagName("role");
	        int totalItems = itemList.getLength();
	       
	     
	        System.out.println("Total roles: "+ totalItems);
	
	        int totalNodeChilds;
	       
	        for(int s=0; s<totalItems ; s++){
	        	RoleCreationUnit currRCU = new RoleCreationUnit();
	            Node node = itemList.item(s);
	                      
	            if(node.getNodeType() == Node.ELEMENT_NODE){
	            	NamedNodeMap attrs =  node.getAttributes();
	            	boolean isRoleToBeCreated  = (attrs.getNamedItem("create").getNodeValue().trim().equals("true") ? true : false);
	            	String roleName = attrs.getNamedItem("name").getNodeValue().trim();
	            	System.out.println("Role named : " + roleName);
	            	String resourceUniqueId = attrs.getNamedItem("resource").getNodeValue().trim();
	            	currRCU.setResourceUniqueId(resourceUniqueId);
	            	currRCU.setRoleName(roleName);
	            	currRCU.setToBeCreated(isRoleToBeCreated);
	            	
	               	NodeList n = ((Element)node).getElementsByTagName("resource-attribute");
	            	totalNodeChilds = n.getLength();
	            	
	            		for(int i=0; i<totalNodeChilds;i++){
	            			if(n.item(i).getNodeType() == Node.ELEMENT_NODE){
	            				NamedNodeMap subAttrs =  n.item(i).getAttributes();
	            				RoleResourceAttributeUnit rrau = new RoleResourceAttributeUnit();
	            				String resAttrName = subAttrs.getNamedItem("name").getNodeValue().trim();
	            				String roleResAttrValue = subAttrs.getNamedItem("value").getNodeValue().trim();
	            				String roleResAttrType = subAttrs.getNamedItem("type").getNodeValue().trim();
	            				boolean isRoleResAttrToBeCreated  = (subAttrs.getNamedItem("create").getNodeValue().trim().equals("true") ? true : false);
	            				System.out.println("Role resource attribute named = " + resAttrName + " with value " + roleResAttrValue + ", of type " + roleResAttrType );
	            				System.out.println("The object rrau is " + rrau.toString());
	            				rrau.setIsToBeCreated(isRoleResAttrToBeCreated);
	            				rrau.setResourceAttributeName(resAttrName);
	            				rrau.setRoleResourceAttributeValue(roleResAttrValue);
	            				rrau.setRoleResourceAttributeType(roleResAttrType);
	            				
	            				currRCU.addRoleResourceAttributeUnit(rrau);
	            				
	            			}
	            			 			
	            		}
	            		
	            		NodeList n_p = ((Element)node).getElementsByTagName("position");
		            	totalNodeChilds = n_p.getLength();
		            	
		            		for(int i=0; i<totalNodeChilds;i++){
		            			if(n_p.item(i).getNodeType() == Node.ELEMENT_NODE){
		            				NamedNodeMap subAttrs =  n_p.item(i).getAttributes();
		            				String positionUniqueId = subAttrs.getNamedItem("uniqueId").getNodeValue().trim();
		            				String positionDisplayName = subAttrs.getNamedItem("displayName").getNodeValue().trim();
		            				System.out.println("Position with uniqueId = " + positionUniqueId + "named " + positionDisplayName+ " is found");
		            				currRCU.addPosition(positionUniqueId, positionDisplayName);
		            				
		            				
		            				
		            			}
		            			 			
		            		}
	            		this.add(currRCU);
	            }
	        }
	     }
	        
	    catch(Exception e)
		{
			System.out.println("Exception occured while parsing XML file:" + e.toString());
		}
	    
	    
	    
	}

	

	
	

}



