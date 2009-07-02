package velo.actions.readyActions;

import velo.actions.Action;
import velo.exceptions.action.ActionExecutionException;

/**
 * @author Asaf Shakarchi
 *
 * A parent of all ready actions, 
 * A ready action is an action that has a known target in life.
 * 
 * Following is the methodology of how to build a ready action:
 * 
 * Validate: override the 'validate' method and verify whether the action can be executed or not 
 * (usually this phase is done by validating whether there's the required data in the action's context)
 * Execute: override the 'execute' method and perform the method execution.
 * 
 * It's also possible to execute code before/after the action execution phase
 * @see Action
 */
public abstract class ReadyAction extends Action {
	public ReadyActionAPI getAPI() {
		return ReadyActionAPI.getInstance();
	}
}
