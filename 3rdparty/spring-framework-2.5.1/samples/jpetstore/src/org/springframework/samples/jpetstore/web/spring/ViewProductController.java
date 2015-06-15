package org.springframework.samples.jpetstore.web.spring;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Product;
import org.springframework.samples.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class ViewProductController implements Controller {

	private PetStoreFacade petStore;

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		String productId = request.getParameter("productId");
		if (productId != null) {
			PagedListHolder itemList = new PagedListHolder(this.petStore.getItemListByProduct(productId));
			itemList.setPageSize(4);
			Product product = this.petStore.getProduct(productId);
			request.getSession().setAttribute("ViewProductAction_itemList", itemList);
			request.getSession().setAttribute("ViewProductAction_product", product);
			model.put("itemList", itemList);
			model.put("product", product);
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
			model.put("itemList", itemList);
			model.put("product", product);
		}
		return new ModelAndView("Product", model);
	}

}
