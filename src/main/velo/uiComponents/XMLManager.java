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
package velo.uiComponents;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;

import velo.xml.XMLConfManager;

public class XMLManager extends UIComponentBase {

	private String type;
	private String xmlContent;
    private String filename;
    XMLConfManager config;
    Set<XMLTagInput> tags = new HashSet<XMLTagInput>();

    public XMLManager() {
    }


    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        try {
            setConfig(getConfig());
        } catch (ConfigurationException ex) {
        	/*
        	//ValueExpression 
        	for (Object curr : getAttributes().keySet()) {
        		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!" + curr);
        	}
        	*/
            throw new IOException("Cannot import XML file named '" + getFilename() + "', " + ex.toString());
        }
        

        return;
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.tags = (Set<XMLTagInput>) values[1];
        this.filename = (String) values[2];
        this.type = (String)values[3];
    }

    /*Does not work as Common Confiugration is not serializable
     * Throws an java.io.NotSerializableException: org.apache.commons.configuration.XMLConfiguration$XMLFileConfigurationDelegate exception*/
    public Object saveState(FacesContext context) {
        Object[] values = new Object[4];
        values[0] = super.saveState(context);
        values[1] = tags;
        values[2] = this.filename;
        values[3] = this.type;
        return values;
    }

    public String getFilename() {
    	if (this.filename != null) return this.filename; 
    	ValueExpression vb = this.getValueExpression("filename"); 
    	if (vb != null) return (String) vb.getValue(FacesContext.getCurrentInstance().getELContext());
    	return null; 
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public XMLConfManager getConfig() throws ConfigurationException {
        if (config == null) {
            //out.println("Config is null, initializing a new Config object with the specified file named \'" + getFilename() + "\'");
        	
        	if (getType().equalsIgnoreCase("file")) {
        		try {
        			config = new velo.xml.XMLConfManager();
        			config.init(getFilename());
        			return config;
        		} catch (ConfigurationException ex) {
        			throw ex;
        		}
        	}
        	else if (getType().equalsIgnoreCase("content")) {
        		try {
        			config = new velo.xml.XMLConfManager();
        			config.initViaContent(getXmlContent());
        			return config;
        		} catch (ConfigurationException ex) {
        			throw ex;
        		}
        	}
        	else {
        		//is not supported
        		//TODO: audit log/indicate an error
        		return null;
        	}
        } else {
            return config;
        }
    }

    public Set<XMLTagInput> getTags() {
        return tags;
    }

    public void setTags(Set<XMLTagInput> tags) {
        this.tags = tags;
    }

    public void setConfig(XMLConfManager config) {
        this.config = config;
    }

    public String getFamily() {
        return "velo.xmlManager";
    }
    
    
    public void saveDataByFile() throws ConfigurationException{
    	//XMLConfManager xmlConfManager = xmlManager.getConfig();
		for (velo.uiComponents.XMLTagInput currTag : getTags()) {
			//log.debug("TAG Object ID: " + currTag);
			// store the tags
			//log.debug("!!!VALUE!!!: " + currTag.getTagValue());
			getConfig().getConfig().setProperty(currTag.getTagName(), escapeTagValue(currTag.getTagValue()));
		}

		getConfig().getConfig().save();
    }
    
    public String saveDataByContent() throws ConfigurationException {
    	//XMLConfManager xmlConfManager = xmlManager.getConfig();
		for (velo.uiComponents.XMLTagInput currTag : getTags()) {
			//log.debug("TAG Object ID: " + currTag);
			// store the tags
			//log.debug("!!!VALUE!!!: " + currTag.getTagValue());
			getConfig().getConfig().setProperty(currTag.getTagName(), escapeTagValue(currTag.getTagValue()));
		}

		StringWriter sw = new StringWriter();
		getConfig().getConfig().save(sw);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!: " + sw);
		return sw.getBuffer().toString();
    }
    
    public String escapeTagValue(String value) {
    	//return value.replace(",", "\\,");
    	return value;
    }


    public String getXmlContent() {
        if (this.xmlContent != null) {
            return this.xmlContent;
        }

        ValueExpression ve = getValueExpression("xmlContent");
        if (ve != null) {
            try {
                return (String) ve.getValue(getFacesContext().getELContext());
            } catch (ELException e) {
                throw new FacesException(e);
            }
        } else {
            return null;
        }
    }

    


	/**
	 * @param xmlContent the xmlContent to set
	 */
	public void setXmlContent(String xmlContent) {
		this.xmlContent = xmlContent;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	


	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
