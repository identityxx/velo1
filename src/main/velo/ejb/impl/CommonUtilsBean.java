/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.ejb.impl;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.CommonUtilsManagerRemote;
import velo.entity.EventLog;

/**
 A Stateless EJB bean for managing common general utils methods.
 
 @author Asaf Shakarchi
 */
@Stateless()
public class CommonUtilsBean implements CommonUtilsManagerLocal, CommonUtilsManagerRemote {
    private static Logger logger = Logger.getLogger(CommonUtilsBean.class.getName());
    
    /**
     Injected entity manager
     */
    @PersistenceContext public EntityManager em;
    
    @Deprecated
    public void addEventLog(EventLog el) {
        logger.info("Persisting EventLog entity: " + el.toString());
        em.persist(el);
    }
    
    @Deprecated
    public void addEventLog(String moduleName, String status, String severity, String summaryMessage,
        String detailedMessage) {
        
        // EventLog el = new EventLog(moduleName,status,severity,summaryMessage,detailedMessage);
        EventLog el = new EventLog(moduleName, severity, summaryMessage);
        
        em.persist(el);
    }
    
    /*
    public Logger getLogger(String loggerName) {
        //SysLogger sl = new SysLogger();
        //return sl.getLogger(loggerName, em);
    }
     */
}
