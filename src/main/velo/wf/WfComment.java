package velo.wf;

import java.io.Serializable;
import java.util.Date;

public class WfComment implements Serializable {
	public String comment;
	public Date date;
	public String fullName;
	public String userName;
	public String extra1;
	public String extra2;
	public String extra3;
	public String extra4;
	public String extra5;
	
	
	public WfComment() {
		
	}
	
	public WfComment(Date date, String comment, String fullName, String userName, String extra1, String extra2, String extra3, String extra4, String extra5) {
		this.date = date;
		this.comment = comment;
		this.fullName = fullName;
		this.userName = userName;
		this.extra1 = extra1;
		this.extra2 = extra2;
		this.extra3 = extra3;
		this.extra4 = extra4;
		this.extra5 = extra5;
	}
	
	public WfComment(Date date, String comment, String fullName, String userName) {
		this.date = date;
		this.comment = comment;
		this.fullName = fullName;
		this.userName = userName;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getExtra1() {
		return extra1;
	}
	public void setExtra1(String extra1) {
		this.extra1 = extra1;
	}
	public String getExtra2() {
		return extra2;
	}
	public void setExtra2(String extra2) {
		this.extra2 = extra2;
	}
	public String getExtra3() {
		return extra3;
	}
	public void setExtra3(String extra3) {
		this.extra3 = extra3;
	}
	public String getExtra4() {
		return extra4;
	}
	public void setExtra4(String extra4) {
		this.extra4 = extra4;
	}
	public String getExtra5() {
		return extra5;
	}
	public void setExtra5(String extra5) {
		this.extra5 = extra5;
	}
	
	
	public String toString() {
		return "Date: " + getDate() + ", FullName: " + getFullName() + ", Username: " + getUserName() + ", Comment: " + getComment() + ", Extra1: " + getExtra1() + ", Extra2: " + getExtra2();
	}
}
