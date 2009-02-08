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
import org.jboss.seam.ui.AbstractEntityLoader;

import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.action.UserManageActions;
import velo.entity.User;

@Name("userNameToUserEntityConverter")
@BypassInterceptors
@Converter
public class UserNameToUserEntityConverter implements javax.faces.convert.Converter {
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
		
		if (value instanceof User) 
		{
			User u = (User)value;
			return u.getName();
		}
		
		
		return null;
	}


	@Transactional
	public Object getAsObject(FacesContext facesContext, UIComponent cmp, String value) throws ConverterException
	{
		if (value == null)
		{
			return null;
		}
		
		UserManagerLocal userManager = (UserManagerLocal)Component.getInstance("userManager");
		User u = userManager.findUser(value);
		
		if (u == null) {
			throw new ConverterException(createMessage(FacesMessage.SEVERITY_ERROR,"Could not find user name '" + value + "'"));
		}
		
		//return getEntityLoader().get(value);
		
		//return value;
		return u;
	}
	
	
	private FacesMessage createMessage(FacesMessage.Severity severity, String msg) {
		return new FacesMessage(severity,msg, null);
	}
}
