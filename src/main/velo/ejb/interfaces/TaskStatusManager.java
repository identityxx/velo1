package velo.ejb.interfaces;

import javax.ejb.Local;

import velo.entity.Task;
import velo.entity.Task.TaskStatus;


@Local
public interface TaskStatusManager {
	public void changeStatus(TaskStatus newStatus, Task task);
}
