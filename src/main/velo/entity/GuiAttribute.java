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
package velo.entity;
//@!@not
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import org.hibernate.validator.NotNull;

/**
 * A class that represents an attribute
 * 
 * @author Asaf Shakarchi
 */

@MappedSuperclass
public class GuiAttribute extends Attribute implements Serializable {
	private static final long serialVersionUID = 1L;
	
   private Integer displayPriority;
   
   private AttributeVisualRenderingType visualRenderingType;
   
   public enum AttributeVisualRenderingType {
		INPUT, SELECTBOX
	}
	
	public GuiAttribute() {
		
	}
	
	public GuiAttribute(String uniqueName, String displayName, String description, AttributeDataTypes dataType, boolean required, boolean managed, int minLength, int maxLength, int minValues, int maxValues) {
		super(uniqueName, displayName, description, dataType, required, managed, minLength, maxLength, minValues, maxValues);
	}

	
	/**
	 * @return the displayPriority
	 */
	@Column(name="DISPLAY_PRIORITY", nullable=false)
	@NotNull
	public Integer getDisplayPriority() {
		return displayPriority;
	}

	/**
	 * @param displayPriority the displayPriority to set
	 */
	public void setDisplayPriority(Integer displayPriority) {
		this.displayPriority = displayPriority;
	}
	
	
	/**
	 * @return the visualRenderingType
	 */
	@Column(name="VISUAL_RENDERING_TYPE", nullable=false)
	@Enumerated(EnumType.STRING)
	public AttributeVisualRenderingType getVisualRenderingType() {
		return visualRenderingType;
	}

	/**
	 * @param visualRenderingType the visualRenderingType to set
	 */
	public void setVisualRenderingType(AttributeVisualRenderingType visualRenderingType) {
		this.visualRenderingType = visualRenderingType;
	}
	
		
	
	//public void updateInputJsfComponent(UIInput uic) {
	public void updateInputJsfComponent() {
        //System.out.println("updateInputJsfComponent, Updating to 'Value' object converters: --BEFORE UPDATE--, FOR UserIdentityAttributeValue ID: " + this.getUserIdentityAttributeValueId() + ", CONVERTER: " + uic.getConverter() + ", VALIDATORS #: " + uic.getValidators().length);
        
        //Set the component as 'required' if the IA is set to required
//jb        if (isRequired()) {
            //jbuic.setRequired(true);
        //jb}
        
        /*
        //Handle Validators
        //Add validators
        switch(getDataType()) {
			case INTEGER: {
				DoubleRangeValidator dbr = new DoubleRangeValidator();
				dbr.setMaximum(getMaxLength()); 
				uic.addValidator(dbr);
			}
			case STRING: {
				LengthValidator lv = new LengthValidator();
				lv.setMaximum(getMaxLength());
				uic.addValidator(lv);
			}
			//case BOOLEAN:;
			case LONG: {
				DoubleRangeValidator dbr = new DoubleRangeValidator();
				dbr.setMaximum(getMaxLength());
				dbr.setMinimum(getMinLength());
				uic.addValidator(dbr);
			}
			//case DATE:
			default: //TODO: Log this, should never get here
        }
        */
        
        
        //Add a string converter per child
        /*JB!
        if (getJsfConverter() != null) {
            uic.setConverter(getJsfConverter());
        }
        */
        
        //System.out.println("updateInputJsfComponent: --END of Update--, For UserIdentityAttributeValue ID: " + this.getUserIdentityAttributeValueId() + ", CONVERTERS: " + uic.getConverter() + ", VALIDATORS #: " + uic.getValidators().length);
        
        //return uic;
    }
    
    
	/*
    //TODO: Implement
    @Transient
    public Collection<Validator> getJsfValidators() {
    	return null;//JB !!!!
    }
    */
    
    /*
    //TODO: Implement
    @Transient
    public Converter getJsfConverter() {
    	switch(getDataType()) {
		case INTEGER: {
			IntegerConverter ic = new IntegerConverter();
			return ic;
		}
		case STRING: {
			//TODO Needs a converter?
			return null;
		}
		case BOOLEAN: {
			BooleanConverter ic = new BooleanConverter();
			return ic;
		}
		case LONG: {
			LongConverter ic = new LongConverter();
			return ic;
		}
		case DATE: {
			DateTimeConverter ic = new DateTimeConverter();
			return ic;
		}
		default: {
			//TODO: Log this, should never get here
			return null;
		}
    }
    */
    //}
    
}