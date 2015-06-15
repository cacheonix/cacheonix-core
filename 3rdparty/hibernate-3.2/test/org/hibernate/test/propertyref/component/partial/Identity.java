//$Id: Identity.java 10921 2006-12-05 14:39:12Z steve.ebersole@jboss.com $
package org.hibernate.test.propertyref.component.partial;

public class Identity {
	private String name;
	private String ssn;
	
	public String getSsn() {
		return ssn;
	}
	public void setSsn(String id) {
		this.ssn = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
