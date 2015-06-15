package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.Order;

public class NewOrderAction extends SecureBaseAction {

  protected ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    OrderActionForm orderForm = (OrderActionForm) form;
    if (orderForm.isShippingAddressRequired()) {
      return mapping.findForward("shipping");
    }
		else if (!orderForm.isConfirmed()) {
      return mapping.findForward("confirm");
    }
		else if (orderForm.getOrder() != null) {
      Order order = orderForm.getOrder();
      getPetStore().insertOrder(order);
      request.getSession().removeAttribute("workingOrderForm");
      request.getSession().removeAttribute("cartForm");
      request.setAttribute("order", order);
      request.setAttribute("message", "Thank you, your order has been submitted.");
      return mapping.findForward("success");
    }
		else {
      request.setAttribute("message", "An error occurred processing your order (order was null).");
      return mapping.findForward("failure");
    }
  }

}
