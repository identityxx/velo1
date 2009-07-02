package velo.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="VL_SYSTEM_ER_SCRIPTED_ACTION")
public class SystemEventResponseScriptedAction extends ScriptedAction {
	private static final long serialVersionUID = 1023411L;
	
	@Override
	@Transient
	public String getDisplayableActionType() {
		return "System Event Response Scripted Action";
	}
}
