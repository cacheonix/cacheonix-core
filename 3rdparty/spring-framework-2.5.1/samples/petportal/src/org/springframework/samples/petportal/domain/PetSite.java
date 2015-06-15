package org.springframework.samples.petportal.domain;

import java.io.Serializable;
import java.net.URL;

/**
 * A PetSite stores the url of a website that is external to
 * the portlet as well as a user-supplied name for the site.
 * This is used to demonstrate a redirect from a portlet.
 * 
 * The websites to be included upon portlet startup are in a file
 * called "petsites.properties" within the WEB-INF directory.
 * 
 * See the bean definitions in "petsites-portlet.xml" for more detail.
 * 
 * @author Mark Fisher
 */
public class PetSite implements Serializable {

	private String name;

	private URL url;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getUrl() {
		return url;
	}
	
	public void setUrl(URL url) {
		this.url = url;
	}
	
}
