package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.Account;

public class NewOrderFormAction extends SecureBaseAction {

  protected ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    AccountActionForm acctForm = (AccountActionForm) request.getSession().getAttribute("accountForm");
    CartActionForm cartForm = (CartActionForm) request.getSession().getAttribute("cartForm");
    if (cartForm != null) {
      OrderActionForm orderForm = (OrderActionForm) form;
      // Re-read account from DB at team's request.
      Account account = getPetStore().getAccount(acctForm.getAccount().getUsername());
      orderForm.getOrder().initOrder(account, cartForm.getCart());
      return mapping.findForward("success");
    }
		else {
      request.setAttribute("message", "An order could not be created because a cart could not be found.");
      return mapping.findForward("failure");
    }
  }

}