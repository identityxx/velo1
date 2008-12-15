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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * An entity that represents a TaskLog entry
 * 
 * @author Asaf Shakarchi
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable { 

	private static final long serialVersionUID = 1987302492306161413L;
	
	private Date creationDate;
	
	private Date lastUpdateDate;
	
	/**
	 * Whether the entity was loaded by the JPA or not. 
	 */
	private boolean isLoaded;

	
	
	//transients (used usually for search criteria)
	private Date fromCreationDate;
	private Date toCreationDate;
	
	

	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return Returns the creationDate.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * @return the lastUpdateDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_UPDATE_DATE")
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	/**
	 * @param lastUpdateDate the lastUpdateDate to set
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	/**
	 * @param isLoaded the isLoaded to set
	 */
	@Transient
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	/**
	 * @return the isLoaded
	 */
	@Transient
	public boolean isLoaded() {
		return isLoaded;
	}

	public void copyValues(Object entity) {
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @return the fromCreationDate
	 */
	@Transient
	public Date getFromCreationDate() {
		return fromCreationDate;
	}

	/**
	 * @param fromCreationDate the fromCreationDate to set
	 */
	public void setFromCreationDate(Date fromCreationDate) {
		this.fromCreationDate = fromCreationDate;
	}

	/**
	 * @return the toCreationDate
	 */
	@Transient
	public Date getToCreationDate() {
		return toCreationDate;
	}

	/**
	 * @param toCreationDate the toCreationDate to set
	 */
	public void setToCreationDate(Date toCreationDate) {
		this.toCreationDate = toCreationDate;
	}
}
