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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.exceptions.ExpressionCreationException;
import velo.exceptions.MethodExecutionException;

/**
 An entity that represents an Email Template
 
 @author Asaf Shakarchi
 */

//Seam annotations
@Name("emailTemplate")
@SequenceGenerator(name="EmailTemplateIdSeq",sequenceName="EMAIL_TEMPLATE_ID_SEQ")
@Table(name="VL_EMAIL_TEMPLATE")
@Entity
@NamedQueries({
	@NamedQuery(name = "emailTemplate.findByName", query = "SELECT object(emailTemplate) FROM EmailTemplate emailTemplate where emailTemplate.name like :name"),
	
	
	
	
    @NamedQuery(name = "emailTemplate.findAll", query = "SELECT object(emailTemplate) FROM EmailTemplate emailTemplate"),
    @NamedQuery(name = "emailTemplate.findById",query = "SELECT object(emailTemplate) FROM EmailTemplate emailTemplate WHERE emailTemplate.emailTemplateId = :emailTemplateId"),
    @NamedQuery(name = "emailTemplate.searchByString", query = "SELECT object(emailTemplate) from EmailTemplate emailTemplate WHERE (emailTemplate.name like :searchString) OR (emailTemplate.description like :searchString)")
})
    
    public class EmailTemplate extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    private Long emailTemplateId;
    private String name;
    private String description;
    private String recipientsEmailAddresses;
    private String subject;
    private String content;
    
    private boolean deletable;
    
    

    //transient
    private Map<String,Object> contentVars = new HashMap<String,Object>();
    

	/**
     @param emailTemplateId the emailTemplateId to set
     */
    public void setEmailTemplateId(Long emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
    }
    
    /**
     @return the emailTemplateId
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_EMAIL_TEMPLATE_GEN",sequenceName="IDM_EMAIL_TEMPLATE_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_EMAIL_TEMPLATE_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EmailTemplateIdSeq")
    //@GeneratedValue //JB
    @Column(name="EMAIL_TEMPLATE_ID")
    public Long getEmailTemplateId() {
        return emailTemplateId;
    }
    
    
    /**
     Set the name of the role
     @param name The name of the role to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     Get the name of the role
     @return The name of the role
     */
    @Column(name="NAME", nullable=false)
    @NotNull
    public String getName() {
        return name;
    }
    
    
    /**
     Set the description of the entity
     @param description The description string to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     Get the description of the entity
     @return The description of the entity
     */
    @Column(name="DESCRIPTION", nullable=false)
    public String getDescription() {
        return description;
    }
    
    
    /**
     @param recipientsEmailAddresses the recipientsEmailAddresses to set
     */
    public void setRecipientsEmailAddresses(String recipientsEmailAddresses) {
        this.recipientsEmailAddresses = recipientsEmailAddresses;
    }
    
    /**
     @return the recipientsEmailAddresses
     */
    @Column(name="RECIPIENTS_EMAIL_ADDRESSES", nullable=true)
    public String getRecipientsEmailAddresses() {
        return recipientsEmailAddresses;
    }
    
    @Column(name="SUBJECT", nullable=false)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    /**
     @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     @return the content
     */
    @Column(name="CONTENT", nullable=true)
    @Lob
    public String getContent() {
        return content;
    }
    
    
    /**
	 * @return the deletable
	 */
    @Column(name="DELETABLE")
	public boolean isDeletable() {
		return deletable;
	}

	/**
	 * @param deletable the deletable to set
	 */
	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof EmailTemplate) {
            EmailTemplate that = (EmailTemplate) other;
            return this.emailTemplateId.longValue() == that.emailTemplateId.longValue();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        if (emailTemplateId == null) return super.hashCode();
        return emailTemplateId.hashCode();
    }
    
    
    @Deprecated
    public Object executeGetMethod(Object obj, String methodName) throws MethodExecutionException {
        //System.out.println("Trying to execute Method name: '" + methodName + "', on Object: " + obj);
        try {
            Class[] parameterTypes = new Class[] {};
            Method name = obj.getClass().getMethod(methodName, parameterTypes);
            Object[] arguments = new Object[] {};
            Object result = name.invoke(obj, arguments);
            
            return result;
            
        } catch (InvocationTargetException ite) {
            throw new MethodExecutionException("Could not execute method named: '" + methodName + "' over object: '" + obj + "', failed message is: " + ite.getMessage());
        } catch (IllegalAccessException iae) {
            throw new MethodExecutionException("Could not execute method named: '" + methodName + "' over object: '" + obj + "', failed message is: " + iae.getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new MethodExecutionException("Could not execute method named: '" + methodName + "' over object: '" + obj + "', failed message is: " + nsme.getMessage());
        }
    }
    
    
    @Transient
    @Deprecated
    public String getMethodNameFromPropertyName(String propertyName) {
        //Get the method name
        String currMethodName = propertyName.substring(propertyName.indexOf(".")+1,propertyName.length());
        
        Character c = currMethodName.charAt(0);
        String methodName = "get" + Character.toUpperCase(c) + currMethodName.substring(1,currMethodName.length());
        
        return methodName;
    }
    
    
    @Deprecated
    public void replaceMacrosByAttributes(Map<String,Object> objects) {
        List<String> list = getMacrosFromString(getContent());
        
        for (String currMacroOrig : list) {
            //Filter the start/end chars of the macro (%)
            String currMacro = currMacroOrig.replace("%", "");
            
            //Make sure we have a '.' in the macro
            if (!currMacro.contains(".")) {
                System.out.println("No objects or wrong macros has been were specified, skipping macro of value: '" + currMacroOrig + "'");
                continue;
            }
            
            //Tokenize the macro by '.' delimiter and keep the elements in an ArrayList
            StringTokenizer st = new StringTokenizer(currMacro,".");
            ArrayList<String> elements = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                elements.add(st.nextToken());
            }
            System.out.println("Elements number: " + elements.size());
            System.out.println("Dumping elements...");
            for (String currElement : elements) {
                System.out.println(currElement);
            }
            
            
            //An indicator whether there is a next element or not.
            boolean hasNextElement = false;
            //The object that kept from current run (needed by next run in order to fetch the next property)
            Object objFromCurrentElement = new Object();
            //Whether an object was found from previous run or not (makes the decigion whether to execute the next property over the original Map or over an object fetched from previous run)
            boolean invokeOverPreviousObject = false;
            
            //Iterate over the elements
            for (int i=0;i<elements.size();i++) {
                
                String currElement = elements.get(i);
                
                //Indicate whether there is a next element or not
                if (i+1 < elements.size()) {
                    hasNextElement = true;
                } else {
                    hasNextElement = false;
                }
                
                //If next element is an object, then fetch the next object and keep it for execution
                if (hasNextElement) {
                    String nextElement = elements.get(i+1);
                    System.out.println("OBJECT: " + currElement + ", method name: '" + nextElement + "'");
                    
                    //If there is another next element, then we know that the current 'next element' is an object.
                    if (i+2 < elements.size()) {
                        System.out.println("Next element is an OBJECT. fetching the object...");
                        //If object is null (means it couldnt be found), then break the whole macro replacement
                        if (!objects.containsKey(currElement)) {
                            System.out.println("Couldnt find element (object) named: '" + currElement + "', skipping the whole macro...");
                            break;
                        }
                        
                        //Get the name of the method of this property
                        String methodNameOfCurrentElement = getMethodNameFromPropertyName(nextElement);
                        
                        
                        //If there was a previous execution, then the next execution should be over the previous fetched object, otherwise fetch the object from the map.
                        //NOTE: in first time it wont get fired anyway, since the indicator was initialzed to false!!
                        //if (invokeOverPreviousObject) {
                        //objFromCurrentElement = objects.get(currElement);
                        
                        //}
                        //else {
                        objFromCurrentElement = objects.get(currElement);
                        if (objFromCurrentElement == null) {
                            System.out.println("Couldnt fetch OBJECT of element named: '" + currElement + "', skipping the whole macro...!");
                            break;
                        }
                        
                        System.out.println(objFromCurrentElement);
                        try {
                            objFromCurrentElement = executeGetMethod(objFromCurrentElement, methodNameOfCurrentElement);
                            //}
                            
                            //Indicate that there was a previous object
                            invokeOverPreviousObject = true;
                        } catch (MethodExecutionException mee) {
                            System.out.println("Skipping macro replacement due to error: " + mee.getMessage());
                            break;
                        }
                    }
                    //This is the last execution, means our last property, lets return its value
                    else {
                        System.out.println("-LAST method execution-");
                        String methodNameOfCurrentElement = getMethodNameFromPropertyName(nextElement);
                        
                        
                        //If there were previous loadings, then the last property's getMethod should be executed over the last fetched object!, otherwise fetch the object from the MAP specified in this method as a parameter
                        if (invokeOverPreviousObject) {
                            System.out.println("Object name that method will be executed over(Which is recieved from previous object) is: " + objFromCurrentElement + ", method to be invoked is: " + methodNameOfCurrentElement);
                            
                            //If fetched object is null then skip the execution
                            if (objFromCurrentElement == null) {
                                System.out.println("Null object! skipping replacement of the whole macro!");
                                break;
                            } else {
                                try {
                                    Object returnedObjFromInvoke = executeGetMethod(objFromCurrentElement, methodNameOfCurrentElement);
                                    
                                    
                                    //DO THE REPLACEMENT
                                    content = content.replace(currMacroOrig, returnedObjFromInvoke.toString());
                                    
                                    
                                    
                                } catch (MethodExecutionException mee) {
                                    System.out.println("Skipping macro replacement due to error: " + mee.getMessage());
                                    break;
                                }
                            }
                        } else {
                            Object objFromMap = objects.get(currElement);
                            
                            if (objFromMap == null) {
                                System.out.println("Cannot find object named: '" + currElement + "' in MAP set as a parameter of this method, skipping element..");
                                break;
                            }
                            
                            System.out.println("Object name that method will be executed over is: " + objFromMap + ", method to be invoked is: " + methodNameOfCurrentElement);
                            try {
                                Object returnedObjFromInvoke = executeGetMethod(objFromMap, methodNameOfCurrentElement);
                                
                                
                                //DO THE REPLACEMENT
                                content = content.replace(currMacroOrig, returnedObjFromInvoke.toString());
                                
                                
                            } catch (MethodExecutionException mee) {
                                System.out.println("Skipping macro replacement due to error: " + mee.getMessage());
                                break;
                            }
                        }
                        
                    }
                    
                }
            }
        }
    }
    
    
    @Transient
    @Deprecated //26-aprl-07: This is old before JEXL was in use and expressions were parsed by our code
    public List<String> getMacrosFromString(String content) {
        List<String> macroList = new ArrayList<String>();
        
        Pattern pattern = Pattern.compile("%.*?%");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String currMacro = content.substring(matcher.start(), matcher.end());
            macroList.add(currMacro);
        }
        
        return macroList;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    //New JEXL methods (used by the 'getParsedContent' method)
    public List<String> getExpressionsFromString(String content) {
        List<String> expressions = new ArrayList<String>();
        
        Pattern pattern = Pattern.compile("%.*?%");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String currExpression = content.substring(matcher.start(), matcher.end());
            
            
            expressions.add(currExpression);
        }
        
        return expressions;
    }
    
    
    @Transient
    public String getParsedContent() throws ExpressionCreationException {
        JexlContext jc = JexlHelper.createContext();
        
        
        for(Map.Entry<String,Object> currVar : getContentVars().entrySet()) {
            jc.getVars().put(currVar.getKey(), currVar.getValue() );
        }
        
        String content = getContent();
        for (String currExpression : getExpressionsFromString(content)) {
            //Filter the start/end chars of the macro (%)
            String currExprWithoutPrecentage = currExpression.replace("%", "");
            
            //Replace &#39; to ' (Which is valid as a part of method execution with parameters)
            currExprWithoutPrecentage = currExprWithoutPrecentage.replace("&#39;","'");
            
            System.out.println("Executing expression: '" + currExprWithoutPrecentage + "'");
            
            try {
                Expression e = ExpressionFactory.createExpression(currExprWithoutPrecentage);
                
                // Now evaluate the expression, getting the result
                Object o = e.evaluate(jc);
                System.out.println("Resulted object is: " + o);
                if (o != null) {
                    content = content.replace(currExpression, o.toString());
                }
                else {
                    content = content.replace(currExpression, "");
                }
            } catch (Exception ex) {
                throw new ExpressionCreationException(ex);
            }
        }
        
        return content;
    }

    
    
    
    
    
    //accessories
	/**
	 * @return the contentVars
	 */
    @Transient
	public Map<String, Object> getContentVars() {
		return contentVars;
	}

	/**
	 * @param contentVars the contentVars to set
	 */
	public void setContentVars(Map<String, Object> contentVars) {
		this.contentVars = contentVars;
	}
	
	public void addContentVar(String macroName, Object obj) {
		getContentVars().put(macroName, obj);
	}
}
