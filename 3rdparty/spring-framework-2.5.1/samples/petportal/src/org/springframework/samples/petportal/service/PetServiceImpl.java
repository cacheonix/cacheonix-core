package org.springframework.samples.petportal.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.samples.petportal.domain.Pet;

/**
 * The PetService implementation.
 *
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class PetServiceImpl implements InitializingBean, PetService {

	private SortedMap pets = Collections.synchronizedSortedMap(new TreeMap());

	private int initPets = -1;

	public void afterPropertiesSet() throws BeansException, ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		if (initPets < 0 || initPets > 0) {
			addPet("Dog", "Poodle", "Fido", dateFormat.parse("05/21/1997"));
		}
		if (initPets < 0 || initPets > 1) {
			addPet("Cat", "Calico", "Boots", dateFormat.parse("11/07/2003"));
		}
		if (initPets < 0 || initPets > 2) {
			addPet("Bird", "Macaw", "Polly", dateFormat.parse("03/17/2001"));
		}
		if (initPets < 0 || initPets > 3) {
			addPet("Snake", "Boa", "Bo", dateFormat.parse("09/03/2005"));
		}
	}

	public Pet getPet(Integer key) {
		synchronized (pets) {
			return (Pet) this.pets.get(key);
		}
	}

	public Pet getPet(int key) {
		return getPet(new Integer(key));
	}

	public int getPetCount() {
		synchronized (pets) {
			return this.pets.size();
		}
	}

	public SortedSet getAllPets() {
		synchronized (pets) {
			return new TreeSet(this.pets.values());
		}
	}

	public int addPet(Pet pet) {
		int key;
		synchronized (pets) {
			if (pets.isEmpty()) {
				key = 1;
			}
			else {
				key = ((Integer) pets.lastKey()).intValue() + 1;
			}
			Integer keyObj = new Integer(key);
			pet.setKey(keyObj);
			this.pets.put(keyObj, pet);
		}
		return key;
	}

	public int addPet(String species, String breed, String name, Date birthdate) {
		Pet pet = new Pet(species, breed, name, birthdate);
		return addPet(pet);
	}

	public void savePet(Pet pet) {
		synchronized (pets) {
			this.pets.put(pet.getKey(), pet);
		}
	}

	public void deletePet(Integer key) {
		synchronized (pets) {
			this.pets.remove(key);
		}
	}

	public void deletePet(Pet pet) {
		deletePet(pet.getKey());
	}

	public void deletePet(int key) {
		deletePet(new Integer(key));
	}

	public void setInitPets(int initPets) {
		this.initPets = initPets;
	}

}
