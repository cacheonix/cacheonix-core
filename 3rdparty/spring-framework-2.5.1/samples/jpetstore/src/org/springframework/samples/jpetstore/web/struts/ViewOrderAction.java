package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.Order;

public class ViewOrderAction extends SecureBaseAction {

  protected ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    AccountActionForm acctForm = (AccountActionForm) form;
    int orderId = Integer.parseInt(request.getParameter("orderId"));
    Order order = getPetStore().getOrder(orderId);
    if (acctForm.getAccount().getUsername().equals(order.getUsername())) {
      request.setAttribute("order", order);
      return mapping.findForward("success");
    }
		else {
      request.setAttribute("message", "You may only view your own orders.");
      return mapping.findForward("failure");
    }
  }

}
