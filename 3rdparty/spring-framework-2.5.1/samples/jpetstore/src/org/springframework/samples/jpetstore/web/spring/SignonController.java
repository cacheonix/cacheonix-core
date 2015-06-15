package org.springframework.samples.jpetstore.web.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Account;
import org.springframework.samples.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class SignonController implements Controller {

	private PetStoreFacade petStore;

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		Account account = this.petStore.getAccount(username, password);
		if (account == null) {
			return new ModelAndView("Error", "message", "Invalid username or password.  Signon failed.");
		}
		else {
			UserSession userSession = new UserSession(account);
			PagedListHolder myList = new PagedListHolder(this.petStore.getProductListByCategory(account.getFavouriteCategoryId()));
			myList.setPageSize(4);
			userSession.setMyList(myList);
			request.getSession().setAttribute("userSession", userSession);
			String forwardAction = request.getParameter("forwardAction");
			if (forwardAction != null) {
				response.sendRedirect(forwardAction);
				return null;
			}
			else {
				return new ModelAndView("index");
			}
		}
	}

}
