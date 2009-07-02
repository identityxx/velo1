package velo.entity;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import velo.exceptions.ExecutionException;

//@Entity
@MappedSuperclass
public abstract class GenericTask extends Task {
	public GenericTask() {
		setStatus(TaskStatus.PENDING);
	}
	
	public abstract void execute() throws ExecutionException;
}
