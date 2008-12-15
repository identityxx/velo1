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
package velo.ejb.seam.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.configuration.ConfigurationException;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.seam.ResourceHome;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.exceptions.OperationException;

@Stateful
@Name("resourceActions")
public class ResourceActionsBean implements ResourceActions {
	@PersistenceContext
	public EntityManager em;
	
	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@In(create=true)
	ResourceHome resourceHome;

	/**
	 * Inject a local Resource ejb
	 */
	@EJB
	public ResourceManagerLocal resourceManager;
	
	@EJB
	public ResourceOperationsManagerLocal resourceOperationsManager;


	public void testConnectivity() {
		
		try {
			resourceOperationsManager.testConnectivity(resourceHome.getInstance());
			
			facesMessages
			.add(FacesMessage.SEVERITY_INFO,
					"Successfully connected to resource: #{resourceHome.instance.displayName}");
			
			/*
			Adapter adapter = resourceManager.adapterFactory(resourceHome.getInstance());
			adapter.connect();
			if (adapter.isConnected()) {
				facesMessages
						.add(FacesMessage.SEVERITY_INFO,
								"Successfully connected to target system: #{resourceHome.instance.displayName}");
			}
			*/
		} catch (OperationException e) {
			FacesMessages
					.instance()
					.add(FacesMessage.SEVERITY_ERROR,e.getMessage());
		}
	}
	
	
	
	//Conf editing
	public String getConfigurationFileName() {
		return resourceHome.getInstance().getConfigurationFilename();
	}
	
	public void updateResourceCustomConfiguration() {
		try {
			log.debug("START: SAVE XML METHOD");
			javax.faces.component.UIViewRoot viewRoot = javax.faces.context.FacesContext
					.getCurrentInstance().getViewRoot();
			java.util.List<javax.faces.component.UIComponent> components = viewRoot
					.getChildren();

			javax.faces.component.UIComponent form = viewRoot.findComponent("xmlResourceCustomConfigurationForm");

			javax.faces.component.UIComponent mainXmlConf = form.findComponent("xmlManager");
			velo.uiComponents.XMLManager xmlManager = (velo.uiComponents.XMLManager) mainXmlConf;
			
			String xmlConfContent = xmlManager.saveDataByContent();
			resourceHome.getInstance().setConfiguration(xmlConfContent);
			
			//NPE
			/*
			if (xmlManager.getType().equalsIgnoreCase("file")) {
				xmlManager.saveDataByFile();
			}
			else if (xmlManager.getType().equalsIgnoreCase("xmlContent")) {
				xmlManager.saveDataByContent();
			}
			else {
				//TODO: Log unsupported/throw an error
			}
			*/
			
			log.debug("END: SAVE XML METHOD");
			facesMessages.add("Successfully custom configuration properties of target: #{resourceHome.instance.displayName}");
		} catch (ConfigurationException ex) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,ex.toString());
		}
	}
	
	
	
	
	
	@Factory("guiResourceAttributes")
	public List<ResourceAttribute> getGuiResourceAttributes() {
		Collection<Resource> resources = resourceManager.findAllActiveResources();

		List<ResourceAttribute> raList = new ArrayList<ResourceAttribute>();
		for(Resource currRes : resources) {
			raList.addAll(currRes.getResourceAttributes());
		}
		
		
		return raList;
	}
	

	@Destroy
	@Remove
	public void destroy() {
	}

}
