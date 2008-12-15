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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="VL_REQUEST_USER_COMMENT")
@SequenceGenerator(name="RequestUserCommentIdSeq",sequenceName="REQUEST_USER_COMMENT_ID_SEQ")
public class RequestUserComment extends EntityUserComment {
	
    private static final long serialVersionUID = 1987302492306161413L;
	    
    private Long requestUserCommentId;
    private Request request;
    
    
	/**
	 * @return the requestUserCommentId
	 */
    @Id
    //@GeneratedValue
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestUserCommentIdSeq")
    @Column(name="REQUEST_USER_COMMENT_ID")
	public Long getRequestUserCommentId() {
		return requestUserCommentId;
	}
    
	/**
	 * @param requestUserCommentId the requestUserCommentId to set
	 */
	public void setRequestUserCommentId(Long requestUserCommentId) {
		this.requestUserCommentId = requestUserCommentId;
	}
	
	/**
	 * @return the request
	 */
	@ManyToOne(optional=false)
    @JoinColumn(name="REQUEST_ID")
	public Request getRequest() {
		return request;
	}
	
	
	/**
	 * @param request the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
	}
	
	
	
	
	//ACCESSORS/HELPER
	public static RequestUserComment factory(User commentBy,String comment) {
		RequestUserComment ruc = new RequestUserComment();
		ruc.setCreationDate(new Date());
		ruc.setCommentBy(commentBy);
		ruc.setComment(comment);

		return ruc;
	}

}
