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
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class XMLTagInput extends HtmlPanelGrid implements Serializable {

    private String tagName;
    private String forVal;
    private String tagFullPath;
    private String tagValue;
    private static Logger logger = Logger.getLogger(XMLTagInput.class.getName());

    
    public XMLTagInput() {
    }

    public XMLTagInput(String tagName, String forVal) {
        this.tagName = tagName;
        this.forVal = forVal;
    }

    public void encodeBegin(FacesContext context) throws IOException {
    	//System.out.println("START: EncodeBegin of XMLTagInput");	
        //System.out.println("Encoding XMLTagInput component with tag named: " + tagName);
        //System.out.println("Component is relevant to XMLManager ID: " + getFor());
        //UiXMLManager is not not always the parent
        //XMLManager xmlManager = (XMLManager)getParent();
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        UIComponent uiComponent = view.findComponent(getFor());

        velo.uiComponents.XMLManager xmlManager = null;
        if (uiComponent instanceof XMLManager) {
            xmlManager = (velo.uiComponents.XMLManager) uiComponent;
        } else {
            //System.out.println("Expected UI Component is not an instance of XMLManager class!");
            //get out!
        }

        try {
            //Only do something if this component has no children (otherwise it means that its state restored from previous run)
            if (getChildCount() == 0) {
                //System.out.println("Loaded component that expected to be an XMLManager of class \'" + uiComponent.getClass().getName() + "\'");

                //System.out.println("xml conf: " + xmlManager.getConfig());
                logger.debug("Initing NODE for tag named: " + getTagName());
                initForNode(xmlManager.getConfig().getConfig().configurationAt(getTagName()).getRoot());

                //register this XmlTag within the manager
                xmlManager.getTags().add(this);
                logger.debug("Registered tag to Manager with ID:" + this);
                super.encodeBegin(context);
            } else {
                //System.out.println("XMLTAGINPUT->ENCODEBEGIN: DO NOTHING, THE CHILDREN GOT RESTEREOD FROM PREVIOUS RUN");
                //only update value
                //updateValue(xmlManager.getConfig().getConfig().configurationAt(getTagName()).getRoot());

                
                //updateValue();
                
                //Update the tag in the xmlManager as XmlTags are not passed by reference for some reason and new objects are created!
                //TODO: Make it cleaner, this loop is ugly
                /*
                for (XMLTagInput currTag : xmlManager.getTags()) {
                	if (currTag.getTagName().equals(getTagName())) {
                		System.out.println("Tag in XMLManager was found!, updating its value...");
                		currTag.setTagValue(getTagValue());
                	}
                }
                */
                
                super.encodeBegin(context);
                //do nothing, the children got restored from previous state
            }
        } catch (ConfigurationException ex) {
            //System.out.println("SHIT! todo, take care of this!");
        }
        
        //System.out.println("END: EncodeBegin of XMLTagInput");
    }

    //Second method after (restoreState) tha gets invoked when the form gets submitted
    public void decode(FacesContext context) {
    	//Set the submitted value!
    	updateSubmittedValue();
    	//the method only updates the tag itself, but for some reason it is a must to update the tag inside the Manager's SET, since these are different objects :-/
    	javax.faces.component.UIViewRoot view = javax.faces.context.FacesContext
		.getCurrentInstance().getViewRoot();
    	UIComponent uiComponent = view.findComponent(getFor());
    	
    	if (uiComponent instanceof XMLManager) {
    		XMLManager xmlManager = (velo.uiComponents.XMLManager) uiComponent;
    		
    		for (XMLTagInput currTag : xmlManager.getTags()) {
            	if (currTag.getTagName().equals(getTagName())) {
            		logger.debug("Tag in XMLManager was found!, updating its value...");
            		currTag.setTagValue(getTagValue());
            	}
            }
        } else {
            //throw new Exception("Expected UI Component is not an instance of XMLManager class!");
        	logger.debug("ERROR: " + "Expected UI Component is not an instance of XMLManager class!");
        }
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }


    /**
     * <p>Return the client identifier of the component for which
     * this component represents associated message(s) (if any).</p>
     */
    public String getFor() {
        if (this.forVal != null) {
            return this.forVal;
        }

        ValueExpression ve = getValueExpression("for");
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
     * <p>Set the client identifier of the component for which this
     * component represents associated message(s) (if any).  This
     * property must be set before the message is displayed.</p>
     *
     * @param newFor The new client id
     */
    public void setFor(String newFor) {
        forVal = newFor;
    }



    public void initForNode(XMLConfiguration.Node node) {
        this.setColumns(3);
        this.setStyleClass("xmlTagPanel");
        this.setHeaderClass("xmlTagPanel");
        this.setColumnClasses("xmlTagReq,xmlTagLabel,xmlTagInput");


        //XmlPanelGrid hpg = new XmlPanelGrid();
        //this.setColumns(3);
        //this.setStyleClass("xmlTagPanel");
        //this.setHeaderClass("xmlTagPanel");
        //this.setColumnClasses("xmlTagReq,xmlTagLabel,xmlTagInput");
        HtmlInputText inputTag = (HtmlInputText) this.getFacesContext()
                    .getApplication().createComponent(HtmlInputText.COMPONENT_TYPE);
        inputTag.setSize(60);
        HtmlOutputText labelTag = (HtmlOutputText) this.getFacesContext()
                    .getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);

        HtmlOutputText reqTag = (HtmlOutputText) this.getFacesContext()
                    .getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
        reqTag.setValue(" ");

        inputTag.setValue(node.getValue());
        setTagValue((String) node.getValue());
        List attrList = node.getAttributes();
        Iterator attrListIt = attrList.iterator();
        while (attrListIt.hasNext()) {
            XMLConfiguration.Node attr = (XMLConfiguration.Node) attrListIt.next();
            if (attr.getName().equalsIgnoreCase("desc")) {
                labelTag.setValue(attr.getValue());
            }

            if (attr.getName().equalsIgnoreCase("req")) {
                if (attr.getValue().toString().equalsIgnoreCase("true")) {
                    inputTag.setRequired(true);
                    reqTag.setValue("*");
                }
            }
        }

        this.setTagFullPath(getFullXmlTagPath(node));
        this.getChildren().add(reqTag);
        this.getChildren().add(labelTag);
        this.getChildren().add(inputTag);
    }

    /*
    public void updateValue() {
        for (UIComponent currComp : getChildren()) {
            if (currComp instanceof HtmlInputText) {
                HtmlInputText input = (HtmlInputText) currComp;
                System.out.println("Updating TAG("+this+") value to: " + input.getValue());
                setTagValue((String)input.getValue());
                
            }
        }
    }
    */
    
    
    public void updateSubmittedValue() {
        for (UIComponent currComp : getChildren()) {
            if (currComp instanceof HtmlInputText) {
                HtmlInputText input = (HtmlInputText) currComp;
                System.out.println("Updating Submitted value into input TAG("+this+") value to: " + input.getSubmittedValue());
                setTagValue((String)input.getSubmittedValue());
            }
        }
    }

    public static String getFullXmlTagPath(XMLConfiguration.Node node) {
        String tagPath = new String();

        int i = 0;
        while (node.getParent().getName() != null) {
            XMLConfiguration.Node currNode = node.getParent();

            if (i > 0) {
                //tagPath += "." + currNode.getName();
                tagPath = currNode.getName() + "." + tagPath;
                node = currNode;
            } else {
                tagPath += node.getName();
            }

            i++;
        }

        //System.out.println("FULL TAG NAME OF NODE '" + node.getName() + "' is: " + tagPath + "'");
        return tagPath;
    }


    public String getTagFullPath() {
        return tagFullPath;
    }

    public void setTagFullPath(String tagFullPath) {
        this.tagFullPath = tagFullPath;
    }


    ////STATE MANAGEMENT
    private Object[] values;

    public Object saveState(FacesContext context) {
        if (values == null) {
            //values = new Object[5];
        	values = new Object[4];
        }
        values[0] = super.saveState(context);
        values[1] = this.forVal;
        values[2] = this.tagFullPath;
        values[3] = this.tagName;
        //values[4] = getValue();
        //values[4] = getTagValue();
        //System.out.println("^^^^^^^^^^^^^SAVING STATE: " + getTagValue());
        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        values = (Object[]) state;
        super.restoreState(context, values[0]);
        forVal = (String) values[1];
        tagFullPath = (String) values[2];
        tagName = (String) values[3];
        //tagValue = (String) values[4];
        //System.out.println("^^^^^^^^^^^^^RESTORING STATE: " + tagValue);
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    public String getValue() {
        for (UIComponent currComp : getChildren()) {
            if (currComp instanceof HtmlInputText) {
                HtmlInputText currInput = (HtmlInputText) currComp;
                return (String) currInput.getValue();
            }
        }

        return null;
    }
}