package velo.ejb.utils;

public class JndiLookup {
	public static boolean bootsrapped = false;
	
	public static String getJNDILocalBeanName(String beanName) {
		String result = beanName + "/local";
		
		if (bootsrapped) {
			return result;
		} else {
			result = "velo/" + result;
			return result;
		}
	}
}
