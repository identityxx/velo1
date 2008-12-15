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
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An entity that represents a TaskLog entry
 * 
 * @author Asaf Shakarchi
 */
@MappedSuperclass
public abstract class EntityLog implements Serializable { 

	private static final long serialVersionUID = 1987302492306161413L;
	
	private Date creationDate;
	private String severity;
	private String summaryMessage;
	private String detailedMessage;


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
	 * @param severity The severity to set.
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @return Returns the severity.
	 */
	@Column(name="SEVERITY")
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param summaryMessage The summaryMessage to set.
	 */
	public void setSummaryMessage(String summaryMessage) {
		this.summaryMessage = summaryMessage;
	}

	/**
	 * @return Returns the summaryMessage.
	 */
	@Column(name="SUMMARY_MESSAGE")
	@Lob
	public String getSummaryMessage() {
		return summaryMessage;
	}

	/**
	 * @param detailedMessage The detailedMessage to set.
	 */
	public void setDetailedMessage(String detailedMessage) {
		this.detailedMessage = detailedMessage;
	}

	/**
	 * @return Returns the detailedMessage.
	 */
	@Column(name="DETAILED_MESSAGE")
	@Lob
	public String getDetailedMessage() {
		return detailedMessage;
	}

}
