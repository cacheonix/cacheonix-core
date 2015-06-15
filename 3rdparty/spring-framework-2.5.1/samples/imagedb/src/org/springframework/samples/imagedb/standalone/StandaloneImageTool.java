package org.springframework.samples.imagedb.standalone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.samples.imagedb.ImageDatabase;
import org.springframework.samples.imagedb.ImageDescriptor;
import org.springframework.util.StopWatch;

/**
 * Standalone sample that simply lists the image descriptors in the database.
 *
 * <p>Accesses the very same "WEB-INF/applicationContext.xml" file as the web
 * application. Needs to be executed in the web app root directory (that is,
 * with the web app root directory as JVM working directory).
 *
 * <p>Takes an optional command line argument that specifies the number of
 * calls to initiate for each image, a la "StandaloneImageTool 5". Can be
 * used to get a rough performance impression.
 *
 * @author Juergen Hoeller
 * @since 08.01.2004
 */
public class StandaloneImageTool {

	public static final String CONTEXT_CONFIG_LOCATION = "WEB-INF/applicationContext.xml";


	private final ImageDatabase imageDatabase;

	public StandaloneImageTool(ImageDatabase imageDatabase) {
		this.imageDatabase = imageDatabase;
	}

	public void listImages(int nrOfCalls) throws IOException {
		List images = this.imageDatabase.getImages();
		StopWatch stopWatch = new StopWatch();
		for (Iterator it = images.iterator(); it.hasNext();) {
			ImageDescriptor image = (ImageDescriptor) it.next();
			stopWatch.start(image.getName());
			ByteArrayOutputStream os = null;
			for (int i = 0; i < nrOfCalls; i++) {
				os = new ByteArrayOutputStream();
				this.imageDatabase.streamImage(image.getName(), os);
			}
			stopWatch.stop();
			System.out.println("Found image '" + image.getName() + "' with content size " + os.size() +
					" and description length " + image.getDescriptionLength());
		}
		System.out.println(stopWatch.prettyPrint());
	}


	public static void main(String[] args) throws IOException {
		int nrOfCalls = 1;
		if (args.length > 1 && !"".equals(args[1])) {
			nrOfCalls = Integer.parseInt(args[1]);
		}
		ApplicationContext context = new FileSystemXmlApplicationContext(CONTEXT_CONFIG_LOCATION);
		ImageDatabase idb = (ImageDatabase) context.getBean("imageDatabase");
		StandaloneImageTool tool = new StandaloneImageTool(idb);
		tool.listImages(nrOfCalls);
	}

}
