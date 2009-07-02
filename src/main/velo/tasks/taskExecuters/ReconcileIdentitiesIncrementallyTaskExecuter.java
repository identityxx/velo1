package velo.tasks.taskExecuters;

import velo.collections.Accounts;
import velo.entity.ResourceReconcileTask;
import velo.entity.Task;
import velo.exceptions.DataTransformException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.TaskExecutionException;
import velo.reconciliation.processes.ReconcileAccountsProcess;
import velo.reconciliation.utils.ReconcileDataImportManager;
import velo.tasks.TaskExecuter;

public class ReconcileIdentitiesIncrementallyTaskExecuter implements TaskExecuter {

	@Override
	public void execute(Task task) throws TaskExecutionException {
		ReconcileAccountsProcess process = new ReconcileAccountsProcess();
		
		//Expecting this task to be an instance of ResourceReconcileTask
		if (!(task instanceof ResourceReconcileTask)) {
			throw new TaskExecutionException("Task is not an instance of 'Resource Reconcile Task'");
		}
		
		ResourceReconcileTask rrTask = (ResourceReconcileTask)task;
		process.setResource(rrTask.getResource());
		
		ReconcileDataImportManager importer = new ReconcileDataImportManager();
		Accounts accs;
		
		try {
			accs = importer.importIdentitiesFromResource(rrTask);
		} catch (DataTransformException e) {
			throw new TaskExecutionException(e);
		}
		
		try {
			process.executeIncrementally(accs);
		} catch (ReconcileProcessException e) {
			throw new TaskExecutionException(e);
		}
	}
}
