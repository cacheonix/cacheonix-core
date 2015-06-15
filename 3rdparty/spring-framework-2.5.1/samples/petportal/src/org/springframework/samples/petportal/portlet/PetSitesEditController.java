package org.springframework.samples.petportal.portlet;

import java.util.Properties;

import javax.portlet.ActionResponse;

import org.springframework.samples.petportal.domain.PetSite;
import org.springframework.samples.petportal.validation.PetSiteValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * This Controller simply populates the model with the current map
 * of 'petSites' and then forwards to the view from which a user can
 * add to or delete from the sites. The HandlerMapping maps to this 
 * Controller when in EDIT mode while no valid 'action' parameter
 * is set. See 'WEB-INF/context/petsites-portlet.xml' for details.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
@Controller
@RequestMapping("EDIT")
@SessionAttributes("site")
public class PetSitesEditController {

	private Properties petSites;
	
	public void setPetSites(Properties petSites) {
		this.petSites = petSites;
	}

	@ModelAttribute("petSites")
	public Properties getPetSites() {
		return this.petSites;
	}

	@RequestMapping  // default (action=list)
	public String showPetSites() {
		return "petSitesEdit";
	}

	@RequestMapping(params = "action=add")  // render phase
	public String showSiteForm(Model model) {
		// Used for the initial form as well as for redisplaying with errors.
		if (!model.containsAttribute("site")) {
			model.addAttribute("site", new PetSite());
		}
		return "petSitesAdd";
	}

	@RequestMapping(params = "action=add")  // action phase
	public void populateSite(
			@ModelAttribute("site") PetSite petSite, BindingResult result, SessionStatus status, ActionResponse response) {

		new PetSiteValidator().validate(petSite, result);
		if (!result.hasErrors()) {
			this.petSites.put(petSite.getName(), petSite.getUrl());
			status.setComplete();
			response.setRenderParameter("action", "list");
		}
	}

	@RequestMapping(params = "action=delete")
	public void removeSite(@RequestParam("site") String site, ActionResponse response) {
		this.petSites.remove(site);
		response.setRenderParameter("action", "list");
	}

}
