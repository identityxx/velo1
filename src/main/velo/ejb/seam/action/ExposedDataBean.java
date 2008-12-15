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
package velo.ejb.seam.action;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.UserManagerLocal;

@Stateful
@Name("exposedData")
public class ExposedDataBean implements ExposedData {
	@In
	EntityManager entityManager;
	
	@EJB
	public UserManagerLocal userManager;
	
	@Logger
	private Log log;
	
	
	public Map<String,Long> getLastYearUserAmountCreation() {
		Calendar c = Calendar.getInstance();
		String[] monthName = {
			"Jan", "Feb","Mar", "Apr", "May",
			"Jun", "Jul","Aug", "Sep", "Oct", 
			"Nov","Dec"
		};
		
		
		//move the calendar to 12 months back
		c.add(Calendar.MONTH, -11);

		//build dates(1st day) of the last 12 months
		Set<Date> yearlyMonths = new LinkedHashSet<Date>();
		log.debug("Building months(1 year) before current month...");
		Calendar tempCal = Calendar.getInstance();
		tempCal.add(Calendar.MONTH, -11);
		for (int i=0;i<=11;i++) {
			log.trace("Adding monthly date obj: #0",c.getTime());
//			yearlyMonths.add(c.getTime());
			
			tempCal.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
			yearlyMonths.add(tempCal.getTime());
			tempCal.add(Calendar.MONTH,1);
			//c.add(Calendar.MONTH, 1);
		}
		

		//iterate over months, get the amount of users created per month
		Map<String,Long> map = new LinkedHashMap<String,Long>();
		for (Date currFirstDayOfMonth : yearlyMonths) {
			c.setTime(currFirstDayOfMonth);
			c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
			
			log.trace("Curr first day of month: " + currFirstDayOfMonth + ", to: " + c.getTime());
			Long amount = userManager.getCreatedUsersAmount(currFirstDayOfMonth,c.getTime());
			log.trace("Adding month #0(#1) with users created amount #2", monthName[c.get(Calendar.MONTH)],c.get(Calendar.YEAR), amount);
			map.put(monthName[c.get(Calendar.MONTH)], amount);
		}
		
		
		for (Map.Entry<String,Long> rr : map.entrySet()) {
			System.out.println("!!!!!!!!: " + rr.getKey() + " : " + rr.getValue());
		}
		
		return map;
	}
	
	@Destroy
	@Remove
	public void destroy() {
	}
}
