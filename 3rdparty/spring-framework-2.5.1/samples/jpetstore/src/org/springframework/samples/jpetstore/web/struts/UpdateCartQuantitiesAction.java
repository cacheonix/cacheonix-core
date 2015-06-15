package org.springframework.samples.jpetstore.web.struts;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.CartItem;

public class UpdateCartQuantitiesAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    CartActionForm cartForm = (CartActionForm) form;
    Iterator cartItems = cartForm.getCart().getAllCartItems();
    while (cartItems.hasNext()) {
      CartItem cartItem = (CartItem) cartItems.next();
      String itemId = cartItem.getItem().getItemId();
      try {
        int quantity = Integer.parseInt(request.getParameter(itemId));
        cartForm.getCart().setQuantityByItemId(itemId, quantity);
        if (quantity < 1) {
          cartItems.remove();
        }
      }
			catch (NumberFormatException e) {
        //ignore on purpose
      }
    }
    return mapping.findForward("success");
  }

}
