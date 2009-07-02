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

import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.ReconcileEventResponse;
import velo.entity.SequencedAction;
import velo.entity.PersistenceAction.ActionTypes;
import velo.entity.Task.TaskStatus;

@Name("reconcileEventResponseHome")
public class ReconcileEventResponseHome extends EntityHome<ReconcileEventResponse> {

	@In
	FacesMessages facesMessages;
	
	@In
	ReconcileResourcePolicyHome reconcileResourcePolicyHome;
	
	private ActionTypes actionType;
	
	
	public void setEventResponseId(Long id) {
		setId(id);
	}

	public Long getEventResponseId() {
		return (Long) getId();
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ReconcileEventResponse getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	
	@Override
	public String persist() {
		getInstance().setReconcilePolicy(reconcileResourcePolicyHome.getInstance());
		return super.persist();
	}

	public ActionTypes getActionType() {
		return actionType;
	}

	public void setActionType(ActionTypes actionType) {
		this.actionType = actionType;
	}
	
	public List<SequencedAction> getActionListForType() {
		if (getActionType() == ActionTypes.READY_ACTION) {
			return getEntityManager().createNamedQuery("readyAction.findAllActive").getResultList();
		} else if (getActionType() == ActionTypes.SCRIPTED_ACTION) {
			return getEntityManager().createNamedQuery("scriptedAction.findAllActive").getResultList();
		} else {
			return null;
		}
	}
	
	
	@Factory("actionTypes")
	public ActionTypes[] getActionTypes() {
		return ActionTypes.values();
	}
}
