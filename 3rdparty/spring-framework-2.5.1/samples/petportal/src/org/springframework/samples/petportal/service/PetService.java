package org.springframework.samples.petportal.service;

import java.util.Date;
import java.util.SortedSet;

import org.springframework.samples.petportal.domain.Pet;

/**
 * The PetService interface.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public interface PetService {
	
	public final static String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";

	public Pet getPet(Integer key);

	public Pet getPet(int key);

	public int getPetCount();

	public SortedSet getAllPets();

	public int addPet(Pet pet);

	public int addPet(String species, String breed, String name, Date birthdate);

	public void savePet(Pet pet);

	public void deletePet(Integer key);

	public void deletePet(Pet pet);

	public void deletePet(int key);
	
}