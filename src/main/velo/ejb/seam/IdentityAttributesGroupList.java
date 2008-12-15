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

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.IdentityAttributesGroup;

@Name("identityAttributesGroupList")
public class IdentityAttributesGroupList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(identityAttributesGroup.name) like concat(lower(#{identityAttributesGroupList.identityAttributesGroup.name}),'%')",
			"lower(identityAttributesGroup.description) like concat(lower(#{identityAttributesGroupList.identityAttributesGroup.description}),'%')",};

	private IdentityAttributesGroup identityAttributesGroup = new IdentityAttributesGroup();

	private static final String EJBQL = "select identityAttributesGroup from IdentityAttributesGroup identityAttributesGroup";
	
	
	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public IdentityAttributesGroup getIdentityAttributesGroup() {
		return identityAttributesGroup;
	}


	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
}
