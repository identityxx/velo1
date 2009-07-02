package velo.collections;

import java.util.Date;
import java.util.HashMap;

import velo.entity.Attribute;

public class Attributes extends HashMap<String,Attribute> {
	public void addAttribute(Attribute attr) {
		System.out.println("@@@@Adding attribute name '" + attr.getUniqueName() +"' to map!");
		//put(attr.getUniqueName(),attr);
		put(String.valueOf(new Date().getTime()), attr);
	}
}
