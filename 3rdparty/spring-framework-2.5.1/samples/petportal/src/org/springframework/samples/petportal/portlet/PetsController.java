package org.springframework.samples.petportal.portlet;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.samples.petportal.domain.Pet;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.samples.petportal.validation.PetValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * This is a simple Controller which delegates to the 
 * {@link PetService PetService} and then populates the model with all 
 * returned Pets. This could have extended AbstractController in which 
 * case only the render phase would have required handling. However, 
 * this demonstrates the ability to simply implement the Controller 
 * interface.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
@Controller
@RequestMapping("VIEW")
@SessionAttributes("pet")
public class PetsController {

	private final PetService petService;

	@Autowired
	public PetsController(PetService petService) {
		this.petService = petService;
	}

	/**
	 * For the page where the 'birthdate' is to be entered, the dateFormat is
	 * provided so that it may be displayed to the user. The format is
	 * retrieved from the PortletPreferences.
	 */
	@ModelAttribute("dateFormat")
	protected String getDateFormat(PortletPreferences preferences) {
		return preferences.getValue("dateFormat", PetService.DEFAULT_DATE_FORMAT);
	}

	/**
	 * Registers a PropertyEditor with the data binder for handling Dates
	 * using the format as currently specified in the PortletPreferences.
	 */
	@InitBinder
	public void initBinder(PortletRequestDataBinder binder, PortletPreferences preferences) {
		String formatString = preferences.getValue("dateFormat", PetService.DEFAULT_DATE_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		binder.setAllowedFields(new String[] {"species", "breed", "name", "birthdate"});
	}

	@RequestMapping  // default render (action=list)
	public String listPets(Model model) {
		model.addAttribute("pets", this.petService.getAllPets());
		return "pets";
	}

	@RequestMapping(params = "action=view")  // render phase
	public String viewPet(@RequestParam("pet") int petId, Model model) {
		model.addAttribute("pet", this.petService.getPet(petId));
		return "petView";
	}

	@RequestMapping(params = "action=add")  // render phase
	public String showPetForm(Model model) {
		// Used for the initial form as well as for redisplaying with errors.
		if (!model.containsAttribute("pet")) {
			model.addAttribute("pet", new Pet());
			model.addAttribute("page", 0);
		}
		return "petAdd";
	}

	@RequestMapping(params = "action=add")   // action phase
	public void submitPage(
			ActionRequest request, ActionResponse response,
			@ModelAttribute("pet") Pet pet, BindingResult result,
			@RequestParam("_page") int currentPage, Model model) {

		if (request.getParameter("_cancel") != null) {
			response.setRenderParameter("action", "list");
		}
		else if (request.getParameter("_finish") != null) {
			new PetValidator().validate(pet, result);
			if (!result.hasErrors()) {
				this.petService.addPet(pet);
				response.setRenderParameter("action", "list");
			}
			else {
				model.addAttribute("page", currentPage);
			}
		}
		else {
			switch (currentPage) {
				case 0: new PetValidator().validateSpecies(pet, result); break;
				case 1: new PetValidator().validateBreed(pet, result); break;
				case 2: new PetValidator().validateName(pet, result); break;
				case 3: new PetValidator().validateBirthdate(pet, result); break;
			}
			int targetPage = currentPage;
			if (!result.hasErrors()) {
				targetPage = PortletUtils.getTargetPage(request, "_target", currentPage);
			}
			model.addAttribute("page", targetPage);
		}
	}

	@RequestMapping(params = "action=delete")  // action phase
	public void deletePet(@RequestParam("pet") int petId, ActionResponse response) {
		this.petService.deletePet(petId);
		response.setRenderParameter("action", "list");
	}

}
