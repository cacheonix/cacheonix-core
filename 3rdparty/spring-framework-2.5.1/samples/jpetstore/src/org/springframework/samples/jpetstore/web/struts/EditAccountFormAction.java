package org.springframework.samples.jpetstore.web.struts;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.Account;

public class EditAccountFormAction extends SecureBaseAction {

  protected ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    AccountActionForm workingAcctForm = (AccountActionForm) form;
    AccountActionForm acctForm = (AccountActionForm) request.getSession().getAttribute("accountForm");
    String username = acctForm.getAccount().getUsername();
    if (workingAcctForm.getAccount() == null) {
      Account account = getPetStore().getAccount(username);
      workingAcctForm.setAccount(account);
    }
    if (workingAcctForm.getCategories() == null) {
      List categories = getPetStore().getCategoryList();
      workingAcctForm.setCategories(categories);
    }
    return mapping.findForward("success");
  }

}
