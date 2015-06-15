//$Id: Person.java 10921 2006-12-05 14:39:12Z steve.ebersole@jboss.com $
package org.hibernate.test.propertyref.component.partial;

public class Person {
	private Long id;
	private Identity identity;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Identity getIdentity() {
		return identity;
	}
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
}
