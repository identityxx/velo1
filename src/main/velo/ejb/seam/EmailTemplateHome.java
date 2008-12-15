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
package velo.ejb.seam;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import velo.entity.EmailTemplate;

@Name("emailTemplateHome")
public class EmailTemplateHome extends EntityHome<EmailTemplate> {

	@In
	FacesMessages facesMessages;
	
	public void setEmailTemplateId(Long id) {
		setId(id);
	}

	public Long getEmailTemplateId() {
		return (Long) getId();
	}

	@Override
	protected EmailTemplate createInstance() {
		EmailTemplate emailTemplate = new EmailTemplate();
		return emailTemplate;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public EmailTemplate getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	public String remove() {
		if (!getInstance().isDeletable()) {
			facesMessages.add("This item cannot be deleted.");
			return null;
		} else {
			return super.remove();
		}
	}
}
