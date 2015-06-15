package org.springframework.samples.petportal.domain;

import java.io.Serializable;

/**
 * This exception is used to demonstrate an exception mapping in the 
 * "customExceptionHandler" of upload-portlet.xml.
 * 
 * @author Mark Fisher
 */
public class InvalidFileTypeException extends RuntimeException implements Serializable {

	public InvalidFileTypeException(String message) {
		super(message);
	}

}
