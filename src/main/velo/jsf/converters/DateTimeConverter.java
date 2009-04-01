package velo.jsf.converters;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;


@Name("dateTimeConverter")
@BypassInterceptors
@org.jboss.seam.annotations.faces.Converter
public class DateTimeConverter extends org.jboss.seam.ui.converter.DateTimeConverter {
	public DateTimeConverter() {
		super();
		
		setType("both");
	}
}
