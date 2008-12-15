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
package velo.attributeValidators;

import velo.entity.IdentityAttribute;
import velo.storage.Attribute;

public abstract class IdentityAttributeValidator implements AttributeValidatorInterface {
    
    private IdentityAttribute identityAttribute;
    private Attribute inputAttribute;
    private Attribute outputAttribute;
    //private UserIdentityAttribute inputAttribute;
    //private UserIdentityAttribute outputAttribute;
    
    
    public IdentityAttributeValidator() {
        
    }
    public IdentityAttributeValidator(IdentityAttribute ia, Attribute ta) {
        setIA(ia);
        setInputAttribute(ta);
    }
    
    public IdentityAttribute getIA() {
        return identityAttribute;
    }
    public IdentityAttribute getIdentityAttribute() {
        return identityAttribute;
    }
    
    public void setIdentityAttribute(IdentityAttribute identityAttribute) {
        this.identityAttribute = identityAttribute;
    }
    public void setIA(IdentityAttribute identityAttribute) {
        this.identityAttribute = identityAttribute;
    }
    /**
     @param inputAttribute the inputAttribute to set
     */
    public void setInputAttribute(Attribute inputAttribute) {
        this.inputAttribute = inputAttribute;
    }
    
    
    public Attribute getOutputAttribute() {
        return outputAttribute;
    }
    
    public void setOutputAttribute(Attribute outputAttribute) {
        this.outputAttribute = outputAttribute;
    }
    
    
    /**
     @return the inputAttribute
     */
    public Attribute getInputAttribute() {
        return inputAttribute;
    }
    
    public void __modify__() {
        setOutputAttribute(getInputAttribute());
        modify();
    }
    
    public boolean isRequired() {
        return false;
    }
    
    public void modify() {
        
    }
        
}
