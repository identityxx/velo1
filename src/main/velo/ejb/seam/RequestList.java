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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.Request;
import velo.entity.Request.RequestStatus;

@Name("requestList")
public class RequestList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(request.requestId) = #{requestList.request.requestId}",
			"lower(request.notes) like concat(lower(#{requestList.request.notes}),'%')",
			"request.creationDate > #{requestList.request.fromCreationDate}",
			"request.creationDate < #{requestList.request.toCreationDate}",
			"request.status = #{requestList.request.status}"};
			//"lower(request.creationDate) = concat(lower(#{requestList.request.creationDate}),'%')",};

	private Request request = new Request();
	
	
	//private static final String EJBQL = "select request from Request request";
	
	@Override
	public String getEjbql() {
		if (super.getEjbql() == null) {
			return "select request from Request request";
		} else {
			return super.getEjbql();
		}
	}
	
	private Map<Request, Boolean> requestSelection = new HashMap<Request, Boolean>();
	
	
	
	
	@PostConstruct
    public void initialize() {
		setOrder("requestId DESC");
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(getEjbql());
    }
	
	
	
	
	
	/*
	@Override
	public Integer getMaxResults() {
		return 25;
	}
	*/
	
	@Override
	public Integer getMaxResults() {
		if (super.getMaxResults() != null) {
			return super.getMaxResults();
		}
		else {
			return 25;
		}
	}

	public Request getRequest() {
		return request;
	}

	@Factory("requestStatuses")
	public RequestStatus[] getRequestStatuses() {
		return RequestStatus.values();
	}
	
	public Map<Request, Boolean> getRequestSelection() {
		return requestSelection;
	}
}
