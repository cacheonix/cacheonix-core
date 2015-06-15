package org.springframework.samples.jpetstore.web.struts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.Order;

public class OrderActionForm extends BaseActionForm {

  /* Constants */

  private static final List CARD_TYPE_LIST = new ArrayList();

  /* Private Fields */

  private Order order;
  private boolean shippingAddressRequired;
  private boolean confirmed;
  private List cardTypeList;

  /* Static Initializer */

  static {
    CARD_TYPE_LIST.add("Visa");
    CARD_TYPE_LIST.add("MasterCard");
    CARD_TYPE_LIST.add("American Express");
  }

  /* Constructors */

  public OrderActionForm() {
    this.order = new Order();
    this.shippingAddressRequired = false;
    this.cardTypeList = CARD_TYPE_LIST;
    this.confirmed = false;
  }

  /* JavaBeans Properties */

  public boolean isConfirmed() { return confirmed; }
  public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

  public Order getOrder() { return order; }
  public void setOrder(Order order) { this.order = order; }

  public boolean isShippingAddressRequired() { return shippingAddressRequired; }
  public void setShippingAddressRequired(boolean shippingAddressRequired) { this.shippingAddressRequired = shippingAddressRequired; }

  public List getCreditCardTypes() { return cardTypeList; }

  /* Public Methods */

  public void doValidate(ActionMapping mapping, HttpServletRequest request, List errors) {

    if (!this.isShippingAddressRequired()) {
      addErrorIfStringEmpty(errors, "FAKE (!) credit card number required.", order.getCreditCard());
      addErrorIfStringEmpty(errors, "Expiry date is required.", order.getExpiryDate());
      addErrorIfStringEmpty(errors, "Card type is required.", order.getCardType());

      addErrorIfStringEmpty(errors, "Shipping Info: first name is required.", order.getShipToFirstName());
      addErrorIfStringEmpty(errors, "Shipping Info: last name is required.", order.getShipToLastName());
      addErrorIfStringEmpty(errors, "Shipping Info: address is required.", order.getShipAddress1());
      addErrorIfStringEmpty(errors, "Shipping Info: city is required.", order.getShipCity());
      addErrorIfStringEmpty(errors, "Shipping Info: state is required.", order.getShipState());
      addErrorIfStringEmpty(errors, "Shipping Info: zip/postal code is required.", order.getShipZip());
      addErrorIfStringEmpty(errors, "Shipping Info: country is required.", order.getShipCountry());

      addErrorIfStringEmpty(errors, "Billing Info: first name is required.", order.getBillToFirstName());
      addErrorIfStringEmpty(errors, "Billing Info: last name is required.", order.getBillToLastName());
      addErrorIfStringEmpty(errors, "Billing Info: address is required.", order.getBillAddress1());
      addErrorIfStringEmpty(errors, "Billing Info: city is required.", order.getBillCity());
      addErrorIfStringEmpty(errors, "Billing Info: state is required.", order.getBillState());
      addErrorIfStringEmpty(errors, "Billing Info: zip/postal code is required.", order.getBillZip());
      addErrorIfStringEmpty(errors, "Billing Info: country is required.", order.getBillCountry());
    }

    if (errors.size() > 0) {
      order.setBillAddress1(order.getShipAddress1());
      order.setBillAddress2(order.getShipAddress2());
      order.setBillToFirstName(order.getShipToFirstName());
      order.setBillToLastName(order.getShipToLastName());
      order.setBillCity(order.getShipCity());
      order.setBillCountry(order.getShipCountry());
      order.setBillState(order.getShipState());
      order.setBillZip(order.getShipZip());
    }

  }

  public void reset(ActionMapping mapping, HttpServletRequest request) {
    super.reset(mapping, request);
    shippingAddressRequired = false;
  }

}
