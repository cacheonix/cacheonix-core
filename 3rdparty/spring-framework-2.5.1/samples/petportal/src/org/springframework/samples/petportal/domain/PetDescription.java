package org.springframework.samples.petportal.domain;

import java.io.Serializable;

/**
 * The PetDescription stores a file's contents as an array of bytes.
 * It is used to demonstrate file upload from within a portlet.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetDescription implements Serializable {

	private static final long serialVersionUID = 5626992748524133629L;

	private byte[] file;
	

	public PetDescription() {
		super();
	}
	
	public PetDescription(byte[] file) {
		this();
		setFile(file);
	}
	

	/**
	 * Set the file as a byte array.
	 */
	public void setFile(byte[] file) {
		this.file = file;
	}
	
	/**
	 * Return the file as a byte array.
	 */
	public byte[] getFile() {
		return this.file;
	}

}
