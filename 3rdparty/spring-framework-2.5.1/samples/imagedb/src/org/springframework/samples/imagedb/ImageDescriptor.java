package org.springframework.samples.imagedb;

import org.springframework.util.Assert;

/**
 * Simple data holder for image descriptions.
 *
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
public class ImageDescriptor {

	public static final int SHORT_DESCRIPTION_MAX_LENGTH = 1000;

	private final String name;

	private final String description;

	protected ImageDescriptor(String name, String description) {
		Assert.notNull(name, "No image name specified");
		this.name = name;
		this.description = (description != null ? description : "");
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getShortDescription() {
		return (description == null || description.length() <= SHORT_DESCRIPTION_MAX_LENGTH) ?
		    description : description.substring(0, SHORT_DESCRIPTION_MAX_LENGTH);
	}

	public int getDescriptionLength() {
		return (description != null ? description.length() : 0);
	}

}
