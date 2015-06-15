package org.springframework.samples.jpetstore.web.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.jpetstore.domain.Account;
import org.springframework.samples.jpetstore.domain.Cart;
import org.springframework.samples.jpetstore.domain.logic.OrderValidator;
import org.springframework.samples.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

/**
 * @author Juergen Hoeller
 * @since 01.12.2003
 */
public class OrderFormController extends AbstractWizardFormController {

	private PetStoreFacade petStore;

	public OrderFormController() {
		setCommandName("orderForm");
		setPages(new String[] {"NewOrderForm", "ShippingForm", "ConfirmOrder"});
	}

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	protected Object formBackingObject(HttpServletRequest request) throws ModelAndViewDefiningException {
		UserSession userSession = (UserSession) request.getSession().getAttribute("userSession");
		Cart cart = (Cart) request.getSession().getAttribute("sessionCart");
		if (cart != null) {
			// Re-read account from DB at team's request.
			Account account = this.petStore.getAccount(userSession.getAccount().getUsername());
			OrderForm orderForm = new OrderForm();
			orderForm.getOrder().initOrder(account, cart);
			return orderForm;
		}
		else {
			ModelAndView modelAndView = new ModelAndView("Error");
			modelAndView.addObject("message", "An order could not be created because a cart could not be found.");
			throw new ModelAndViewDefiningException(modelAndView);
		}
	}

	protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors, int page) {
		if (page == 0 && request.getParameter("shippingAddressRequired") == null) {
			OrderForm orderForm = (OrderForm) command;
			orderForm.setShippingAddressRequired(false);
		}
	}

	protected Map referenceData(HttpServletRequest request, int page) {
		if (page == 0) {
			List creditCardTypes = new ArrayList();
			creditCardTypes.add("Visa");
			creditCardTypes.add("MasterCard");
			creditCardTypes.add("American Express");
			Map model = new HashMap();
			model.put("creditCardTypes", creditCardTypes);
			return model;
		}
		return null;
	}

	protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int currentPage) {
		OrderForm orderForm = (OrderForm) command;
		if (currentPage == 0 && orderForm.isShippingAddressRequired()) {
			return 1;
		}
		else {
			return 2;
		}
	}

	protected void validatePage(Object command, Errors errors, int page) {
		OrderForm orderForm = (OrderForm) command;
		OrderValidator orderValidator = (OrderValidator) getValidator();
		errors.setNestedPath("order");
		switch (page) {
			case 0:
				orderValidator.validateCreditCard(orderForm.getOrder(), errors);
				orderValidator.validateBillingAddress(orderForm.getOrder(), errors);
				break;
			case 1:
				orderValidator.validateShippingAddress(orderForm.getOrder(), errors);
		}
		errors.setNestedPath("");
	}

	protected ModelAndView processFinish(
			HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) {
		OrderForm orderForm = (OrderForm) command;
		this.petStore.insertOrder(orderForm.getOrder());
		request.getSession().removeAttribute("sessionCart");
		Map model = new HashMap();
		model.put("order", orderForm.getOrder());
		model.put("message", "Thank you, your order has been submitted.");
		return new ModelAndView("ViewOrder", model);
	}

}
