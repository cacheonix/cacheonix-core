package org.springframework.samples.jpetstore.domain.logic;

import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author Juergen Hoeller
 * @since 01.12.2003
 */
public class OrderValidator implements Validator {

	public boolean supports(Class clazz) {
		return Order.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		validateCreditCard((Order) obj, errors);
		validateBillingAddress((Order) obj, errors);
		validateShippingAddress((Order) obj, errors);
	}

	public void validateCreditCard(Order order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "creditCard", "CCN_REQUIRED", "FAKE (!) credit card number required.");
		ValidationUtils.rejectIfEmpty(errors, "expiryDate", "EXPIRY_DATE_REQUIRED", "Expiry date is required.");
		ValidationUtils.rejectIfEmpty(errors, "cardType", "CARD_TYPE_REQUIRED", "Card type is required.");
	}

	public void validateBillingAddress(Order order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "billToFirstName", "FIRST_NAME_REQUIRED", "Billing Info: first name is required.");
		ValidationUtils.rejectIfEmpty(errors, "billToLastName", "LAST_NAME_REQUIRED", "Billing Info: last name is required.");
		ValidationUtils.rejectIfEmpty(errors, "billAddress1", "ADDRESS_REQUIRED", "Billing Info: address is required.");
		ValidationUtils.rejectIfEmpty(errors, "billCity", "CITY_REQUIRED", "Billing Info: city is required.");
		ValidationUtils.rejectIfEmpty(errors, "billState", "STATE_REQUIRED", "Billing Info: state is required.");
		ValidationUtils.rejectIfEmpty(errors, "billZip", "ZIP_REQUIRED", "Billing Info: zip/postal code is required.");
		ValidationUtils.rejectIfEmpty(errors, "billCountry", "COUNTRY_REQUIRED", "Billing Info: country is required.");
	}

	public void validateShippingAddress(Order order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "shipToFirstName", "FIRST_NAME_REQUIRED", "Shipping Info: first name is required.");
		ValidationUtils.rejectIfEmpty(errors, "shipToLastName", "LAST_NAME_REQUIRED", "Shipping Info: last name is required.");
		ValidationUtils.rejectIfEmpty(errors, "shipAddress1", "ADDRESS_REQUIRED", "Shipping Info: address is required.");
		ValidationUtils.rejectIfEmpty(errors, "shipCity", "CITY_REQUIRED", "Shipping Info: city is required.");
		ValidationUtils.rejectIfEmpty(errors, "shipState", "STATE_REQUIRED", "Shipping Info: state is required.");
		ValidationUtils.rejectIfEmpty(errors, "shipZip", "ZIP_REQUIRED", "Shipping Info: zip/postal code is required.");
		ValidationUtils.rejectIfEmpty(errors, "shipCountry", "COUNTRY_REQUIRED", "Shipping Info: country is required.");
	}
}
