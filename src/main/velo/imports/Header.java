package velo.imports;

import java.util.Date;

public class Header {
	private String resourceUniqueName;
	private Date creationDate;
	private String fetchType;
	
	public String getResourceUniqueName() {
		return resourceUniqueName;
	}
	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getFetchType() {
		return fetchType;
	}
	public void setFetchType(String fetchType) {
		this.fetchType = fetchType;
	}
}
