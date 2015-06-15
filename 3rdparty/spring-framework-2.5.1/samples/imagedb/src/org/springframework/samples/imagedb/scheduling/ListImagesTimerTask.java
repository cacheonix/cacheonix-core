package org.springframework.samples.imagedb.scheduling;

import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.samples.imagedb.ImageDatabase;
import org.springframework.samples.imagedb.ImageDescriptor;
import org.springframework.util.StringUtils;

/**
 * TimerTask implementation that lists all images in the image database.
 * Writes the list to the log at INFO level and sends a corresponding email.
 *
 * <p>The email recipient is defined in "WEB-INF/mail.properties", to be
 * passed to this TimerTask via a bean property with a placeholder value.
 *
 * <p>NOTE: This is mainly an illustration for how to implement a
 * Spring-managed TimerTask. It is normally preferable to move
 * business logic to manager classes, delegating to them via
 * MethodInvokingTimerTaskFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 23.02.2004
 * @see ListImagesQuartzJob
 * @see org.springframework.scheduling.timer.MethodInvokingTimerTaskFactoryBean
 */
public class ListImagesTimerTask extends TimerTask {

	private final Log logger = LogFactory.getLog(getClass());

	private ImageDatabase imageDatabase;

	private MailSender mailSender;

	private String mailFrom;

	private String mailTo;

	public void setImageDatabase(ImageDatabase imageDatabase) {
		this.imageDatabase = imageDatabase;
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public void run() {
		logger.info("Listing images in image database, scheduled by Timer");
		List images = this.imageDatabase.getImages();
		String[] imageNames = new String[images.size()];
		for (int i = 0; i < images.size(); i++) {
			ImageDescriptor image = (ImageDescriptor) images.get(i);
			imageNames[i] = image.getName();
		}

		String text = "Images in image database: " + StringUtils.arrayToDelimitedString(imageNames, ", ");
		logger.info(text);

		if (!"".equals(this.mailTo)) {
			logger.info("Sending image list mail to: " + this.mailTo);
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(this.mailFrom);
			message.setTo(this.mailTo);
			message.setSubject("Image list");
			message.setText(text);
			this.mailSender.send(message);
		}
		else {
			logger.info("Not sending image list mail - specify mail settings in 'WEB-INF/mail.properties'");
		}

	}

}
