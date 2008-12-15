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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "VL_RESOURCE_GLOBAL_OPER")
@SequenceGenerator(name="ResourceGlobalOperationIdSeq",sequenceName="RESOURCE_GLOBAL_OPER_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "resourceGlobalOperation.findByUniqueName",query = "SELECT object(rgo) FROM ResourceGlobalOperation rgo WHERE rgo.uniqueName = :uniqueName")
})
public class ResourceGlobalOperation extends ExtBasicEntity {
	private Long resourceGlobalOperationId;
	private boolean must;

	public ResourceGlobalOperation() {

	}

	public ResourceGlobalOperation(String uniqueName, String displayName,
			String description, boolean must) {
		super(uniqueName, displayName, description);
		setMust(must);
	}
	
	
	/**
	 * @return the resourceGlobalOperationId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceGlobalOperationIdSeq")
	//@GeneratedValue
	@Column(name = "RESOURCE_GLOBAL_OPER_ID")
	public Long getresourceGlobalOperationId() {
		return resourceGlobalOperationId;
	}

	/**
	 * @param resourceGlobalOperationId the resourceGlobalOperationId to set
	 */
	public void setresourceGlobalOperationId(Long resourceGlobalOperationId) {
		this.resourceGlobalOperationId = resourceGlobalOperationId;
	}


	/**
	 * @return the must
	 */
	@Column(name = "MUST", nullable=false)
	public boolean isMust() {
		return must;
	}

	/**
	 * @param must
	 *            the must to set
	 */
	public void setMust(boolean must) {
		this.must = must;
	}

}
