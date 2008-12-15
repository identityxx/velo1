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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="VL_FORM")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(name="FormIdSeq",sequenceName="FORM_ID_SEQ")
//@DiscriminatorColumn(name="TYPE")
//@MappedSuperclass
public class Form extends BaseEntity implements Serializable {

	private Long formId;
	
	private Request request;
	
	private Long key;
	

	/**
	 * @return the formId
	 */
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="FormIdSeq")
    @Column(name="FORM_ID")
	public Long getFormId() {
		return formId;
	}

	/**
	 * @param formId the formId to set
	 */
	public void setFormId(Long formId) {
		this.formId = formId;
	}

	/**
	 * @return the request
	 */
	@ManyToOne(optional=true)
    @JoinColumn(name="REQUEST_ID", nullable=true)
	public Request getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
	}

	//key is a unique name in mysql
	@Column(name="FORM_KEY")
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
}
