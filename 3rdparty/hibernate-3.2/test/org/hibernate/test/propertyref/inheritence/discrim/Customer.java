//$Id: Customer.java 10921 2006-12-05 14:39:12Z steve.ebersole@jboss.com $
package org.hibernate.test.propertyref.inheritence.discrim;

/**
 * @author Gavin King
 */
public class Customer extends Person {
	private String customerId;

	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
}
