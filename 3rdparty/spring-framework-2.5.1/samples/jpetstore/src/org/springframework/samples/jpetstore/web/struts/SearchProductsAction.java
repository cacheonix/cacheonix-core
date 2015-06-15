package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.util.StringUtils;

public class SearchProductsAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String keyword = request.getParameter("keyword");
		if (keyword != null) {
			if (!StringUtils.hasLength(keyword)) {
				request.setAttribute("message", "Please enter a keyword to search for, then press the search button.");
				return mapping.findForward("failure");
			}
			PagedListHolder productList = new PagedListHolder(getPetStore().searchProductList(keyword.toLowerCase()));
			productList.setPageSize(4);
			request.getSession().setAttribute("SearchProductsAction_productList", productList);
			request.setAttribute("productList", productList);
			return mapping.findForward("success");
		}
		else {
      String page = request.getParameter("page");
      PagedListHolder productList = (PagedListHolder) request.getSession().getAttribute("SearchProductsAction_productList");
			if (productList == null) {
				request.setAttribute("message", "Your session has timed out. Please start over again.");
				return mapping.findForward("failure");
			}
			if ("next".equals(page)) {
				productList.nextPage();
			}
			else if ("previous".equals(page)) {
				productList.previousPage();
			}
			request.setAttribute("productList", productList);
			return mapping.findForward("success");
    }
  }

}
