package velo.ejb.seam.actions;

import org.jboss.seam.framework.EntityHome;

import velo.entity.ActionScript;

public class ScriptedActionEntityHome<E> extends EntityHome<E>{
	private ActionScript script;
	
	public ActionScript getScript() {
		if (script == null) {
			script = new ActionScript();
			script.init();
		}
		
		return script;
	}
}
