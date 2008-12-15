package velo.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generic {
	 //TODO: Implement via regexp!
	//sucks, this expression allows invalid chars as well (\\S should be replaced I guess)
	public static boolean isEmailValid(String email) {
		if (!isNotEmptyAndNotNull(email)) {
			return false;
		}
		
		/* Set up a pattern of the form non-spaces, an @, non-spaces */
		Pattern emailPattern = Pattern.compile("^\\S+@\\S+$");
	    	
		Matcher fit = emailPattern.matcher(email);
		return fit.matches();
	}
	
	public static boolean isNotEmptyAndNotNull(String str) {
		if (str != null) {
			return str.length() > 0; 
		}
		
		return false;
	}
	
	
	public static void main(String args[]) {
		System.out.println(Generic.isEmailValid(null));
	}
}
