package org.springframework.samples.jpetstore.web.spring;

import java.io.Serializable;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Account;

/**
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class UserSession implements Serializable {

	private Account account;

	private PagedListHolder myList;

	public UserSession(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}

	public void setMyList(PagedListHolder myList) {
		this.myList = myList;
	}

	public PagedListHolder getMyList() {
		return myList;
	}

}
