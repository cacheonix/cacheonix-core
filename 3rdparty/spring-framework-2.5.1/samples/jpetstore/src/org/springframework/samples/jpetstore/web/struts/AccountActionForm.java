package org.springframework.samples.jpetstore.web.struts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.samples.jpetstore.domain.Account;

public class AccountActionForm extends BaseActionForm {

  /* Constants */

  public static final String VALIDATE_EDIT_ACCOUNT = "editAccount";
  public static final String VALIDATE_NEW_ACCOUNT = "newAccount";
  private static final ArrayList LANGUAGE_LIST = new ArrayList();

  /* Private Fields */

  private String username;
  private String password;
  private String repeatedPassword;
  private List languages;
  private List categories;
  private String validate;
  private String forwardAction;
  private Account account;
  private PagedListHolder myList;

  /* Static Initializer */

  static {
    LANGUAGE_LIST.add("english");
    LANGUAGE_LIST.add("japanese");
  }

  /* Constructors */

  public AccountActionForm() {
    languages = LANGUAGE_LIST;
  }

  /* JavaBeans Properties */

  public PagedListHolder getMyList() { return myList; }
  public void setMyList(PagedListHolder myList) { this.myList = myList; }

  public String getForwardAction() { return forwardAction; }
  public void setForwardAction(String forwardAction) { this.forwardAction = forwardAction; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getRepeatedPassword() { return repeatedPassword; }
  public void setRepeatedPassword(String repeatedPassword) { this.repeatedPassword = repeatedPassword; }

  public Account getAccount() { return account; }
  public void setAccount(Account account) { this.account = account; }

  public List getLanguages() { return languages; }
  public void setLanguages(List languages) { this.languages = languages; }

  public List getCategories() { return categories; }
  public void setCategories(List categories) { this.categories = categories; }

  public String getValidate() { return validate; }
  public void setValidate(String validate) { this.validate = validate; }

  /* Public Methods */

  public void doValidate(ActionMapping mapping, HttpServletRequest request, List errors) {
    if (validate != null) {
      if (VALIDATE_EDIT_ACCOUNT.equals(validate) || VALIDATE_NEW_ACCOUNT.equals(validate)) {
        if (VALIDATE_NEW_ACCOUNT.equals(validate)) {
          account.setStatus("OK");
          addErrorIfStringEmpty(errors, "User ID is required.", account.getUsername());
          if (account.getPassword() == null || account.getPassword().length() < 1 || !account.getPassword().equals(repeatedPassword)) {
            errors.add("Passwords did not match or were not provided.  Matching passwords are required.");
          }
        }
        if (account.getPassword() != null && account.getPassword().length() > 0) {
          if (!account.getPassword().equals(repeatedPassword)) {
            errors.add("Passwords did not match.");
          }
        }
        addErrorIfStringEmpty(errors, "First name is required.", this.account.getFirstName());
        addErrorIfStringEmpty(errors, "Last name is required.", this.account.getLastName());
        addErrorIfStringEmpty(errors, "Email address is required.", this.account.getEmail());
        addErrorIfStringEmpty(errors, "Phone number is required.", this.account.getPhone());
        addErrorIfStringEmpty(errors, "Address (1) is required.", this.account.getAddress1());
        addErrorIfStringEmpty(errors, "City is required.", this.account.getCity());
        addErrorIfStringEmpty(errors, "State is required.", this.account.getState());
        addErrorIfStringEmpty(errors, "ZIP is required.", this.account.getZip());
        addErrorIfStringEmpty(errors, "Country is required.", this.account.getCountry());
      }
    }

  }

  public void reset(ActionMapping mapping, HttpServletRequest request) {
    super.reset(mapping, request);
    setUsername(null);
    setPassword(null);
    setRepeatedPassword(null);
  }

}
