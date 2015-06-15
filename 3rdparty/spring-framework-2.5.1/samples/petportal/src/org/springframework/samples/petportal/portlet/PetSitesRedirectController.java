package org.springframework.samples.petportal.portlet;

import java.util.Properties;

import javax.portlet.ActionResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This Controller demonstrates a redirect to a website that is external 
 * to the portlet. The 'petsites-portlet' HandlerMapping will map to this 
 * view whenever in VIEW mode. See 'WEB-INF/context/petsites-portlet.xml' 
 * for details.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
@Controller
@RequestMapping("VIEW")
public class PetSitesRedirectController {

	private Properties petSites;
	
	public void setPetSites(Properties petSites) {
		this.petSites = petSites;
	}
	
	@ModelAttribute("petSites")
	public Properties getPetSites() {
		return this.petSites;
	}

	@RequestMapping  // default render
	public String showPetSites() {
		return "petSitesView";
	}

	@RequestMapping  // default action
	public void doRedirect(@RequestParam("url") String url, ActionResponse response) throws Exception {
		response.sendRedirect(url);
	}

}
