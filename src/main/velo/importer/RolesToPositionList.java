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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class RolesToPositionList extends ArrayList<RolesToPosition> {
	
	public RolesToPositionList() {
    }
	
	
	
	
	public void parseImportedXmlFile (ByteArrayInputStream xmlFileContent) {
		
		System.out.println("Starting to parce imported xml file containing roles to position associations");
		//xml parsing logic
		try{
		
	       	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse(xmlFileContent);
	        NodeList itemList = doc.getElementsByTagName("position");
	        int totalItems = itemList.getLength();
	       
	     
	        System.out.println("Total positions: "+ totalItems);
	
	        int totalNodeChilds;
	       
	        for(int s=0; s<totalItems ; s++){
	        	RolesToPosition currRTP = new RolesToPosition();
	            Node node = itemList.item(s);
	                      
	            if(node.getNodeType() == Node.ELEMENT_NODE){
	            	NamedNodeMap attrs =  node.getAttributes();
	            	String positionUniqueId = attrs.getNamedItem("uniqueId").getNodeValue().trim();
	            	System.out.println("Position uniqueId: " + positionUniqueId);
	            	currRTP.setPositionUniqueId(positionUniqueId);
	            	String positionDisplayName = attrs.getNamedItem("name").getNodeValue().trim();
	            	System.out.println("Position display name: " + positionDisplayName);
	            	currRTP.setPositionDisplayName(positionDisplayName);
	               	NodeList n = ((Element)node).getElementsByTagName("role");
	            	totalNodeChilds = n.getLength();
	            	
	            		for(int i=0; i<totalNodeChilds;i++){
	            			if(n.item(i).getNodeType() == Node.ELEMENT_NODE){
	            				NamedNodeMap subAttrs =  n.item(i).getAttributes();
	            				String roleName = subAttrs.getNamedItem("name").getNodeValue().trim();
	            				System.out.println("role name="+roleName);
	            				currRTP.addRole(roleName);
	            			}
	            			 			
	            		}
	            		this.add(currRTP);
	            }
	        }
	     }
	        
	    catch(Exception e)
		{
			System.out.println("Exception occured while parsing XML file:" + e.toString());
		}
	}


}



