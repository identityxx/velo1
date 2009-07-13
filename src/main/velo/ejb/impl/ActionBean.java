package velo.ejb.impl;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import velo.ejb.interfaces.ActionManagerLocal;
import velo.entity.ActionLanguage;
import velo.entity.PersistenceAction;
import velo.entity.ReadyAction;
import velo.entity.SequencedAction;
import velo.entity.WorkflowScriptedAction;

@Stateless
@AutoCreate
@Name("actionManager")
public class ActionBean implements ActionManagerLocal {
	private static Logger log = Logger.getLogger(ActionBean.class.getName());
	
	@PersistenceContext
	public EntityManager em;
	
	public ReadyAction findReadyAction(String name) {
		log.debug("Finding Ready Action with name '" + name + "'");

		try {
			Query q = em.createNamedQuery("readyAction.findByName").setParameter("name",name);
			return (ReadyAction) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("'Could not find any Ready Action with name '" + name + "', returning null.");
			return null;
		}
	}
	
	public SequencedAction findSequencedAction(String name) {
		log.debug("Finding Sequenced Action with name '" + name + "'");

		try {
			Query q = em.createNamedQuery("sequencedAction.findByName").setParameter("name",name);
			return (SequencedAction) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("'Could not find any sequence action with name '" + name + "', returning null.");
			return null;
		}
	}
	
	public ActionLanguage findActionLanguage(String name) {
		log.debug("Finding Action Language with name '" + name + "'");

		try {
			Query q = em.createNamedQuery("actionLanguage.findByName").setParameter("name",name);
			return (ActionLanguage) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("'Could not find any Action Language with name '" + name + "', returning null.");
			return null;
		}
	}
	
	public void persistAction(SequencedAction action) {
		em.persist(action);
	}
}
