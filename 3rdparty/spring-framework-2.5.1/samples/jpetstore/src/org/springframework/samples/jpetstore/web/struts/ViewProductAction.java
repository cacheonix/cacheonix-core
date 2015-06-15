package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Product;

public class ViewProductAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    String productId = request.getParameter("productId");
    if (productId != null) {
			PagedListHolder itemList = new PagedListHolder(getPetStore().getItemListByProduct(productId));
			itemList.setPageSize(4);
			Product product = getPetStore().getProduct(productId);
      request.getSession().setAttribute("ViewProductAction_itemList", itemList);
			request.getSession().setAttribute("ViewProductAction_product", product);
			request.setAttribute("itemList", itemList);
      request.setAttribute("product", product);
    }
		else {
			PagedListHolder itemList = (PagedListHolder) request.getSession().getAttribute("ViewProductAction_itemList");
			Product product = (Product) request.getSession().getAttribute("ViewProductAction_product");
      String page = request.getParameter("page");
      if ("next".equals(page)) {
        itemList.nextPage();
      }
			else if ("previous".equals(page)) {
        itemList.previousPage();
      }
			request.setAttribute("itemList", itemList);
      request.setAttribute("product", product);
    }
    return mapping.findForward("success");
  }

}
