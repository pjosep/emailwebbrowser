package requestManager;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

/**
 * 
 * EmailImageAttacher handles attaching images to a message
 * 
 */
public class EmailImageAttacher {

	private static HashMap<String, String> extensionTypeMap = null;

	/**
	 * Constructor - sets up the internal members
	 */
	public EmailImageAttacher() {
		if (extensionTypeMap == null) {
			synchronized (EmailImageAttacher.class) {
				if (extensionTypeMap == null) {
					staticInit();
				}
			}
		}
	}

	/**
	 * Handles all initialization that only needs to take place one for this
	 * class
	 */
	private void staticInit() {
		extensionTypeMap = new HashMap<String, String>();
		extensionTypeMap.put("", "image/unkown");
		extensionTypeMap.put("bm", "image/bmp");
		extensionTypeMap.put("bmp", "image/bmp");
		extensionTypeMap.put("ras", "image/cmu-raster");
		extensionTypeMap.put("rast", "image/cmu-raster");
		extensionTypeMap.put("fif", "image/fif");
		extensionTypeMap.put("flo", "image/florian");
		extensionTypeMap.put("turbot", "image/florian");
		extensionTypeMap.put("g3", "image/g3fax");
		extensionTypeMap.put("gif", "image/gif");
		extensionTypeMap.put("ief", "image/ief");
		extensionTypeMap.put("iefs", "image/ief");
		extensionTypeMap.put("jfif", "image/jpeg");
		extensionTypeMap.put("jfif-tbnl", "image/jpeg");
		extensionTypeMap.put("jpe", "image/jpeg");
		extensionTypeMap.put("jpeg", "image/jpeg");
		extensionTypeMap.put("jpg", "image/jpeg");
		extensionTypeMap.put("jut", "image/jutvision");
		extensionTypeMap.put("nap", "image/naplps");
		extensionTypeMap.put("naplps", "image/naplps");
		extensionTypeMap.put("pic", "image/pict");
		extensionTypeMap.put("pict", "image/pict");
		extensionTypeMap.put("png", "image/png");
		extensionTypeMap.put("x-png", "image/png");
		extensionTypeMap.put("tif", "image/tiff");
		extensionTypeMap.put("tiff", "image/tiff");
		extensionTypeMap.put("mcf", "image/vasa");
		extensionTypeMap.put("dwg", "image/vnd.dwg");
		extensionTypeMap.put("dxf", "image/vnd.dwg");
		extensionTypeMap.put("svf", "image/vnd.dwg");
		extensionTypeMap.put("fpx", "image/vnd.fpx");
		extensionTypeMap.put("rf", "image/vnd.rn-realflash");
		extensionTypeMap.put("rp", "image/vnd.rn-realpix");
		extensionTypeMap.put("wbmp", "image/vnd.wap.wbmp");
		extensionTypeMap.put("xif", "image/vnd.xiff");
		extensionTypeMap.put("xbm", "image/xbm");
		extensionTypeMap.put("ico", "image/x-icon");
		extensionTypeMap.put("art", "image/x-jg");
		extensionTypeMap.put("jps", "image/x-jps");
		extensionTypeMap.put("nif", "image/x-niff");
		extensionTypeMap.put("niff", "image/x-niff");
		extensionTypeMap.put("pcx", "image/x-pcx");
		extensionTypeMap.put("pct", "image/x-pict");
		extensionTypeMap.put("xpm", "image/xpm");
		extensionTypeMap.put("pnm", "image/x-portable-anymap");
		extensionTypeMap.put("pbm", "image/x-portable-bitmap");
		extensionTypeMap.put("pgm", "image/x-portable-graymap");
		extensionTypeMap.put("ppm", "image/x-portable-pixmap");
		extensionTypeMap.put("qif", "image/x-quicktime");
		extensionTypeMap.put("qti", "image/x-quicktime");
		extensionTypeMap.put("qtif", "image/x-quicktime");
		extensionTypeMap.put("rgb", "image/x-rgb");
		extensionTypeMap.put("pm", "image/x-xpixmap");
		extensionTypeMap.put("xwd", "image/x-xwd");
	}

	/**
	 * Attaches a Collection of Images to an email
	 * 
	 * @param stringImageCollection
	 *            collection of image data
	 * @param multipart
	 *            multipart to attach the image data to
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void attachImages(
			Collection<UrlPathImageData> stringImageCollection,
			Multipart multipart) throws MessagingException, IOException {
		for (UrlPathImageData urlPathImage : stringImageCollection) {
			attachImage(multipart, urlPathImage);
		}
		System.out.println("SIZE: " + stringImageCollection.size());
	}

	/**
	 * Attach a single image to the multipart
	 * 
	 * @param multipart
	 *            multipart of an email to attach image
	 * @param urlPathImage
	 *            image data
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void attachImage(Multipart multipart, UrlPathImageData urlPathImage)
			throws MessagingException, IOException {
		String ext = urlPathImage.getOriginalNameExt();
		String imageType = extensionTypeMap.get(ext.trim().toLowerCase());
		if (imageType == null)
			imageType = "image/uknown";
		ByteArrayDataSource source = new ByteArrayDataSource(urlPathImage
				.getImageData(), imageType);

		BodyPart messageBodyPart = new MimeBodyPart();

		messageBodyPart.setDisposition(Part.ATTACHMENT);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setHeader("Content-ID", "<" + urlPathImage.getRenamed()
				+ ">");
		messageBodyPart.setFileName(urlPathImage.getOriginalNameNoPath());

		// Add part to multi-part
		multipart.addBodyPart(messageBodyPart);
	}

}
