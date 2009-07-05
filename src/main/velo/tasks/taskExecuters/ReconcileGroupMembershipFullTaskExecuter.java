package velo.tasks.taskExecuters;

import velo.actions.readyActions.ReadyActionAPI;
import velo.collections.ResourceGroups;
import velo.entity.Resource;
import velo.entity.ResourceReconcileTask;
import velo.entity.Task;
import velo.exceptions.DataTransformException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.TaskExecutionException;
import velo.reconciliation.processes.ReconcileGroupMembershipProcess;
import velo.reconciliation.processes.ReconcileGroupsProcess;
import velo.reconciliation.utils.ReconcileDataImportManager;
import velo.tasks.TaskExecuter;

public class ReconcileGroupMembershipFullTaskExecuter implements TaskExecuter {

	@Override
	public void execute(Task task) throws TaskExecutionException {
		ReconcileGroupMembershipProcess process = new ReconcileGroupMembershipProcess();
		
		ResourceReconcileTask rrTask = (ResourceReconcileTask)task;
		//process.setResource(rrTask.getResource());
		Resource resource = ReadyActionAPI.getInstance().getResourceManager().findResource(rrTask.getResourceUniqueName());
		process.setResource(resource);
		
		ReconcileDataImportManager importer = new ReconcileDataImportManager();
		ResourceGroups groups;
		
		try {
			groups = importer.importGroupMembership(rrTask);
		} catch (DataTransformException e) {
			throw new TaskExecutionException(e);
		}
		
		try {
			process.executeFull(groups);
		} catch (ReconcileProcessException e) {
			throw new TaskExecutionException(e);
		}
	}
}
