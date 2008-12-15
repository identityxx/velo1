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
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.User;

@Name("selfServiceMyRequestedRequestList")
public class SelfServiceMyRequestedRequestList extends EntityQuery {

	private static final String[] RESTRICTIONS = {};

	public User requester;
	
	@Override
	public String getEjbql() {
		return "select request from Request request where request.requester.userId = " + requester.getUserId() + " order by request.requestId DESC";
	}

	@Override
	public Integer getMaxResults() {
		return 5;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}
}
