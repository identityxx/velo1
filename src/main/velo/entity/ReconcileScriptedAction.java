package velo.entity;

import javax.persistence.Transient;

//@Entity
//@Table(name="VL_REC_SCRIPTED_ACTION")
public class ReconcileScriptedAction extends ScriptedAction {
	private static final long serialVersionUID = 1023411L;
	
	
	@Override
	@Transient
	public String getDisplayableActionType() {
		return "Reconcile Scripted Action";
	}
}
