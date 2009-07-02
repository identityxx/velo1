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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.IgnoreDependency;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.xml.sax.SAXException;

import velo.adapters.Adapter;
import velo.adapters.AdapterFactoryabbbbb;
import velo.adapters.ResourceAdapter;
import velo.common.SysConf;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.ResourceManagerRemote;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.utils.PageControl;
import velo.ejb.utils.QueryGenerator;
import velo.entity.ReconcileProcessSummary;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceType;
import velo.entity.User;
import velo.entity.ReconcileProcessSummary.ReconcileProcesses;
import velo.exceptions.AccountIdGenerationException;
import velo.exceptions.DeleteObjectViolation;
import velo.exceptions.FactoryException;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.NoUserIdentityAttributeValueException;
import velo.exceptions.ResourceDescriptorException;
import velo.exceptions.ResourceTypeDescriptorException;
import velo.exceptions.ScriptLoadingException;
import velo.exceptions.TaskCreationException;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import velo.resource.resourceDescriptor.ResourceDescriptorReader;
import velo.resource.resourceTypeDescriptor.ResourceTypeDescriptor;
import velo.resource.resourceTypeDescriptor.ResourceTypeDescriptorReader;
import velo.rules.AccountsCorrelationRule;
import velo.scripting.ScriptFactory;
import velo.storage.Attribute;

/**
 * A Stateless EJB bean for Resource
 * 
 * @author Asaf Shakarchi
 */
@EJB(name="resourceEjbRef", beanInterface=ResourceManagerLocal.class)
@Stateless()
@Name("resourceManager")
@AutoCreate
public class ResourceBean implements ResourceManagerLocal,
        ResourceManagerRemote {
    
    private static Logger log = Logger.getLogger(ResourceBean.class.getName());
    
    /**
     * Injected entity manager
     */
    // @PersistenceContext(name="edm_acc_em", unitName="pu1")
    @PersistenceContext
    public EntityManager em;
    
    /**
     * Inject a local ResourceAttributeManager EJB
     */
    @EJB
    public ResourceAttributeManagerLocal tsam;
    
    
    /**
     * Inject a local AccountManager EJB
     */
    @org.jboss.annotation.IgnoreDependency
	@IgnoreDependency
    @EJB
    public AccountManagerLocal am;
    
    //JB@EJB //CAUSE JBOSS CIRCULATION ERROR, BUT WHY? NO DEPENDENCY ON THE OTHER SIDE
    @org.jboss.annotation.IgnoreDependency
	@IgnoreDependency
    @EJB
    TaskManagerLocal tm;
    
    
    
    public List<Resource> findResources(Resource criteria, String[] optionalData, PageControl pc) throws Exception {
    	QueryGenerator qg = new QueryGenerator(criteria,optionalData,pc);
    	
    	try {
    		System.out.println("!!!!!" + qg.getQueryString());
			Query q = qg.getQuery(em);
			return q.getResultList();
		} catch (Exception e) {
			throw e;
		}
    }
    
    
    public Resource findResource(String uniqueName) {
    	log.debug("Finding Resource in repository with unique name '" + uniqueName + "'");

		try {
			Query q = em.createNamedQuery("resource.findByUniqueName").setParameter("uniqueName",uniqueName);
			return (Resource) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Found user did not result any user for name '" + uniqueName + "', returning null.");
			return null;
		}
    }
    
    public Resource findResourceEagerly(String uniqueName) {
    	Resource r = findResource(uniqueName);
    	
    	if (r == null) {
    		return null;
    	}
    	
    	r.getAccounts().size();
    	r.getResourceActions().size();
    	r.getGroups().size();
    	r.getResourceAttributes().size();
    	r.getResourceAdmins().size();
    	r.getResourceType().getResourceTypeAttributes().size();
    	
    	return r;
    }
    
    public ResourceType findResourceType(String uniqueName) {
    	log.debug("Finding Resource Type in repository with unique name '" + uniqueName + "'");

		try {
			Query q = em.createNamedQuery("resourceType.findByUniqueName").setParameter("uniqueName",uniqueName);
			return (ResourceType) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Found user did not result any user for name '" + uniqueName + "', returning null.");
			return null;
		}
    }
    
    public List<ResourceAttribute> findAllActiveResourceAttributesToSync() {
    	List<Resource> resources = em.createNamedQuery("resource.findAllActive").getResultList();
    	
    	List<ResourceAttribute> attrs = new ArrayList<ResourceAttribute>();
    	
    	for (Resource currResource : resources) {
    		attrs.addAll(currResource.getAttributesToSync());
    	}
    	
    	return attrs;
    }
    
    
    public ReconcileProcessSummary findLatestReconcileProcessSummary(ReconcileProcesses processType) {
    	List<ReconcileProcessSummary> lst =  em.createNamedQuery("reconcileProcessSummary.findLatestForProcessType").setParameter("processType",processType).getResultList();
    	
    	if (lst.size() > 0) {
    		return lst.iterator().next();
    	} else {
    		return null;
    	}
    }
    
    public ReconcileProcessSummary findLatestSuccessfullReconcileProcessSummary(ReconcileProcesses processType) {
    	List<ReconcileProcessSummary> lst =  em.createNamedQuery("reconcileProcessSummary.findLatestSuccessfullForProcessType").setParameter("processType",processType).getResultList();
    	
    	if (lst.size() > 0) {
    		return lst.iterator().next();
    	} else {
    		return null;
    	}
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //maybe should move out of here to the resource entity?
    public ResourceAdapter adapterFactory(Resource resource) throws FactoryException {
        try {
        	ResourceDescriptor tsd = resource.factoryResourceDescriptor();
            
        	log.info("Retrieving adapter Class name for factoring correct adapter for target system name: "
                    + resource.getDisplayName());
            //String adapterClassName = tsd.getResourceDescriptorAdapter().getClassName();
        	String adapterClassName = tsd.getString("adapter.className");
            log.info("For Adapter factory, fetched adapter class name for Resource name: "
                    + resource.getDisplayName()
                    + ", adapter class name: " + adapterClassName);
            
            Adapter factoredAdapter = AdapterFactoryabbbbb.adapterFactory(
                    adapterClassName, resource);
            log.info("Factored Adapter class name is: "
                    + factoredAdapter.getClass().getName());
            Class[] a = factoredAdapter.getClass().getInterfaces();
            log
                    .info("Factored Adapter INTERFACES number for adapter class name: "
                    + factoredAdapter.getClass().getName()
                    + ", is: "
                    + a.length);
            for (int i = 0; i < a.length; i++) {
            	log.info("Interface (" + i + ")"
                        + " of factored adapter name: " + a[i]);
            }
            Class[] ab = factoredAdapter.getClass().getClasses();
            log
                    .info("Factored Adapter CLASSES number for adapter class name: "
                    + factoredAdapter.getClass().getName()
                    + ", is: "
                    + ab.length);
            
            for (int i = 0; i < ab.length; i++) {
            	log.info("CLASS (" + i + ")" + " of factored adapter name: "
                        + ab[i]);
            }
            
            ResourceAdapter adapter = (ResourceAdapter)factoredAdapter;
            log.info("factored adapter object named: " + adapter);
            return adapter;
        } catch (FactoryException fe) {
            throw new FactoryException(
                    "Failed to factory adapter, failed with message: "
                    + fe.getMessage());
        } catch (ResourceDescriptorException tsde) {
            throw new FactoryException(
                    "Failed to factory adapter  due to ResourceDescriptor read error, failed with message: "
                    + tsde.getMessage());
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public boolean addResource(Resource ts) {
        em.persist(ts);
        
        return true;
    }
    
    public boolean removeResource(Resource ts) throws DeleteObjectViolation {
        //Check dependencies first.
        if (isResourceAssignedToRoles(ts)) {
            throw new DeleteObjectViolation("Target System assigned to Role(s).");
        }
        
        //TODO Remove the files related to target system too
        removeResourceEntity(ts);
        
        return true;
    }
    
    public void persistResourceEntity(Resource resource) {
        em.persist(resource);
    }
    
    public void removeResourceEntity(Resource resource) {
        Resource tsToDelete = em.merge(resource);
        em.remove(tsToDelete);
    }
    
    
    public void persistResourceTypeEntity(ResourceType resourceType) {
        em.persist(resourceType);
    }
    
    public void removeResourceTypeEntity(ResourceType resourceType) {
        ResourceType tstToDelete = em.merge(resourceType);
        em.remove(tstToDelete);
    }
    
    @Deprecated
    public ResourceType findResourceTypeByUniqueName(String uniqueName) throws NoResultFoundException {
        try {
        	log.debug("Finding Resource Type for Unique Name: '" + uniqueName + "'");
            ResourceType loadedResourceType = (ResourceType) em.createNamedQuery(
                    "resourceType.findByUniqueName").setParameter("uniqueName", uniqueName).getSingleResult();
            
            return loadedResourceType;
        } catch (NoResultException e) {
            throw new NoResultFoundException("Failed to load Target System Type entity, couldnt find Target System Type for Unique name '" + uniqueName + "'");
        }
    }
    
    
    public boolean updateResource(Resource ts) {
        em.merge(ts);
        
        return true;
    }
    
    public Resource findResourceById(long id) throws NoResultFoundException {
        try {
            System.out.println("Finding Resource for ID: " + id);
            Resource loadedTs = (Resource) em.createNamedQuery(
                    "findResourceById").setHint("toplink.refresh", "true")
                    .setParameter("id", id)
                    .getSingleResult();
            //?what for? em.refresh(loadedTs);
            System.out.println("Found Resource name: " + loadedTs.getUniqueName());
            return loadedTs;
        } catch (NoResultException e) {
            throw new NoResultFoundException("Failed to load resource, couldnt find Resource ID number: " + id);
        }
    }
    
    public Resource loadResourceByIdEagrly(long id) throws NoResultFoundException {
        try {
            System.out.println("EAGRLY Loading Resource for ID: " + id);
            Resource loadedTs = (Resource) em.createNamedQuery(
                    "findResourceById").setHint("toplink.refresh", "true")
                    .setParameter("id", id)
                    .getSingleResult();
            
            log.debug("Eagrly loaded with groups amounts: " + loadedTs.getGroups().size());
            log.debug("Eagrly loaded with attributes amounts: " + loadedTs.getResourceAttributes().size());
            
            return loadedTs;
        } catch (NoResultException e) {
            throw new NoResultFoundException("Failed to load resource, couldnt find Resource ID number: " + id);
        }
    }
    
    public Resource loadResourceForAction(long id) throws NoResultFoundException {
        Resource loadedTs = findResourceById(id);
        log.debug("Eagrly loaded with attributes amounts: " + loadedTs.getResourceAttributes().size());
        
        return loadedTs;
    }
    
    public Resource findResourceByName(String resourceUniqueName) throws NoResultException {
        try {
            return (Resource) em.createNamedQuery("findResourceByName").setHint("toplink.refresh", "true")
                    .setParameter("uniqueName", resourceUniqueName.toUpperCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            // throw new EJBException("Couldnt load Resource for name: " +
            // resourceName);
            // throw new EJBException(e);
            throw e;
        } catch (Exception e) {
            throw new NoResultException(e.getMessage());
        }
    }
    
    
    public List<Resource> findAllActiveResources() {
        return em.createNamedQuery("resource.findAllActive").setHint("toplink.refresh", "true").getResultList();
    }
    
    public boolean isAccountExistOnResource(Resource ts, String accountName) {
        Query q = em.createNamedQuery("isAccountByNameExistOnResource").setHint("toplink.refresh", "true")
                .setParameter("accountName", accountName).setParameter(
                "resource", ts);
        Long num = (Long) q.getSingleResult();
        
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    
        /*
        public ResourceDescriptor factoryResourceDescriptor(
                        Resource resource) throws ResourceDescriptorException {
                // Get the XML Descriptor class from the ResourceType object
                String tsDescriptorReaderClassName = resource.getResourceType()
                                .getResourceDescriptorClassName();
                logger.info("Factoring ResourceDescriptor class name: "
                                + tsDescriptorReaderClassName + ", for target system: "
                                + resource.getDisplayName());
         
                try {
                        // Cast the found descriptor from simple object to the basic
                        // TSDescriptor class (Should be enough to handle all Descryptor
                        // functionality)
                        ResourceDescriptorReader tsDescriptorReader = (ResourceDescriptorReader) Factory
                                        .factoryInstance(tsDescriptorReaderClassName);
         
                        String descriptorFileName = SysConf.getSysConf().getString("system.directory.user_workspace_dir") + "/" +
                        SysConf.getSysConf().getString("system.directory.targets_files_dir") + "/"
                        + resource.getShortName().toLowerCase() + "/"
                        + SysConf.getSysConf().getString("system.directory.conf_dir_per_ts") + "/"
                        + resource.getConfFileName();
         
                        // Parse the conf file
                        tsDescriptorReader.parseAndSetXmlFile(descriptorFileName);
                        return tsDescriptorReader.getResourceDescriptor();
                } catch (ClassNotFoundException cnfe) {
                        // System.out.println("Fatal error occured, target description class
                        // was not found for class: " + cnfe.getMessage());
                        // return null;
                        // throw new RemoteException("Class not found while trying to
                        // factory ResourceDescriptor, failed with message: " +
                        // cnfe.getMessage());
                        throw new ResourceDescriptorException(
                                        "Could not find the corresponding resourceDescriptorReader class, seeked class name: "
                                                        + tsDescriptorReaderClassName);
                }
                catch(SAXException saxe) {
                        throw new ResourceDescriptorException(saxe.getMessage());
                }
                catch(IOException ioe) {
                        throw new ResourceDescriptorException(ioe.getMessage());
                }
        }
         */
    
    
    @Deprecated
    public ResourceDescriptor factoryResourceDescriptor(Resource resource) throws ResourceDescriptorException {
        ResourceDescriptorReader tsDescriptorReader = new ResourceDescriptorReader();
        
        //TODO: use resource.getConfigurationFilename instead
        String descriptorFileName = SysConf.getSysConf().getString("system.directory.user_workspace_dir") + "/" +
                SysConf.getSysConf().getString("system.directory.targets_files_dir") + "/"
                + resource.getUniqueName().toLowerCase() + "/"
                + SysConf.getSysConf().getString("system.directory.conf_dir_per_ts") + "/";
                //+ resource.getConfFileName();
        
        try {
            //	Parse the conf file
            tsDescriptorReader.parseAndSetXmlFile(descriptorFileName);
            return tsDescriptorReader.getResourceDescriptor();
        } catch (IOException ioe) {
            throw new ResourceDescriptorException(ioe.getMessage());
        } catch(SAXException saxe) {
            throw new ResourceDescriptorException(saxe.getMessage());
        }
    }
    
    
    @Deprecated
    public ResourceTypeDescriptor factoryResourceTypeDescriptor(ResourceType tst) throws ResourceTypeDescriptorException {
        ResourceTypeDescriptorReader tstdr = new ResourceTypeDescriptorReader();
        
//arr        String descriptorFileName = SysConf.getResourceTypeDescriptorFileName(tst);
        String descriptorFileName = null;
        
        /*String descriptorFileName = SysConf.getSysConf().getString("system.directory.target_system_type_conf_dir")
                + "/"
                + tst.getNameIdentifier().toLowerCase()
                + ".xml";
        */
        
        
        try {
            //	Parse the conf file
            tstdr.parseAndSetXmlFile(descriptorFileName);
            return tstdr.getResourceTypeDescriptor();
        } catch (IOException ioe) {
            throw new ResourceTypeDescriptorException(ioe.getMessage());
        } catch(SAXException saxe) {
            throw new ResourceTypeDescriptorException(saxe.getMessage());
        }
    }
    
    
    
    /*
    public ResourceActionInterface factoryActionScriptByActionName(
            Resource resource, String actionName)
            throws ScriptLoadingException {
        // Get a propercase of the short target system name
        // String scriptName =
        // StringUtils.capitalize(this.getShortName().toLowerCase());
        // Decided at the end to go all for lowercase
        String scriptName = resource.getUniqueName().toLowerCase() + "_";
        // Now add the actionName to the scriptName
        scriptName += actionName.toLowerCase();
        
        String scriptResourceName = "targets_scripts" + "."
                + resource.getUniqueName().toLowerCase() + "." + "actions"
                + "." + scriptName;
        
        ActionManager am = new ActionManager();
        ResourceActionInterface tsai = am.factoryScriptedResourceAction(
                scriptResourceName, resource);
        
        Class[] c = tsai.getClass().getInterfaces();
        log.debug("Name is: " + c[0].getName());
        
        return tsai;
    }
    */
    
        /*
         * @Transient public ResourceDescriptor factoryResourceAdapter() {
         * //Try to factory the corresponding Adapter for this Target System through
         * the TSDescriptor's Adapter class field ResourceDescriptorReader
         * tsDescriptorReader =
         * (ResourceDescriptorReader)Factory.factoryInstance(tsDescriptorReaderClassName);
         * ResourceDescriptor factoryResourceDescriptor() }
         */
    
        /*
         * @param accountName The account name to check whether a User exist for it
         * or not @return true/false upon match/unmatch
         */
        /*
         * @Transient public boolean isUserExitForAccount(String accountName) {
         * //Transform the accountName attribute to a Username return false; }
         */
    
    public AccountsCorrelationRule correlationRuleFactory(
            Resource resource) {
        String scriptResourceName = "targets_scripts" + "."
                + resource.getUniqueName().toLowerCase() + "." + "rules"
                + "." + "accounts_correlation_rule";
        try {
            ScriptFactory sf = new ScriptFactory();
            return (AccountsCorrelationRule) sf
                    .factoryScriptableObjectByResourceName(scriptResourceName);
        } catch (ScriptLoadingException sle) {
        	log
                    .warn("Cannot factory a correlation rule for target system name: "
                    + resource.getDisplayName()
                    + ", message: "
                    + sle.getMessage());
            return null;
        }
    }
    
    
    public String generateNewAccountId(Resource resource, User user) throws AccountIdGenerationException {
        //Find the ResourceAttribute that flagged as an 'Account ID' for the specified target system
        
        try {
        	log.info("Trying to generate a new account id for User: '" + user.getName()+ "', on target system: '" + resource.getDisplayName() + "'");
            ResourceAttribute tsaAccId = tsam.factoryAccountIdResourceAttribute(resource);
            log.info("Found target system attribute that was flagged as an Account ID with name: '" + tsaAccId.getDisplayName() + "'");
            
            log.info("Getting virtual account attribute value which is our generated account ID...");
            Attribute accIdAttr = am.loadVirtualAccountAttribute(user,tsaAccId);
            log.info("Got a virtual account attribute value which is our generated account ID which is: '" + accIdAttr.getValueAsString());
            
            //Make sure that the value recieved is not a null value.
            if (accIdAttr.getValueAsString() == null) {
                throw new AccountIdGenerationException("Could not generate account ID for User name: '" + user.getName() + "', on target system: '" + resource.getDisplayName() + "' since the generated value is NULL.");
            }
            
            return accIdAttr.getValueAsString();
        } catch (NoResultFoundException nrfe) {
            throw new AccountIdGenerationException(nrfe.getMessage() + ", Each target system must have at least one attribute which represents an ACCOUNT ID!");
        } catch (NoUserIdentityAttributeFoundException nuiafe) {
            throw new AccountIdGenerationException(nuiafe.getMessage());
        } catch (NoUserIdentityAttributeValueException nuiave) {
            throw new AccountIdGenerationException(nuiave.getMessage());
        } catch (LoadingVirtualAccountAttributeException lvaae) {
            throw new AccountIdGenerationException(lvaae.getMessage());
        }
                /*
                catch (MultipleAttributeValueVaiolation mavv) {
                        throw new AccountIdGenerationException(mavv.getMessage());
                }
                 */
    }
    
    
    
    
    public Object refreshEntity(Object entity) {
        // em.refresh(em.merge(entity));
        Object ob = em.merge(entity);
        em.refresh(ob);
        return ob;
    }
    
    public void flushEntityManager() {
        em.flush();
    }

    @Deprecated
    public Long syncActiveData(String uniqueName) throws TaskCreationException {
        try {
            Resource ts = findResourceByName(uniqueName);
            //JBreturn syncListAction(ts,null);
            return null;
        } catch (NoResultException nre) {
            throw new TaskCreationException(nre.getMessage());
        }
    }
    
    
    public boolean isResourceAssignedToRoles(Resource ts) {
        Query q = em.createNamedQuery("resource.isAssignedToRoles").setParameter("resource", ts);
        Long num = (Long) q.getSingleResult();
        
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }
}
