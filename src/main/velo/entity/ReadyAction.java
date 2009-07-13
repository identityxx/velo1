package velo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import velo.exceptions.FactoryException;
import velo.exceptions.action.ActionExecutionException;
import velo.patterns.Factory;

@Table(name="VL_READY_ACTION")
@Entity
@NamedQueries( {
	@NamedQuery(name = "readyAction.findByName", query = "SELECT readyAction FROM ReadyAction readyAction WHERE readyAction.name = :name"),
	@NamedQuery(name = "readyAction.findAllActive", query = "SELECT readyAction FROM ReadyAction readyAction WHERE readyAction.active = 1")
})
public class ReadyAction extends SequencedAction {
	private String readyActionDefClassName;
	
	private velo.actions.readyActions.ReadyAction factoredReadyAction;
	
	public ReadyAction(String name, String description, String readyActionDefClassName) {
		super(name,description,true,true,0);
		setReadyActionDefClassName(readyActionDefClassName);
	}
	
	public ReadyAction() {
		
	}
	
	@Override
	public void execute() throws ActionExecutionException {
		try {
			if (factoredReadyAction == null) {
				//velo.actions.readyActions.ReadyAction ra;
				factoredReadyAction = factoryReadyAction();
				factoredReadyAction.setContext(getContext());
			}
			
			//execute the action
			factoredReadyAction._execute();
		} catch (FactoryException e) {
			throw new ActionExecutionException(e.getMessage());
		}
		
	}

	@Column(name="READY_ACTION_DEF_CLASS_NAME",nullable=false)
	public String getReadyActionDefClassName() {
		return readyActionDefClassName;
	}

	public void setReadyActionDefClassName(String readyActionDefClassName) {
		this.readyActionDefClassName = readyActionDefClassName;
	}
	
	
	public velo.actions.readyActions.ReadyAction factoryReadyAction() throws FactoryException {
		try {
			return (velo.actions.readyActions.ReadyAction)Factory.factoryInstance(getReadyActionDefClassName(),velo.actions.readyActions.ReadyAction.class);
		} catch (FactoryException e) {
			throw(e);
		}
	}
	
	@Override
	@Transient
	public String getDisplayableActionType() {
		return "Ready Action";
	}
}
