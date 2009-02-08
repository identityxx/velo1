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

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.ActionRule;

@Name("ruleList")
public class RuleList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(rule.actionDefinitionId) = #{ruleList.rule.actionDefinitionId}"
			};

	private ActionRule rule = new ActionRule();
	
	
	@Override
	public String getEjbql() {
		if (super.getEjbql() == null) {
			return "select rule from ActionRule rule";
		} else {
			return super.getEjbql();
		}
	}
	
	@PostConstruct
    public void initialize() {
		setOrder("actionDefinitionId DESC");
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

	public ActionRule getRule() {
		return rule;
	}
}
