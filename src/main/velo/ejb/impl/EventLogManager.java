package velo.ejb.impl;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import velo.entity.EventLogEntry;
import velo.entity.EventLogEntry.EventLogLevel;
import velo.entity.EventLogEntry.EventLogModule;

@Name("eventLogManager")
public class EventLogManager {
	@Logger Log log;
	@In EntityManager entityManager;
	
	public void addEventLogEntry(EventLogEntry ele) {
        log.info("Persisting EventLog entity: " + ele.toString());
        entityManager.persist(ele);
    }
    
    public void addEventLogEntry(EventLogModule module, String category, EventLogLevel eventLogLevel, String message) {
        
    	EventLogEntry ele = new EventLogEntry(module,category, eventLogLevel, message);
        
        //TODO: THE SERVER IP/HOST
        //el.setServer(server)
        ele.setCreationDate(new Date());
        //TODO: support by whom: el.setLoggedByUser();
        
        entityManager.persist(ele);
    }
}
