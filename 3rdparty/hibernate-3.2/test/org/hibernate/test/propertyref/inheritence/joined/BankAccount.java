//$Id: BankAccount.java 10921 2006-12-05 14:39:12Z steve.ebersole@jboss.com $
package org.hibernate.test.propertyref.inheritence.joined;

public class BankAccount extends Account {
	private String accountNumber;
	private String bsb;

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getBsb() {
		return bsb;
	}

	public void setBsb(String bsb) {
		this.bsb = bsb;
	}
}
