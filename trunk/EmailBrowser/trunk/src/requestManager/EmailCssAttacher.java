package requestManager;

import java.io.IOException;
import java.util.Collection;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

/**
 * 
 * EmailCssAttacher handles attaching CSS pages to a message
 * 
 */
public class EmailCssAttacher {

	/**
	 * Constructor - sets up the internal members
	 */
	public EmailCssAttacher() {

	}

	/**
	 * Attaches a Collection of CSS Pages to an email
	 * 
	 * @param stringImageCollection
	 *            collection of CSS data
	 * @param multipart
	 *            multipart to attach the CSS data to
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void attachExternals(Collection<UrlPathCssData> stringCssCollection,
			Multipart multipart) throws MessagingException, IOException {
		for (UrlPathCssData urlPathCss : stringCssCollection) {
			attachExternal(multipart, urlPathCss);
		}
		System.out.println("SIZE: " + stringCssCollection.size());
	}

	/**
	 * Attach a single image to the multipart
	 * 
	 * @param multipart
	 *            multipart of an email to attach CSS
	 * @param urlPathCssData
	 *            urlPathCss - URL path of CSS Page
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void attachExternal(Multipart multipart, UrlPathCssData urlPathCss)
			throws MessagingException, IOException {

		ByteArrayDataSource source = new ByteArrayDataSource(urlPathCss
				.getCssData().getBytes(), "text/css");

		BodyPart messageBodyPart = new MimeBodyPart();

		messageBodyPart.setDisposition(Part.ATTACHMENT);
		messageBodyPart.setDataHandler(new DataHandler(source));

		System.out.println("IMPORTANT1:" + urlPathCss.getRenamed());

		messageBodyPart.setHeader("Content-ID", "<" + urlPathCss.getRenamed()
				+ ">");
		messageBodyPart.setFileName(urlPathCss.getOriginalNameNoPath());

		// Add part to multi-part
		multipart.addBodyPart(messageBodyPart);
	}

}