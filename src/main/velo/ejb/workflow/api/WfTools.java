package velo.ejb.workflow.api;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("wfTools")
@AutoCreate
public class WfTools {
	@Logger
	Log log;

	public boolean isEqual(String str1, String str2) {
		boolean result = str1.equals(str2);
		
		log.debug("Is String '#0' equals string '#1'?: #2",str1,str2,result);
		return result;
	}
	
	public boolean isEqualIgnoreCase(String str1, String str2) {
		boolean result = str1.equalsIgnoreCase(str2);
		
		log.debug("Is String '#0' equals string '#1'? (Ignoring case!): #2",str1,str2,result);
		return result;
	}
	
	public boolean isEmpty(String str) {
		log.debug("Is value '#0' empty?", str);
		
		if (str == null) {
			log.debug("Value is null, returning true.");
			return true;
		} else {
			if (str.length() < 1) {
				log.debug("Value is empty, returning true.");
				return true;
			} else {
				log.debug("Value is NOT null, returning false.");
				return false;
			}
		}
	}
	
	public boolean isNotEmpty(String str) {
		log.debug("Is value '#0' NOT empty?", str);
		
		if (str == null) {
			log.debug("Value is null, returning false.");
			return false;
		} else {
			if (str.length() < 1) {
				log.debug("Value is null, returning false.");
				return false;
			} else {
				log.debug("Value is NOT null, returning true.");
				return true;
			}
		}
	}
}
