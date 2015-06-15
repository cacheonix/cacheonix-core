package org.springframework.samples.imagedb.web;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.imagedb.ImageDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * MultiActionController for the image list/upload UI.
 *
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
@Controller
public class ImageController {

	private final ImageDatabase imageDatabase;

	@Autowired
	public ImageController(ImageDatabase imageDatabase) {
		this.imageDatabase = imageDatabase;
	}

	@RequestMapping("/imageList")
	public String showImageList(Model model) {
		model.addAttribute("images", this.imageDatabase.getImages());
		return "imageList";
	}

	@RequestMapping("/imageContent")
	public void streamImageContent(@RequestParam("name") String name, OutputStream outputStream) throws IOException {
		this.imageDatabase.streamImage(name, outputStream);
	}

	@RequestMapping("/imageUpload")
	public String processImageUpload(
			@RequestParam("name") String name, @RequestParam("description") String description,
			@RequestParam("image") MultipartFile image) throws IOException {

		this.imageDatabase.storeImage(name, image.getInputStream(), (int) image.getSize(), description);
		return "redirect:imageList";
	}

	@RequestMapping("/clearDatabase")
	public String clearDatabase() {
		this.imageDatabase.clearDatabase();
		return "redirect:imageList";
	}

}
