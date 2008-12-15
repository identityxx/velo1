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
import javax.persistence.MappedSuperclass;


/**
 * An abstract class that is the base of all approvers attached to any entity (i.e role,resource,groups,etc..)
 * 
 * @author Asaf Shakarchi
 */
@MappedSuperclass
public class EntityApprovers extends BaseEntity {

	private Integer approvalLevel = 1;
	
	private String description;
	
	/**
	 * @return the approvalLevel
	 */
	@Column(name="APPROVAL_LEVEL", nullable=false)
	public Integer getApprovalLevel() {
		return approvalLevel;
	}
	/**
	 * @param approvalLevel the approvalLevel to set
	 */
	public void setApprovalLevel(Integer approvalLevel) {
		this.approvalLevel = approvalLevel;
	}
	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION")
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
}
