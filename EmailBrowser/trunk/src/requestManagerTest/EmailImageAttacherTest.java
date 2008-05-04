package requestManagerTest;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import requestManager.EmailImageAttacher;
import requestManager.IMAPStoreConnector;
import requestManager.UrlPathImageData;

import com.sun.mail.util.BASE64DecoderStream;

/**
 * 
 * Tests all public functions in the EmailImageAttacher
 * 
 */
public class EmailImageAttacherTest extends TestCase {

	static public String testImagePath = "src" + File.separator
			+ "requestManagerTest" + File.separator + "images" + File.separator;

	static public String testUrlPath = "src" + File.separator
			+ "requestManagerTest" + File.separator + "pages" + File.separator;

	/**
	 * Tests the attachment of Images (in the form of byte arrays) to email
	 * 
	 * @throws Exception
	 */
	public void testAttachImagesByteArray() throws Exception {
		String[] imageFileNames = { "01.gif", "02.jpg", "03.BMP", "04.png",
				"05.pcx", "06.tif", "07.jpeg" };

		byte[][] testImages = sendMsgWithImages(imageFileNames);

		// wait until email arrives
		Thread.sleep(1000);

		// Connect to the email and get all the messages
		IMAPStoreConnector myConnector = new IMAPStoreConnector();
		if (myConnector.connect(EmailReaderTest.user, EmailReaderTest.password) == false) {
			assertTrue("Code should never reach here", false);
		}
		Message[] myMessages = myConnector
				.getMessages("EmailImageAttacherTest");
		assertEquals(myMessages.length, 1);
		Message msg = myMessages[myMessages.length - 1];
		MimeMultipart myMultipart = (MimeMultipart) msg.getContent();

		assertEquals(myMultipart.getCount(), imageFileNames.length + 1);

		// The content type must be a multipart

		assertTrue(msg.getContentType().contains("multipart/RELATED"));

		// the first multipart is going to be the html
		BodyPart myBodyPart = myMultipart.getBodyPart(0);
		assertTrue(myBodyPart.getContentType().contains("TEXT/HTML"));

		// for each of the other multiparts, verify that the image is found
		for (int i = 1; i < imageFileNames.length; i++) {
			BodyPart test = myMultipart.getBodyPart(i);
			Object content = test.getContent();

			if (content instanceof BASE64DecoderStream) {
				byte[] decodedBytes = read2ByteArray((BASE64DecoderStream) content);

				boolean found = false;
				for (int j = 0; j < imageFileNames.length; j++) {
					if (testImages[j].length == decodedBytes.length) {
						if (Arrays.equals(testImages[j], decodedBytes)) {
							found = true;
							testImages[j] = new byte[0];
							break;
						}
					}
				}
				assertTrue("Image " + test.getFileName()
						+ " was not found in the email", found);
			} else {
				assertTrue(
						"There should be no other multiparts other than base 64 ones",
						false);
			}
		}

		myConnector.Terminate();

	}

	/**
	 * Helper function that sends an email with images to emailwebbrowser
	 * 
	 * @param imageFileNames
	 *            array of image file paths
	 * @return an array of byte arrays of the image data that was sent
	 * @throws MessagingException
	 * @throws IOException
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 */
	private byte[][] sendMsgWithImages(String[] imageFileNames)
			throws MessagingException, IOException, AddressException,
			NoSuchProviderException {
		EmailImageAttacher imageAttacher = new EmailImageAttacher();
		HashMap<String, UrlPathImageData> stringImageHash = new HashMap<String, UrlPathImageData>();
		byte[][] testImages = new byte[imageFileNames.length][];

		String htmlText = "";

		for (int i = 0; i < imageFileNames.length; i++) {
			testImages[i] = read2ByteArray(testImagePath + imageFileNames[i]);
			UrlPathImageData pathImageData = new UrlPathImageData(
					imageFileNames[i], imageFileNames[i], testImages[i]);
			stringImageHash.put(imageFileNames[i], pathImageData);
			htmlText += "<H1>Testing file " + pathImageData.getRenamed()
					+ "</H1>" + "<img src=\"cid:" + pathImageData.getRenamed()
					+ "\">\r\n";
		}

		Session session = EmailReaderTest.setupEmail();
		Message message = new MimeMessage(session);

		// Create your new message part
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(htmlText, "text/html");

		// Create a related multi-part to combine the parts
		MimeMultipart multipart = new MimeMultipart("related");
		multipart.addBodyPart(messageBodyPart);

		imageAttacher.attachImages(stringImageHash.values(), multipart);

		// Associate multi-part with message
		message.setContent(multipart);
		message.setSubject("EmailImageAttacherTest");

		EmailReaderTest.sendEmail(EmailReaderTest.user,
				EmailReaderTest.password, "emailwebbrowser@gmail.com", session,
				message);
		return testImages;
	}

	/**
	 * Reads a file and returns its contents as a byte array
	 * 
	 * @param filePath
	 *            path of the file being read
	 * @return byte array of the contents of the filePath
	 */
	public static byte[] read2ByteArray(String filePath) {
		byte[] bytes = null;
		try {
			File file = new File(filePath);
			FileInputStream file_input = new FileInputStream(file);
			DataInputStream data_in = new DataInputStream(file_input);
			bytes = new byte[(int) file.length()];
			data_in.read(bytes);

			data_in.close();
			file_input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	/**
	 * Takes a BASE64DecoderStream and returns the byte array of data in it
	 * 
	 * @param in
	 *            base 64 decoder stream
	 * @return byte array of the contents of the base 64 decoder stream
	 */
	public byte[] read2ByteArray(BASE64DecoderStream in) throws Exception {
		byte[] buf = null; // output buffer
		int bufLen = 20000 * 1024;
		try {
			buf = new byte[bufLen];
			byte[] tmp = null;
			int len = 0;

			List<byte[]> data = new ArrayList<byte[]>(24); // keeps pieces of
			// data
			while ((len = in.read(buf, 0, bufLen)) != -1) {
				tmp = new byte[len];
				System.arraycopy(buf, 0, tmp, 0, len); // still need to do copy
				data.add(tmp);
			}

			/*
			 * This part is optional. This method could return a List data for
			 * further processing, etc.
			 */
			len = 0;
			if (data.size() == 1) {
				return data.get(0);
			}

			for (int i = 0; i < data.size(); i++) {
				len += (data.get(i)).length;
			}

			buf = new byte[len]; // final output buffer
			len = 0;

			for (int i = 0; i < data.size(); i++) { // fill with data
				tmp = data.get(i);
				System.arraycopy(tmp, 0, buf, len, tmp.length);
				len += tmp.length;
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return buf;
	}
}
