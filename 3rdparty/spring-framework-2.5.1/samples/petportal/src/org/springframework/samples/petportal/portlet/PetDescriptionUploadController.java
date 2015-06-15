package org.springframework.samples.petportal.portlet;

import java.util.SortedSet;

import org.springframework.samples.petportal.domain.Pet;
import org.springframework.samples.petportal.domain.PetDescription;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

/**
 * This Controller demonstrates multipart file uploads. In this case, 
 * an uploaded text file will be used as the description for a Pet.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
@Controller
@RequestMapping("VIEW")
public class PetDescriptionUploadController {
	
	private PetService petService;
	
	public void setPetService(PetService petService) {
		this.petService = petService;
	}

	@ModelAttribute("pets")
	public SortedSet getPets() {
		return this.petService.getAllPets();
	}
	
	/**
	 * Register the PropertyEditor for converting from a MultipartFile to an array of bytes.
	 */
	@InitBinder
	public void registerMultipartEditor(WebDataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

	/**
	 * If there are no Pets, display the 'noPetsForUpload' view. 
	 * Otherwise show the upload form.
	 */
	@RequestMapping  // default render
	public String showUploadForm() {
		if (this.petService.getPetCount() > 0) {
			return "upload";
		}
		else {
			return "noPetsForUpload";
		}
	}

	/**
	 * On submit, set the description property for the selected Pet as a String.
	 */
	@RequestMapping  // default action
	public void processUpload(PetDescription upload, @RequestParam("selectedPet") int petKey) {
		byte[] file = upload.getFile();
		String description = new String(file);
		Pet pet = this.petService.getPet(petKey);
		pet.setDescription(description);
		this.petService.savePet(pet);
	}

}
