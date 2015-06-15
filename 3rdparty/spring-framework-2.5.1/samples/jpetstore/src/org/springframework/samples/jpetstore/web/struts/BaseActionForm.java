package org.springframework.samples.jpetstore.web.struts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class BaseActionForm extends ActionForm {

  /* Public Methods */

  public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
    ActionErrors actionErrors = null;
    ArrayList errorList = new ArrayList();
    doValidate(mapping, request, errorList);
    request.setAttribute("errors", errorList);
    if (!errorList.isEmpty()) {
      actionErrors = new ActionErrors();
      actionErrors.add(ActionErrors.GLOBAL_ERROR, new ActionError("global.error"));
    }
    return actionErrors;
  }

  public void doValidate(ActionMapping mapping, HttpServletRequest request, List errors) {
  }

  /* Protected Methods */

  protected void addErrorIfStringEmpty(List errors, String message, String value) {
    if (value == null || value.trim().length() < 1) {
      errors.add(message);
    }
  }

}
