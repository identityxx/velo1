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
package velo.resource.resourceDescriptor;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 * @author Asaf Shakarchi
 * 
 * A class that represents a Target-System Descriptor
 * Descriptor is the configuration parameters of a certain Target-System represnted as a class
 */
public class ResourceDescriptor implements Serializable {
	private static Logger log = Logger.getLogger(ResourceDescriptor.class.getName());
	private static String rootTag = "resource-descriptor";
	private XMLConfiguration config = new XMLConfiguration();
	
	
	
	public void loadResourceXmlConfiguration(String xml) throws ConfigurationException{
		try {
			Reader reader = new StringReader(xml);
			//xmlConf.setDelimiterParsingDisabled(true);
            //xmlConf.setListDelimiter("|".charAt(0));
            //xmlConf.setDelimiter("|".charAt(0));
			config.load(reader);
			
		}catch (ConfigurationException e) {
			log.error("Could not load resource descriptor via xml data: " + e.getMessage() + "");
		}
	}
	
	/**
	 * @param config the config to set
	 */
	public void setConfig(XMLConfiguration config) {
		this.config = config;
	}
	
	public XMLConfiguration getConfig() throws ConfigurationException {
		if (config != null) {
			return config;
		} else {
			throw new ConfigurationException("Cannot return configuration, load configuration via XML first!");
		}
	}
	
	
	public String getString(String key) {
		try {
			log.trace("Getting string key for: " + getTag(key));
			
			return getConfig().getString(getTag(key));
		}catch (ConfigurationException e) {
			log.error("Could not get key: " + getTag(key));
			return null;
		}
	}
	
	public Integer getInt(String key) {
		try {
			return getConfig().getInt(getTag(key));
		}catch (ConfigurationException e) {
			return null;
		}
	}
	
	public Boolean getBoolean(String key) {
		try {
			return getConfig().getBoolean(getTag(key));
		}catch (ConfigurationException e) {
			log.error("Could not get key: " + getTag(key));
			return null;
		}
	}
	
	public String[] getStringArray(String key) {
		try {
			return getConfig().getStringArray(getTag(key));
		}catch (ConfigurationException e) {
			log.error("Could not get key: " + getTag(key));
			return null;
		}
	}
	
	//Does not support recursive tags!
	public Map<String,String> getAllTags(String key) {
		try {
			Map<String,String> map = new HashMap<String,String>();
			SubnodeConfiguration specificNodes = getConfig().configurationAt(key);
			Iterator nodes = specificNodes.getRootNode().getChildren().listIterator();
			while (nodes.hasNext()) {
				XMLConfiguration.Node currNode = (XMLConfiguration.Node) nodes.next();
				if (currNode.getChildren().size()<1) {
					map.put(currNode.getName(), currNode.getValue().toString());
				}
			}
			
			return map;
		}catch (ConfigurationException e) {
			log.error("Could not get key: " + getTag(key));
			return null;
		}
	}
	
	
	
	
	private String getTag(String key) {
		//return rootTag+"."+key;
		return key;
	}
	
	
	
	
	
	
	
	
	
	
	
	/*
	@Deprecated
	private ResourceDescriptorAttributes resourceDescriptorAttributes;
	@Deprecated
	private ResourceDescriptorAdapter resourceDescriptorAdapter;
	@Deprecated
	private ResourceDescriptorSpecificAttributes resourceSpecificAttributes;
	@Deprecated
	private Map specificAttributes = new HashMap();
	*/
	  
	/**
	 * Constructor
	 */
	public ResourceDescriptor() {
		
	}
	
	/**
	 * 
	 * @param resourceDescriptorAttributes The resourceDescriptorAttributes object to set.
	 */
	/*
	@Deprecated
	public void setResourceDescriptorAttributes(ResourceDescriptorAttributes resourceDescriptorAttributes) {
		this.resourceDescriptorAttributes = resourceDescriptorAttributes;
	}
	*/

	/**
	 * @return Returns the resourceAttributes.
	 */
	/*
	@Deprecated
	public ResourceDescriptorAttributes getResourceDescriptorAttributes() {
		return resourceDescriptorAttributes;
	}
	*/
	
	/**
	 * @param resourceDescriptorAdapter The resourceAdapter to set.
	 */
	/*
	@Deprecated
	public void setResourceDescriptorAdapter(ResourceDescriptorAdapter resourceDescriptorAdapter) {
		this.resourceDescriptorAdapter = resourceDescriptorAdapter;
	}
	*/

	/**
	 * @return Returns the resourceAdapter.
	 */
	/*
	@Deprecated
	public ResourceDescriptorAdapter getResourceDescriptorAdapter() {
		return resourceDescriptorAdapter;
	}
	*/

	/**
	 * @param resourceSpecificAttributes The resourceSpecificAttributes to set.
	 */
	/*
	@Deprecated
	public void setResourceSpecificAttributes(ResourceDescriptorSpecificAttributes resourceSpecificAttributes) {
		this.resourceSpecificAttributes = resourceSpecificAttributes;
	}
	*/

	/**
	 * @return Returns the resourceSpecificAttributes.
	 */
	/*
	@Deprecated
	public ResourceDescriptorSpecificAttributes getResourceSpecificAttributes() {
		return resourceSpecificAttributes;
	}
	*/

	/**
	 * @param specificAttributes The specificAttributes to set.
	 */
	/*
	@Deprecated
	public void setSpecificAttributes(Map<String,String> specificAttributes) {
		this.specificAttributes = specificAttributes;
	}
	*/

	/**
	 * @return Returns the specificAttributes.
	 */
	/*
	@Deprecated
	public Map<String,String> getSpecificAttributes() {
		return specificAttributes;
	}
	*/

}
