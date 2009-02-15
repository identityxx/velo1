package velo.jsf.validators;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Validator;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import velo.validators.Generic;

@Name("velo.ui.DateTimeValidator")
@Validator
@BypassInterceptors
public class DateTimeValidator implements javax.faces.validator.Validator,Serializable {
	Date min;
	Date max;
	private static LogProvider log = Logging.getLogProvider(DateTimeValidator.class);
	
	public static final String MIN_DATE_ERROR_MSG_ID = "velo.ui.validator.error.MIN_DATE";
	public static final String MIN_DATE_ERROR_MSG = "Minimum date constraint.";
	public static final String MAX_DATE_ERROR_MSG_ID = "velo.ui.validator.error.MAX_DATE";
	public static final String MAX_DATE_ERROR_MSG = "Maximum date constraint.";
	public static final String pattern = "yyyyMMddHHmmss";
	
	public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
		Date dateValue = (Date)value;
		//SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date minDate = getMin();
		Date maxDate = getMax();
		
		if (log.isTraceEnabled()) {
			log.trace("DateTimeValidator has invoked.");
			log.trace("\tSpecified Value:" + dateValue);
			log.trace("\tSpecified MIN date pattern:" + getMin());
			log.trace("\tSpecified MAX date pattern:" + getMax());
		}
		
		/*
		if (Generic.isNotEmptyAndNotNull(getMin())) {
			try {
				minDate = format.parse(getMin());
			}catch(ParseException e) {
				throw new ValidatorException(FacesMessages.createFacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, e.getMessage(),e.getMessage(),dateValue));
			}
		}
		
		if (Generic.isNotEmptyAndNotNull(getMax())) {
			try {
				maxDate = format.parse(getMax());
			}catch(ParseException e) {
				throw new ValidatorException(FacesMessages.createFacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, e.getMessage(),e.getMessage(),dateValue));
			}
		}
		*/
		
		
		log.trace("Checking whether the minimim date ('"+minDate+"') is before the specified value ('"+dateValue+"').");
		if (dateValue.before(minDate)) {
			throw new ValidatorException(FacesMessages.createFacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, MIN_DATE_ERROR_MSG_ID,MIN_DATE_ERROR_MSG,dateValue,minDate,maxDate));
		}
		
		log.trace("Checking whether the maximum date ('"+maxDate+"') is before the specified value ('"+dateValue+"').");
		if (dateValue.after(maxDate)) {
			throw new ValidatorException(FacesMessages.createFacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, MAX_DATE_ERROR_MSG_ID,MAX_DATE_ERROR_MSG,dateValue,minDate,maxDate));
		}
		
		log.trace("Successfully validated DateTime.");
	}

	public Date getMin() {
		return min;
	}

	public void setMin(Date min) {
		this.min = min;
	}

	public Date getMax() {
		return max;
	}

	public void setMax(Date max) {
		this.max = max;
	}
}
