package org.springframework.samples.petportal.validation;

import org.springframework.samples.petportal.domain.PetSite;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * A validator for {@link PetSite PetSite} objects.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class PetSiteValidator {

	public void validate(PetSite petSite, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "name", "NAME_REQUIRED", "Name is required.");
		ValidationUtils.rejectIfEmpty(errors, "url", "URL_REQUIRED", "URL is required.");
	}
	
}
