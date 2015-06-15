package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewCartAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    CartActionForm cartForm = (CartActionForm) form;
    AccountActionForm acctForm = (AccountActionForm) request.getSession().getAttribute("accountForm");
    String page = request.getParameter("page");
    if (acctForm != null && acctForm.getAccount() != null) {
      if ("next".equals(page)) {
        acctForm.getMyList().nextPage();
      }
			else if ("previous".equals(page)) {
        acctForm.getMyList().previousPage();
      }
    }
    if ("nextCart".equals(page)) {
      cartForm.getCart().getCartItemList().nextPage();
    }
		else if ("previousCart".equals(page)) {
      cartForm.getCart().getCartItemList().previousPage();
    }
    return mapping.findForward("success");
  }

}
