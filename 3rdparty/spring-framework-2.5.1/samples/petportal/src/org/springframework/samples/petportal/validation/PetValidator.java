package org.springframework.samples.petportal.validation;

import org.springframework.samples.petportal.domain.Pet;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * A validator for {@link Pet} objects.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class PetValidator {

	public void validate(Pet pet, Errors errors) {
		validateSpecies(pet, errors);
		validateBreed(pet, errors);
		validateName(pet, errors);
		validateBirthdate(pet, errors);
	}
	
	public void validateSpecies(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "species", "SPECIES_REQUIRED", "Species is required.");
	}

	public void validateBreed(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "breed", "BREED_REQUIRED", "Breed is required.");
	}
	
	public void validateName(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "name", "NAME_REQUIRED", "Name is required.");
	}
	
	public void validateBirthdate(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "birthdate", "required.java.util.Date", "Birthdate is required.");
	}
}
