package velo.tasks.taskExecuters;

import velo.collections.Accounts;
import velo.entity.ResourceReconcileTask;
import velo.entity.Task;
import velo.exceptions.DataTransformException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.TaskExecutionException;
import velo.reconciliation.processes.ReconcileIdentitiesExistenceProcess;
import velo.reconciliation.utils.ReconcileDataImportManager;
import velo.tasks.TaskExecuter;

public class ReconcileAllIdentitiesTaskExecuter implements TaskExecuter {

	@Override
	public void execute(Task task) throws TaskExecutionException {
		ReconcileIdentitiesExistenceProcess process = new ReconcileIdentitiesExistenceProcess();
		
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
			process.execute(accs);
		} catch (ReconcileProcessException e) {
			throw new TaskExecutionException(e);
		}
	}
}
