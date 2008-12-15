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
package velo.ejb.seam;

import java.sql.Blob;
import java.util.Date;

import org.hibernate.Hibernate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

import velo.ejb.impl.WorkflowBean;
import velo.entity.WorkflowProcessDef;
import velo.exceptions.OperationException;

@Name("workflowProcessDefHome")
public class WorkflowProcessDefHome extends EntityHome<WorkflowProcessDef> {

	@Logger
	private Log log;
	
	@In
	FacesMessages facesMessages;
	
	@In(create=true)
	WorkflowBean workflowManager;
	
	private byte[] processUploadedFile;
	private String processContentType;
	private String processFileName;
	
	private byte[] imageUploadedFile;
	private String imageContentType;
	private String imageFileName;
	
	
	public void setWorkflowProcessDefId(Long id) {
		setId(id);
	}

	public Long getWorkflowProcessDefId() {
		return (Long) getId();
	}

	@Override
	protected WorkflowProcessDef createInstance() {
		WorkflowProcessDef workflowProcessDef = new WorkflowProcessDef();
		return workflowProcessDef;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public WorkflowProcessDef getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	@Override
	public String persist() {
		getEntityManager().flush();
		String fileContent = new String(getProcessUploadedFile());
		log.trace("File name: #0, Content Type: #1",getProcessFileName(),getProcessContentType());
		log.trace("-START- of dumping process xml content");
		log.trace(fileContent);
		log.trace("-END- of dumping process xml content");
		
		try {
			workflowManager.deployProcessDefinition(fileContent, getInstance());
			getInstance().setCreationDate(new Date());
			Blob b = Hibernate.createBlob(getImageUploadedFile());
			getInstance().setImage(b);
			
			return super.persist();
		}catch (OperationException e) {
			log.error("Could not persist a new workflow process definition due to: " + e.getMessage());
			facesMessages.add("Could not perform operation due to: " + e.getMessage());
			
			return null;
		}
	}
	
	@Override
	public String update() {
		getEntityManager().flush();
		String fileContent = new String(getProcessUploadedFile());
		log.trace("File name: #0, Content Type: #1",getProcessFileName(),getProcessContentType());
		log.trace("-START- of dumping process xml content");
		log.trace(fileContent);
		log.trace("-END- of dumping process xml content");
		
		try {
			//workflowManager.updateProcessDefinition(fileContent, getInstance());
			workflowManager.deployProcessDefinition(fileContent, getInstance());
			getInstance().setLastUpdateDate(new Date());
			Blob b = Hibernate.createBlob(getImageUploadedFile());
			getInstance().setImage(b);
			return super.update();
		}catch (OperationException e) {
			log.error("Could not persist a new workflow process definition due to: " + e.getMessage());
			facesMessages.add("Could not perform operation due to: " + e.getMessage());
			
			return null;
		}
	}

	public String remove() {
		try {
			workflowManager.undeployProcessDefinition(getInstance());
			return super.remove();
		}catch (OperationException e) {
			facesMessages.add("Could not perform operation due to: " + e.getMessage());
			return null;
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @return the processUploadedFile
	 */
	public byte[] getProcessUploadedFile() {
		return processUploadedFile;
	}

	/**
	 * @param processUploadedFile the processUploadedFile to set
	 */
	public void setProcessUploadedFile(byte[] processUploadedFile) {
		this.processUploadedFile = processUploadedFile;
	}

	/**
	 * @return the processContentType
	 */
	public String getProcessContentType() {
		return processContentType;
	}

	/**
	 * @param processContentType the processContentType to set
	 */
	public void setProcessContentType(String processContentType) {
		this.processContentType = processContentType;
	}

	/**
	 * @return the processFileName
	 */
	public String getProcessFileName() {
		return processFileName;
	}

	/**
	 * @param processFileName the processFileName to set
	 */
	public void setProcessFileName(String processFileName) {
		this.processFileName = processFileName;
	}

	/**
	 * @return the imageUploadedFile
	 */
	public byte[] getImageUploadedFile() {
		return imageUploadedFile;
	}

	/**
	 * @param imageUploadedFile the imageUploadedFile to set
	 */
	public void setImageUploadedFile(byte[] imageUploadedFile) {
		this.imageUploadedFile = imageUploadedFile;
	}

	/**
	 * @return the imageContentType
	 */
	public String getImageContentType() {
		return imageContentType;
	}

	/**
	 * @param imageContentType the imageContentType to set
	 */
	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	/**
	 * @return the imageFileName
	 */
	public String getImageFileName() {
		return imageFileName;
	}

	/**
	 * @param imageFileName the imageFileName to set
	 */
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
	
	
}
