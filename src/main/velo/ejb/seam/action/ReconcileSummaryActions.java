package velo.ejb.seam.action;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.ReconcileManagerLocal;

@Name("reconcileSummaryActions")
public class ReconcileSummaryActions {
	@Logger Log log;
	@In FacesMessages facesMessages;
	@In ReconcileManagerLocal reconcileManager;
	
	public void deleteAllReconcileSummaries() {
		int amount = reconcileManager.deleteAllReconcileProcessSummaries(new Date());
		
		facesMessages.add("Sucessfully removed '#0' reconcile process summaries.",amount);
	}
}
