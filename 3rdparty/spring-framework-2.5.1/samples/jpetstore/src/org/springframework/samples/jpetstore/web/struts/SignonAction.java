package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Account;

public class SignonAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.getSession().removeAttribute("workingAccountForm");
    request.getSession().removeAttribute("accountForm");
    if (request.getParameter("signoff") != null) {
      request.getSession().invalidate();
      return mapping.findForward("success");
    }
		else {
      AccountActionForm acctForm = (AccountActionForm) form;
      String username = acctForm.getUsername();
      String password = acctForm.getPassword();
      Account account = getPetStore().getAccount(username, password);
      if (account == null) {
        request.setAttribute("message", "Invalid username or password.  Signon failed.");
        return mapping.findForward("failure");
      }
			else {
				String forwardAction = acctForm.getForwardAction();
				acctForm = new AccountActionForm();
				acctForm.setForwardAction(forwardAction);
        acctForm.setAccount(account);
        acctForm.getAccount().setPassword(null);
        PagedListHolder myList = new PagedListHolder(getPetStore().getProductListByCategory(account.getFavouriteCategoryId()));
				myList.setPageSize(4);
				acctForm.setMyList(myList);
				request.getSession().setAttribute("accountForm", acctForm);
        if (acctForm.getForwardAction() == null || acctForm.getForwardAction().length() < 1) {
          return mapping.findForward("success");
        }
				else {
          response.sendRedirect(acctForm.getForwardAction());
          return null;
        }
      }
    }
  }

}
