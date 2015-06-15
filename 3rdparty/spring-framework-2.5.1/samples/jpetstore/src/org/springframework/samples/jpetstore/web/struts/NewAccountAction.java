package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Account;

public class NewAccountAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    AccountActionForm acctForm = (AccountActionForm) form;
		if (AccountActionForm.VALIDATE_NEW_ACCOUNT.equals(acctForm.getValidate())) {
			acctForm.getAccount().setListOption(request.getParameter("account.listOption") != null);
			acctForm.getAccount().setBannerOption(request.getParameter("account.bannerOption") != null);
			Account account = acctForm.getAccount();
			String username = acctForm.getAccount().getUsername();
			getPetStore().insertAccount(account);
			acctForm.setAccount(getPetStore().getAccount(username));
			PagedListHolder myList = new PagedListHolder(getPetStore().getProductListByCategory(account.getFavouriteCategoryId()));
			myList.setPageSize(4);
			acctForm.setMyList(myList);
			request.getSession().setAttribute("accountForm", acctForm);
			request.getSession().removeAttribute("workingAccountForm");
			return mapping.findForward("success");
		}
		else {
			request.setAttribute("message", "Your account was not created because the submitted information was not validated.");
			return mapping.findForward("failure");
		}
  }

}
