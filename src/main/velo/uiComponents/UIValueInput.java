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

import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import velo.entity.UserIdentityAttributeValue;
import velo.entity.GuiAttribute.AttributeVisualRenderingType;
import velo.entity.IdentityAttribute.IdentityAttributeSources;

public class UIValueInput extends UIInput {
//public class UIValueInput extends UIInput {
	private static Logger log = Logger.getLogger(UIValueInput.class.getName());
	
    public UIValueInput() {
        super();
    }
    
    public String getRendererType() {
        // No renderer, component renders itself.
        return null;
    }
    
    public void decode(FacesContext context) {
        // Proxy up submitted value from child component
    	log.trace("Decode: SUBMITTED VALUE IS: " + getChildComponent().getSubmittedValue());

    	//Object value = getConvertedValue(context, getChildComponent().getSubmittedValue());
    	
    	/*
    	Converter converter = getChildComponent().getConverter();
    	Object val = null;
    	if (converter != null) {
    		val = converter.getAsObject(context, getChildComponent(), (String)getChildComponent().getSubmittedValue());
    	} else {
    		val = getChildComponent().getSubmittedValue();
    	}
    	*/
    	
    	//Should pass converter!
        setSubmittedValue(getChildComponent().getSubmittedValue());
    	//log.trace("Submitted value object type: " + val.getClass().getName());
    	//setSubmittedValue(val);
    }
    
    
    public void encodeBegin(FacesContext context) throws IOException {
        log.trace("EncodeBegin started for UserIdentityAttributeValue: " + this.getId());
        
        UserIdentityAttributeValue uiav = (UserIdentityAttributeValue) getAttributes().get("valueObject");
        

        if (this.getChildCount() < 1) {
            // Only add child component if it's not already in the view tree
            
            if (uiav.getUserIdentityAttribute().getIdentityAttribute().getVisualRenderingType() == AttributeVisualRenderingType.SELECTBOX) {
                //HtmlSelectOneListbox child = (HtmlSelectOneListbox) this.getFacesContext()
            	
            	HtmlSelectOneListbox childSelectOneListBox = (HtmlSelectOneListbox) this.getFacesContext()
                .getApplication().createComponent(
                HtmlSelectOneListbox.COMPONENT_TYPE);

            	
            	childSelectOneListBox.setId(this.getId() + "_child");

                //System.out.println("Adding children component...with ID: " + child.getId());
                //uiav.getUserIdentityAttribute().getIdentityAttribute().updateInputJsfComponent(child);
                
            	if (uiav.getUserIdentityAttribute().getIdentityAttribute().getSource() != IdentityAttributeSources.LOCAL) {
            		childSelectOneListBox.setDisabled(true);
            	}
            	
                this.getChildren().add(childSelectOneListBox);
                
            } else if(uiav.getUserIdentityAttribute().getIdentityAttribute().getVisualRenderingType() == AttributeVisualRenderingType.INPUT) {
                //HtmlInputText child = (HtmlInputText) this.getFacesContext().getApplication().createComponent(HtmlInputText.COMPONENT_TYPE);
            	HtmlInputText childInputText = (HtmlInputText) this.getFacesContext().getApplication().createComponent(HtmlInputText.COMPONENT_TYPE);
            	childInputText.setId(this.getId() + "_child1");

            	
            	if (uiav.getUserIdentityAttribute().getIdentityAttribute().getSource() != IdentityAttributeSources.LOCAL) {
            		childInputText.setDisabled(true);
            	}
            	
            	this.getChildren().add(childInputText);

            }
            
            
            /*
            Converter converter = uiav.getUserIdentityAttribute().getIdentityAttribute().getJsfConverter();
            if (converter == null) {
            	log.trace("Resulted NO converter!");
            } else {
            	log.trace("Converter class is : " + converter.getClass().getName());
            }
            
            child.setConverter(converter);
            */
            
            
            //this.getChildren().add(child);
            //TODO: Add validators!
            //log.trace("Child validators: " + child.getValidators());
        }
        
        getChildComponent().setValue(this.getValue());
        
        
        super.encodeBegin(context);
    }

    /*
    public void encodeBegin(FacesContext context) throws IOException {
    	UserIdentityAttributeValue uiav = (UserIdentityAttributeValue) getAttributes().get("valueObject");
    	//ExpressionFactory ef = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
    	//ValueExpression valueExp = ef.createValueExpression(getFacesContext().getELContext(),"#{value.value}",uiav.getClass()); 
    	//setValueExpression("value", valueExp);
    	
    	ValueExpression vExp = getValueExpression("value");
    	System.out.println("!!!!!!!!!!!!!!!!: " + vExp.getExpressionString();
    	System.out.println("!!!!!!!!!!!!!!!!: " + getValue());
    	System.out.println("!!!!!!!!!!!!!!!!: " + getValue());
    	System.out.println("!!!!!!!!!!!!!!!!: " + getValue());
    	
    	
    	log.trace("EncodeBegin started for UserIdentityAttributeValue: " + this.getId());
        UserIdentityAttributeValue uiav = (UserIdentityAttributeValue) getAttributes().get("valueObject");
        Converter converter = uiav.getUserIdentityAttribute().getIdentityAttribute().getJsfConverter();
        
        setConverter(converter);
        if (converter == null) {
        	log.trace("Resulted NO converter!");
        } else {
        	log.trace("Converter class is : " + converter.getClass().getName());
        }
    }
    */
    
    //Currently supporting only one child.
    private UIInput getChildComponent() {
        return (UIInput) this.getChildren().get(0);
    }
    
    
    
}

