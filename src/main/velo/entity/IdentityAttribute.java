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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

import velo.attributeValidators.AttributeValidatorInterface;
import velo.entity.IdentityAttributeSourceByResourceAttribute.SyncByResourceAttributePolicy;
import velo.exceptions.AttributeValidationException;
import velo.exceptions.ScriptLoadingException;
import velo.scripting.ScriptFactory;
//@!@not
/**
 An entity that represents a User Identity-Attribute
 
 @author Asaf Shakarchi
 */
//Seam annotations
@Name("identityAttribute")

@Table(name="VL_IDENTITY_ATTRIBUTE")
@Entity
@SequenceGenerator(name="IdentityAttributeIdSeq",sequenceName="IDENTITY_ATTRIBUTE_ID_SEQ")
@NamedQueries({
	@NamedQuery(name = "identityAttribute.findByUniqueName",query = "SELECT object(ia) FROM IdentityAttribute AS ia WHERE ia.uniqueName = :uniqueName"),
	@NamedQuery(name = "identityAttribute.findAllToSync", query = "SELECT ia FROM IdentityAttribute ia WHERE ia.synchronize = 1"),
	@NamedQuery(name = "identityAttribute.findManager", query = "SELECT ia FROM IdentityAttribute ia WHERE ia.manager = 1"),
	@NamedQuery(name = "identityAttribute.findIdentifier", query = "SELECT ia FROM IdentityAttribute ia WHERE ia.identifier = 1"),
	
	
	
	
    
    //@NamedQuery(name = "findIdentityAttributesAttachedToresourceAttribute", query = "SELECT ia FROM IdentityAttribute AS ia,IdentityAttributeSourceByresourceAttribute as iasbtsa WHERE iasbtsa.resourceAttribute = :resourceAttribute"),
    @NamedQuery(name = "identityAttribute.findAll", query = "SELECT object(identityAttribute) FROM IdentityAttribute identityAttribute ORDER BY identityAttribute.displayPriority"),
    @NamedQuery(name = "identityAttribute.findAllActive", query = "SELECT object(identityAttribute) FROM IdentityAttribute identityAttribute ORDER BY identityAttribute.displayPriority"),
    @NamedQuery(name = "identityAttribute.findNotAssignedToGroups", query = "SELECT object(identityAttribute) FROM IdentityAttribute identityAttribute WHERE identityAttribute.identityAttributesGroup IS NULL"),
    @NamedQuery(name = "identityAttribute.searchByString", query = "SELECT object(identityAttribute) from IdentityAttribute identityAttribute WHERE ( (identityAttribute.uniqueName like :searchString) OR (identityAttribute.displayName like :searchString) )"),
    @NamedQuery(name = "identityAttribute.isExistByUniqueName", query = "SELECT count(ia) FROM IdentityAttribute AS ia WHERE ia.uniqueName = :uniqueName")
})
    public class IdentityAttribute extends GuiAttribute implements Serializable,Cloneable {
    
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     The ID of the Identity Attribute
     */
    private Long identityAttributeId;
    
    private IdentityAttributesGroup identityAttributesGroup;
    
    private boolean visibleInRequest;
    
    private boolean requiredInRequest;
    
    private boolean visibleInUserList;
    
    private boolean synchronize;
    
    private boolean manager;
    
    private boolean identifier;
    
    private Set<UserIdentityAttribute> userIdentityAttributes;
    
    /**
     The sources of this IA.
     */
    private Set<IdentityAttributeSource> sources = new HashSet<IdentityAttributeSource>();
    
    /**
     An identity attribute ID to set
     @param identityAttributeId The ID of the IdentityAttribute to set
     */
    public void setIdentityAttributeId(Long identityAttributeId) {
        this.identityAttributeId = identityAttributeId;
    }
    
    /**
     Get the IdentityAtribute ID
     @return An ID number of the IdentityAtribute entity
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_IDENTITY_ATTRIBUTE_GEN",sequenceName="IDM_IDENTITY_ATTRIBUTE_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_IDENTITY_ATTRIBUTE_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="IdentityAttributeIdSeq")
    //@GeneratedValue //JB
    @Column(name="IDENTITY_ATTRIBUTE_ID")
    public Long getIdentityAttributeId() {
        return identityAttributeId;
    }
    
    /**
     @param identityAttributesGroup the identityAttributesGroup to set
     */
    public void setIdentityAttributesGroup(IdentityAttributesGroup identityAttributesGroup) {
        this.identityAttributesGroup = identityAttributesGroup;
    }
    
    /**
     @return the identityAttributesGroup
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="IDENTITY_ATTRIBUTES_GROUP_ID", nullable=true, unique=false)
    public IdentityAttributesGroup getIdentityAttributesGroup() {
        return identityAttributesGroup;
    }
    
    
    /**
     @param sources The sources to set.
     */
    public void setSources(Set<IdentityAttributeSource> sources) {
        this.sources = sources;
    }
    
    /**
     @return Returns the sources.
     */
    //@OneToMany(mappedBy = "identityAttribute", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE,CascadeType.REFRESH,CascadeType.REMOVE})
    @OneToMany(mappedBy = "identityAttribute", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<IdentityAttributeSource> getSources() {
        return sources;
    }
    
    
    /**
     @param visibleInRequest the visibleInRequest to set
     */
    public void setVisibleInRequest(boolean visibleInRequest) {
        this.visibleInRequest = visibleInRequest;
    }
    
    /**
     @return the isVisibleInRequest
     */
    @Column(name="VISIBLE_IN_REQUEST", nullable=false)
    public boolean isVisibleInRequest() {
        return visibleInRequest;
    }
    
    /**
     @param requiredInRequest the requiredInRequest to set
     */
    public void setRequiredInRequest(boolean requiredInRequest) {
        this.requiredInRequest = requiredInRequest;
    }
    
    /**
     @return the isRequiredInRequest
     */
    @Column(name="REQUIRED_IN_REQUEST", nullable=false)
    public boolean isRequiredInRequest() {
        return requiredInRequest;
    }
    
    @Column(name="VISIBLE_IN_USER_LIST", nullable=false)
    public boolean isVisibleInUserList() {
        return visibleInUserList;
    }
    
    public void setVisibleInUserList(boolean visibleInUserList) {
        this.visibleInUserList = visibleInUserList;
    }
    
    /**
     @param userIdentityAttributes the userIdentityAttributes to set
     */
    public void setUserIdentityAttributes(Set<UserIdentityAttribute> userIdentityAttributes) {
        this.userIdentityAttributes = userIdentityAttributes;
    }
    
    
    /**
     @return the userIdentityAttributes
     */
    @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="identityAttribute", fetch=FetchType.LAZY)
    public Set<UserIdentityAttribute> getUserIdentityAttributes() {
        return userIdentityAttributes;
    }
    
    @Column(name="SYNCHRONIZE", nullable=false)
    public boolean isSynchronize() {
        return synchronize;
    }
    
    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }
    
    @Column(name="MANAGER", nullable=false)
    public boolean isManager() {
		return manager;
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	@Column(name="IDENTIFIER", nullable=false)
	public boolean isIdentifier() {
		return identifier;
	}

	public void setIdentifier(boolean identifier) {
		this.identifier = identifier;
	}

	@Transient
    public IdentityAttribute clone() {
        try {
            IdentityAttribute clonedEntity = (IdentityAttribute) super.clone();
            
            return clonedEntity;
        } catch(CloneNotSupportedException cnfe) {
            System.out.println("Couldnt clone class: " + this.getClass().getName() + ", with exception message: " + cnfe.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        //System.out.println("Obj in entity's equals() is: " + obj);
        if (!(obj != null && obj instanceof IdentityAttribute))
            return false;
        IdentityAttribute ent = (IdentityAttribute) obj;
        if (this.identityAttributeId.equals(ent.identityAttributeId))
            return true;
        return false;
    }
    
    
    @Transient
    public UserIdentityAttribute factoryUserIdentityAttribute(User user) {
        UserIdentityAttribute uia = new UserIdentityAttribute();
        uia.setIdentityAttribute(this);
        uia.setUser(user);
        
        return uia;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Transient
    public void validateByRequestAttribute(RequestAttribute reqAttr) throws AttributeValidationException {
    	/*JB
        try {
            Attribute attribute = Attribute.factoryByRequestAttribute(reqAttr);
            validateByAttribute(attribute);
        } catch (AttributeSetValueException asve) {
            throw new AttributeValidationException(asve);
        }
        */
    }
    
    
    /**
     Validate that the specified attribute is valid and fits current Identity Attribute
     - algorithm:
     - If the Identity-Attribute was set as required, check that there are values specified, otherwise throw an exception.
     - Iterate over all Attribute's values, per value do:
     - If the specified attribute has values, then check that their data types fits the IdentityAttribute data type, otherwise throw an exception.
     - If set more/less than the allowed values, then throw an exception
     - If set values with longer length than allowed per value, then throw an exception
     - If Set a regular expression property in IdentityAttribute, then check against each of the values on the specified attribute, if failed, throw an exception
     
     @param attribute The Attribute entity to validate
     @throws AttributeValidationException
     */
    @Transient
    public void validateByAttribute(Attribute attribute) throws AttributeValidationException {
    	/*JB
        //System.out.println("Comparing Values of IA id: '" + getIdentityAttributeId() + "' to the specified Attribute, dumping values of specified Attribute with value amount: '" + attribute.getValues().size() + "'");
        for (AttributeValue av : attribute.getValues()) {
            //System.out.println("Printint first value: '" + av.getValueAsString() + "'");
        }
        
        if (isHasScriptableValidator()) {
            try {
                IdentityAttributeValidator avs = (IdentityAttributeValidator)getScriptedValidator();
                avs.setIA(this);
                avs.setInputAttribute(attribute);
                avs.validate();
            } catch (ScriptLoadingException sle) {
                throw new AttributeValidationException("Script validation name: '" + getValidatorScriptFileName() + "' could not be loaded due to: " + sle.getMessage());
            } catch (ValidationException ve) {
                throw new AttributeValidationException(ve.getMessage());
            }
            //A lot of other exceptions may occure(Such as NullPointerException/groovy.lang.MissingMethodException/etc...) -  important, we do not trust the script itself!
            catch (Exception npe) {
                throw new AttributeValidationException(npe.getMessage());
            }
        }
        
        //If IA is required, then make sure we got any values
        boolean foundAtleastOneValue = false;
        int countValues = 0;
        for (AttributeValue  currAttrValue : attribute.getValues()) {
            countValues++;
            
            //TODO: This is a problem, since from GUI we recieve input that always threated as STRING, needs to think of a way of how to use converters and set data types opon converters
                //Check wrong data types
                //if (getDataType().equals(currAttrValue.getDataType())) {
                throw new AttributeValidationException("Attribute types are not equal while IdentityAttribute name: '" + getName() + "' type is: " + getDataType().toString() + ", the set VALUE data type is: " + currAttrValue.getDataType());
            //}
            
            //Check that the values are not longer than the allowed length
            if (currAttrValue.getValueAsString().length() > getMaxLength()) {
                throw new AttributeValidationException("One of the specified Attribute's value('"+currAttrValue.getValueAsString()+"') is longer than the allowed size!");
            }
            
            if (!foundAtleastOneValue) {
                //If size of value is bigger than 0, then found at least one value
                if (currAttrValue.getValueAsString().length() > 0) {
                    foundAtleastOneValue = true;
                }
            }
        }
        
        //if required, make sure found at least one valid value
        if (isRequired()) {
            if (!foundAtleastOneValue) {
                throw new AttributeValidationException("A value is required !");
            }
        }
        
        //Check if the specified attribute's values have reached the maximum
        if (countValues > getMaxValues()) {
            throw new AttributeValidationException("Maximum values exception, allowed maximum values is: " + getMaximumValues() + ", while specified attribute values number is: " + countValues);
        }
        
        //Check if the specified attribute's values have NOT reached the minimum
        if (countValues < getMinValues()) {
            throw new AttributeValidationException("Minimum values exception, allowed minimum values is: " + getMinimumValues() + ", while specified attribute values number is: " + countValues);
        }
*/        
    }
    
    
    @Transient
    public void validateByUserIdentityAttributeValues(Collection<UserIdentityAttributeValue> uiavList) throws AttributeValidationException {
        
    	/*JB
        //for (UserIdentityAttributeValue currUIAV : uiavList) {
          //      List<AttributeValue> values = new ArrayList<AttributeValue>();
            //    values.addAll(uiavList);
        //}

        Attribute attr = new Attribute();
        attr.setValuesByUserIdentityAttributeValues(uiavList);
        
        //ANY USE OF THIS? List<AttributeValue> values = new ArrayList<AttributeValue>();
        
        
        //SHITvalues.addAll(uiavList);
        
        //SHITattr.getValues().addAll(uiavList);
        
        validateByAttribute(attr);
        */
    }
    
    
    @Transient
    public String getValidatorScriptResourceName() {
    	/*JB
        String scriptName = SysConf.getSysConf().getString("system.directory.user_workspace_dir")
            +  "/" + SysConf.getSysConf().getString("system.directory.identity_attribute_validstors_dir")
            + "/" + getValidatorScriptFileName();
        
        return scriptName;
        */
    	return null;
    }
    
    
    
    @Transient
    public AttributeValidatorInterface getScriptedValidator() throws ScriptLoadingException {
        //System.out.println("Trying to validate attribue by loading resource name: " + getValidatorScriptResourceName());
        ScriptFactory sf = new ScriptFactory();
        Object scriptedObj = sf.factoryScriptableObjectByResourceName(getValidatorScriptResourceName());
        AttributeValidatorInterface avs = (AttributeValidatorInterface)scriptedObj;
        
        return avs;
    }
    
    /**
     NOTE: From external validations whether this IA is required or not use this method! and not 'isRequired' directly!
     Since this method, if needed also check in script if this IA is a must or not!
     */
    @Transient
    public boolean isIdentityAttributeRequired() {
        if (isRequired()) {
            return true;
        }
        //JB
        else {
        	return false;
        }
        
        /*JB !!!
        if (getValidatorScriptFileName() != null) {
            if (getValidatorScriptFileName().length() > 0) {
                try {
                    AttributeValidatorInterface avs = getScriptedValidator();
                    return avs.isRequired();
                } catch (ScriptLoadingException sle) {
                    return true;
                }
            } else {
                return isRequired();
            }
        } else {
            return isRequired();
        }
        */
    }
    
    @Transient
    public boolean isHasScriptableValidator() {
    	/*JB
        if (getValidatorScriptFileName() != null) {
            if (getValidatorScriptFileName().length() > 0) {
                return true;
            }
        }
        
        return false;
        */
    	return false;
    }
    
    
    @Transient
    public void factoryNewResourceAttributeSource() {
    	IdentityAttributeSourceByResourceAttribute iaras = new IdentityAttributeSourceByResourceAttribute();
    	iaras.setCreationDate(new Date());
    	iaras.setIdentityAttribute(this);
    	iaras.setDescription("[set]");
    	iaras.setSequence(0);
    	iaras.setSyncByResourceAttributePolicy(SyncByResourceAttributePolicy.SYNC_BY_ACCOUNT_ASSOCIATED_TO_USER);
    	
    	getSources().add(iaras);
    }
}
