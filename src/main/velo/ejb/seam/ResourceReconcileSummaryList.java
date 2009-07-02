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

import velo.entity.ReconcileProcessSummary;

@Name("resourceReconcileSummaryList")
public class ResourceReconcileSummaryList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(reconcileProcessSummary.reconcileProcessSummaryId) = #{resourceReconcileSummaryList.reconcileProcessSummary.reconcileProcessSummaryId}",
			"reconcileProcessSummary.processType = #{resourceReconcileSummaryList.reconcileProcessSummary.processType}",
			"reconcileProcessSummary.startDate >= #{resourceReconcileSummaryList.reconcileProcessSummary.fromStartDate}",
			"reconcileProcessSummary.endDate >= #{resourceReconcileSummaryList.reconcileProcessSummary.toEndDate}",
	};

	private ReconcileProcessSummary reconcileProcessSummary = new ReconcileProcessSummary();
	private Integer amount;

	private static final String EJBQL = "select rps from ReconcileProcessSummary rps ORDER BY reconcileProcessSummaryId DESC";
	
	@Override
	public Integer getMaxResults() {
		if (getAmount() != null) {
			if (getAmount() != 0) {
				return getAmount();
			}
		}
		
		return 25;
	}

	public ReconcileProcessSummary getReconcileProcessSummary() {
		return reconcileProcessSummary;
	}

	
	//ACCESSORS
	/**
	 * @return the amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	
	public List getResultList() {
		/*
		List<ResourceReconcileSummary> rrs = new ArrayList<ResourceReconcileSummary>();
		for (Object currObj : super.getResultList()) {
			ResourceReconcileSummaryEntity currRRSE = (ResourceReconcileSummaryEntity)currObj;
			
			Object unmarshalledObj = currRRSE.unmarshal();
			rrs.add((ResourceReconcileSummary)unmarshalledObj);
		}
		
		
		return rrs;
		*/
		return super.getResultList();
	}
	
	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
}
