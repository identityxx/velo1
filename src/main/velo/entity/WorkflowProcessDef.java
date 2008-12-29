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
package velo.entity;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

@Table(name = "VL_WORKFLOW_PROCESS_DEF")
@Entity
@SequenceGenerator(name="WorkflowProcessDefIdSeq",sequenceName="WORKFLOW_PROCESS_DEF_ID_SEQ")
public class WorkflowProcessDef extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	//private static Logger log = Logger.getLogger(WorkflowProcessDef.class.getName());
	
	private Long workflowProcessDefId;
	private String uniqueName;
	private String description;
	private Integer version;
	private Blob image;
	private String processDefEngineKey;
	
	
	/**
	 * @return the workflowProcessDefId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="WorkflowProcessDefIdSeq")
	@Column(name = "WORKFLOW_PROCESS_DEF_ID")
	public Long getWorkflowProcessDefId() {
		return workflowProcessDefId;
	}
	
	/**
	 * @param workflowProcessDefId the workflowProcessDefId to set
	 */
	public void setWorkflowProcessDefId(Long workflowProcessDefId) {
		this.workflowProcessDefId = workflowProcessDefId;
	}
	
	/**
	 * @return the uniqueName
	 */
	@Column(name = "UNIQUE_NAME")
	@NotNull
	public String getUniqueName() {
		return uniqueName;
	}
	
	/**
	 * @param uniqueName the uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	
	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION")
	@NotNull
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the processDefEngineKey
	 */
	@Column(name = "PROCESS_DEF_ENGINE_KEY")
	@NotNull
	public String getProcessDefEngineKey() {
		return processDefEngineKey;
	}
	
	/**
	 * @param processDefEngineKey the processDefEngineKey to set
	 */
	public void setProcessDefEngineKey(String processDefEngineKey) {
		this.processDefEngineKey = processDefEngineKey;
	}

	/**
	 * @return the version
	 */
	@Column(name="VERSION")
	@NotNull
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * @return the image
	 */
	@Lob
	@Column(name="IMAGE")
	@NotNull
	public Blob getImage() {
		return image;
	}
	
	/**
	 * @param image the image to set
	 */
	public void setImage(Blob image) {
		this.image = image;
	}
	
	
	
	
	
	@Transient
	public byte[] getImageAsBytes() {
		try {
			return getImage().getBytes(1, (int) getImage().length());
		}catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
