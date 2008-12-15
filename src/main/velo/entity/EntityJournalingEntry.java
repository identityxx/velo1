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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.validator.NotNull;

import velo.entity.UserJournalingEntry.UserJournalingActionType;

@Table(name = "VL_ENTITY_JOURNALING")
@Entity
@SequenceGenerator(name="EntityJournalingIdSeq",sequenceName="ENTITY_JOURNALING_ID_SEQ")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ENTITY_TYPE")
public class EntityJournalingEntry extends BaseEntity {
	private static final long serialVersionUID = 1L;
	private static transient Logger log = Logger.getLogger(EntityJournalingEntry.class.getName());

	private Long entityJournalingId;
	private String summaryMessage;
	private String detailedMessage;
	private User performedBy;
	
	public EntityJournalingEntry() {
		
	}
	
	public EntityJournalingEntry(User performedBy, String summaryMessage, String detailedMessage) {
		setPerformedBy(performedBy);
		setSummaryMessage(summaryMessage);
		setDetailedMessage(detailedMessage);
		setCreationDate(new Date());
	}
	
	/**
	 * @return the entityJournalingId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="EntityJournalingIdSeq")
	@Column(name = "ENTITY_JOURNALING_ID")
	public Long getEntityJournalingId() {
		return entityJournalingId;
	}
	
	/**
	 * @param entityJournalingId the entityJournalingId to set
	 */
	public void setEntityJournalingId(Long entityJournalingId) {
		this.entityJournalingId = entityJournalingId;
	}
	
	/**
	 * @return the summaryMessage
	 */
	@Column(name="SUMMARY_MESSAGE", nullable=false)
	@NotNull
	public String getSummaryMessage() {
		return summaryMessage;
	}
	
	/**
	 * @param summaryMessage the summaryMessage to set
	 */
	public void setSummaryMessage(String summaryMessage) {
		this.summaryMessage = summaryMessage;
	}
	
	/**
	 * @return the detailedMessage
	 */
	@Column(name="DETAILED_MESSAGE", nullable=true)
	public String getDetailedMessage() {
		return detailedMessage;
	}
	
	/**
	 * @param detailedMessage the detailedMessage to set
	 */
	public void setDetailedMessage(String detailedMessage) {
		this.detailedMessage = detailedMessage;
	}
	
	/**
	 * @return the performedBy
	 */
	@ManyToOne()
	@JoinColumn(name = "PERFORMED_BY_USER_ID", nullable = true)
	public User getPerformedBy() {
		return performedBy;
	}
	
	/**
	 * @param performedBy the performedBy to set
	 */
	public void setPerformedBy(User performedBy) {
		this.performedBy = performedBy;
	}
}
