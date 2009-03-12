package velo.jsf.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.seam.action.UserManageActions;
import velo.entity.Account;
import velo.entity.Resource;

@Name("accountNameToAccountEntityConverter")
@BypassInterceptors
@Converter
public class AccountNameToAccountEntityConverter implements javax.faces.convert.Converter {
	private String resourceUniqueName;
	
	/*
	private AbstractEntityLoader entityLoader;

	public AbstractEntityLoader getEntityLoader()
	{
		if (entityLoader == null)
		{
			return AbstractEntityLoader.instance();
		}
		else
		{
			return entityLoader;
		}
	}


	public void setEntityLoader(AbstractEntityLoader entityLoader)
	{
		this.entityLoader = entityLoader;
	}
	 */
	
	@SuppressWarnings("unchecked")
	@Transactional
	public String getAsString(FacesContext facesContext, UIComponent cmp, Object value) throws ConverterException
	{
		if (value == null)
		{
			return null;
		}
		
		if (value instanceof String) {
			return value.toString();
		}
		
		return null;
	}


	@Transactional
	public Object getAsObject(FacesContext facesContext, UIComponent cmp, String value) throws ConverterException
	{
		if (value == null) {return null;}
		if (value.length() < 1) {return null;}

		//an ugly workaround to fetch the resource unique name from userManageActions as parameter binding to converters via facelets seems not be working
		UserManageActions userManageActions =  (UserManageActions)Component.getInstance("userManageActions");
		Resource res = userManageActions.getSelectedResourceForAccountToUserAssociation();
		resourceUniqueName = res.getUniqueName();
		

		AccountManagerLocal accountManager = (AccountManagerLocal)Component.getInstance("accountManager");
		Account acc = accountManager.findAccount(value,resourceUniqueName);
		
		if (acc == null) {
			throw new ConverterException(createMessage(FacesMessage.SEVERITY_ERROR,"Could not find account name '" + value + "' for resource '" + resourceUniqueName + "'"));
		}
		
		//TODO: //Ugly...:/
		//u.getApproversGroups().size();
		
		
		
		
		
		//return getEntityLoader().get(value);
		
		//return value;
		return acc;
	}
	
	
	private FacesMessage createMessage(FacesMessage.Severity severity, String msg) {
		return new FacesMessage(severity,msg, null);
	}


	
	
	
	public String getResourceUniqueName() {
		return resourceUniqueName;
	}


	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}
}
