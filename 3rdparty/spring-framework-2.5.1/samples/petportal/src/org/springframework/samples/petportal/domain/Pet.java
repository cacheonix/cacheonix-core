package org.springframework.samples.petportal.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.Assert;

/**
 * A simple domain object representing a Pet.
 *
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class Pet implements Comparable, Serializable {

	private Integer key;

	private String species;

	private String breed;

	private String name;

	private Date birthdate;

	private String description;

	private int hashCode = Integer.MIN_VALUE;


	public Pet() {
		super();
	}

	public Pet(String species, String breed, String name, Date birthdate) {
		super();
		setSpecies(species);
		setBreed(breed);
		setName(name);
		setBirthdate(birthdate);
	}


	/**
	 * Get the Pet's name.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the Pet's name.
	 */
	public void setName(String name) {
		Assert.notNull(name, "name may not be null");
		this.name = name;
		this.hashCode = Integer.MIN_VALUE;
	}

	/**
	 * Get the Pet's species.
	 *
	 * @return species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * Set the Pet's species.
	 */
	public void setSpecies(String species) {
		Assert.notNull(species, "species may not be null");
		this.species = species;
		this.hashCode = Integer.MIN_VALUE;
	}

	/**
	 * Get the Pet's breed.
	 *
	 * @return breed
	 */
	public String getBreed() {
		return this.breed;
	}

	/**
	 * Set the Pet's breed.
	 */
	public void setBreed(String breed) {
		Assert.notNull(breed, "breed may not be null");
		this.breed = breed;
		this.hashCode = Integer.MIN_VALUE;
	}

	/**
	 * Get the Pet's birthdate.
	 *
	 * @return birthdate
	 */
	public Date getBirthdate() {
		return birthdate;
	}

	/**
	 * Set the Pet's birthdate.
	 */
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	/**
	 * Get the Pet's description
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the Pet's description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the Pet's key.
	 *
	 * @return key
	 */
	public Integer getKey() {
		return key;
	}

	/**
	 * Set the Pet's key.
	 */
	public void setKey(Integer key) {
		this.key = key;
	}

	public int compareTo(Object obj) {
		if (obj == null) {
			throw new NullPointerException("Cannot compare to null object");
		}
		if (!(obj instanceof Pet)) {
			throw new ClassCastException("Can only compare to class " + this.getClass().getName());
		}
		if (this.species == null || this.breed == null || this.name == null) {
			throw new NullPointerException("This object is not initialized yet");
		}
		if (this.equals(obj)) {
			return 0;
		}
		Pet pet = (Pet) obj;
		int res = getSpecies().compareTo(pet.getSpecies());
		if (res != 0) {
			return res;
		}
		res = getBreed().compareTo(pet.getBreed());
		if (res != 0) {
			return res;
		}
		return getName().compareTo(pet.getName());
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pet)) {
			return false;
		}
		if (this.species == null || this.breed == null || this.name == null) {
			return false;
		}
		Pet pet = (Pet) obj;
		return (this.species.equals(pet.getSpecies()) &&
				this.breed.equals(pet.getBreed()) &&
				this.name.equals(pet.getName()));
	}

	public int hashCode() {
		if (Integer.MIN_VALUE == this.hashCode) {
			String hashStr = this.getClass().getName() + ":" + this.toString();
			this.hashCode = hashStr.hashCode();
		}
		return this.hashCode;
	}

	public String toString() {
		return this.species + ":" + this.breed + ":" + this.name;
	}

}
