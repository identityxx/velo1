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

import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.ApproversGroup;
import velo.entity.User;
import velo.exceptions.OperationException;

@Name("approversGroupHome")
public class ApproversGroupHome extends EntityHome<ApproversGroup> {

	@In
	FacesMessages facesMessages;

	@In
	UserManagerLocal userManager;

	public void setApproversGroupId(Long id) {
		setId(id);
	}

	public Long getApproversGroupId() {
		return (Long) getId();
	}

	@Override
	protected ApproversGroup createInstance() {
		ApproversGroup approverGroup = new ApproversGroup();
		approverGroup.setType("generic");
		return approverGroup;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ApproversGroup getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}


	@Override
	public ApproversGroup getInstance() {
		//Can't use isManaged as it'll involve getInstance()
		
		
		if (getEntityManager().contains( super.getInstance()) && super.getInstance().getApprovers() != null) {
			userManager.setEntityManager(getEntityManager());

			try {
				for (User currApprover : super.getInstance().getApprovers()) {
					userManager.loadUserAttributes(currApprover);
				}

			} catch (OperationException e) {
				getLog().error(e.getMessage());
				getFacesMessages().add(e.getMessage());
				return null;
			}
		}

		return super.getInstance();
	}
}
