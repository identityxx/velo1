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
//@!@clean
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents an Email Template
 * 
 * @author Asaf Shakarchi
 */

// Seam annotations
@Name("actionLanguage")
@Table(name = "VL_ACTION_LANGUAGE")
@SequenceGenerator(name="ActionLanguageIdSeq",sequenceName="ACTION_LANGUAGE_ID_SEQ")
@Entity
public class ActionLanguage extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1987302492306161423L;

	private Long actionLanguageId;
	private String name;
	private String description;
	

	/**
	 * @param actionLanguageId
	 *            the actionLanguageId to set
	 */
	public void setActionLanguageId(Long actionLanguageId) {
		this.actionLanguageId = actionLanguageId;
	}

	/**
	 * @return the actionLanguageId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ActionLanguageIdSeq")
	//@GeneratedValue
	@Column(name = "ACTION_LANGUAGE_ID")
	public Long getActionLanguageId() {
		return actionLanguageId;
	}

	/**
	 * Set the name of the role
	 * 
	 * @param name
	 *            The name of the role to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the role
	 * 
	 * @return The name of the role
	 */
	@Column(name = "NAME", nullable = false)
	@NotNull
	public String getName() {
		return name;
	}

	/**
	 * Set the description of the entity
	 * 
	 * @param description
	 *            The description string to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the description of the entity
	 * 
	 * @return The description of the entity
	 */
	@Column(name = "DESCRIPTION", nullable = false)
	public String getDescription() {
		return description;
	}


	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ActionLanguage) {
			ActionLanguage that = (ActionLanguage) other;
			return this.actionLanguageId.longValue() == that.actionLanguageId
					.longValue();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (actionLanguageId == null)
			return super.hashCode();
		return actionLanguageId.hashCode();
	}

	public void copyValues(Object entity) {
		ActionLanguage al = (ActionLanguage) entity;
		this.setDescription(al.getDescription());
		this.setName(al.getName());
	}
}
