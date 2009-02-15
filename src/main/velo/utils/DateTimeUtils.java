package velo.utils;

import java.util.Calendar;
import java.util.Date;

import org.jboss.seam.annotations.Name;

@Name("dateTimeUtils")
public class DateTimeUtils {
	public Date getIncrementDateByDays(Integer days) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, days);
		
		return c.getTime();
	}
}
