package org.springframework.samples.imagedb.scheduling;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.samples.imagedb.ImageDatabase;
import org.springframework.samples.imagedb.ImageDescriptor;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;

/**
 * Quartz Job implementation that lists all images in the image database.
 * Writes the list to the log at INFO level and sends a corresponding email.
 *
 * <p>The email recipient is defined in "WEB-INF/mail.properties", to be
 * passed to this Quartz Job via a bean property with a placeholder value.
 *
 * <p>NOTE: This is mainly an illustration for how to implement a
 * Spring-managed Quartz Job. It is normally preferable to move
 * business logic to manager classes, delegating to them via
 * MethodInvokingJobDetailFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 23.02.2004
 * @see ListImagesTimerTask
 * @see org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean
 */
public class ListImagesQuartzJob extends QuartzJobBean {

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

	protected void executeInternal(JobExecutionContext context) {
		logger.info("Listing images in image database, scheduled by Quartz");
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

		logger.info("Next job execution at: " + context.getNextFireTime());
	}

}
