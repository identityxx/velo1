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

import javax.el.ValueExpression;
import javax.faces.component.UIComponentBase;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import java.io.IOException;
//import velo.entity.UserIdentityAttributeValue;


public class UIValueGenericInput extends UIComponentBase {
	public void encodeBegin(FacesContext context) throws IOException {
		//UserIdentityAttributeValue uiav = (UserIdentityAttributeValue) getAttributes().get("valueObject");
		
		HtmlInputText inputText = new HtmlInputText();
		ValueExpression vex = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(),"#{value.value}", java.lang.String.class);
        inputText.setValueExpression("value", vex);
        inputText.setParent(this);
        inputText.setReadonly(false);
        inputText.encodeAll(context);
	}
		
		
	public String getFamily() {
		return "EdentityFamily";
	}
	
}

/*
public class UIValueInput extends UIComponentBase {

	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String hellomsg = (String) getAttributes().get("hellomsg");

		writer.startElement("h3", this);
		if (hellomsg != null)
			writer.writeText(hellomsg, "hellomsg");
		else
			writer.writeText("Hello from a custom JSF UI Component!", null);
		writer.endElement("h3");
		writer.startElement("p", this);
		writer.writeText(" Today is: " + new Date(), null);
		writer.endElement("p");
	}

	public String getFamily() {
		return "HelloFamily";
	}
}
*/

