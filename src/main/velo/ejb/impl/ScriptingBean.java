package velo.ejb.impl;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import velo.ejb.interfaces.ScriptingManagerLocal;
import velo.entity.ActionLanguage;

@Stateless()
@Name("scriptingManager")
@AutoCreate
public class ScriptingBean implements ScriptingManagerLocal {
	private static Logger log = Logger.getLogger(ScriptingBean.class.getName());
	
	
	@PersistenceContext
	public EntityManager em;
	
	
	public ActionLanguage findActionLanguage(String name) {
		try {
			Query q = em.createNamedQuery("actionLanguage.findByName").setParameter("name",name);
			return (ActionLanguage) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Could not find any action language for name '" + name + "', returning null.");
			return null;
		}
	}
}
