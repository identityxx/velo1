package velo.ejb.seam.action;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.collections.Accounts;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.seam.ResourceHome;
import velo.entity.ReadyAction;
import velo.entity.ReconcileEvent;
import velo.entity.ReconcileEventResponse;
import velo.entity.Resource;
import velo.exceptions.DataTransformException;
import velo.exceptions.ReconcileProcessException;
import velo.reconciliation.processes.ReconcileIdentitiesExistenceProcess;
import velo.reconciliation.utils.ReconcileDataImportManager;

@Name("testActions")
public class TestActions {
	@Logger Log log;
	@In FacesMessages facesMessages;
	@In(create=true) ResourceHome resourceHome;
	@In EventManagerLocal eventManager;
	@In EntityManager entityManager;
	@In ResourceManagerLocal resourceManager;
	
	public void executeAllIdentitiesReconcile() { 
		log.info("Executing all identities reconcile for resource name '#0'", resourceHome.getInstance().getDisplayName());
		//effective resource loading
		
		resourceHome.setId(new Long(1));
		
		ReconcileIdentitiesExistenceProcess process = new ReconcileIdentitiesExistenceProcess();
		process.setResource(resourceHome.getInstance());
		
		ReconcileDataImportManager i = new ReconcileDataImportManager();
		Accounts accounts = null;
		//try {
//			accounts = i.importAllIdentitiesFromResource(resourceHome.getInstance());
		//} catch (DataTransformException e) {
//			facesMessages.add(e.getMessage());
			//return;
		//}
		
		//expecting all identities.
		try {
			process.execute(accounts);
		} catch (ReconcileProcessException e) {
			facesMessages.add(e.getMessage());
			return;
		}
	}
	
	
	public void importReconcileItems() {
		//Load the event 
		ReconcileEvent re = eventManager.findReconcileEvent("IDENTITY_REMOVED");
		ReadyAction ra = entityManager.find(ReadyAction.class, new Long(1));
		Resource res = resourceManager.findResource("testapp1");
		
		//entityManager.persist(ra);
		//Attach a reconcile response with an action that just prints the context
		ReconcileEventResponse rer = new ReconcileEventResponse(ra,res.getReconcilePolicy());
		rer.setActive(true);
		re.addResponse(rer);
		//entityManager.persist(rer);
		
		
		//entityManager.merge(re);
	}
}
