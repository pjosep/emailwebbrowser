package requestManagerTest;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import requestManager.EmailReader;

/**
 * 
 * Tests the receiving an email request for a webpage (with five images) and the
 * return of that email.
 * 
 */
public class RequestManagerFiveImageTest extends TestCase {

	/*
	 * DANGER DANGER DANGER
	 * 
	 * This test first deletes the contents of the mailbox.
	 * 
	 * ALL EMAILS IN THE INBOX ARE DELETED
	 * 
	 * DANGER DANGER DANGER
	 */

	/**
	 * 
	 * Tests the response system for email requests using a webpage with 5
	 * images
	 * 
	 */
	public void testOperation() throws Exception {

		RequestManagerOneImageTest
				.sendWebpageRequest("http://csil-projects.cs.uiuc.edu/~tjbrenna/emailbrowsertests/MonaFiveImageTest.html");

		RequestManagerOneImageTest.triggerResponse();

		EmailReader myReaderResponse = new EmailReader();
		Message[] myMessages = RequestManagerOneImageTest
				.getResponseMessages(myReaderResponse);

		MimeMultipart myMultipart = (MimeMultipart) myMessages[0].getContent();

		// There should be two parts in this email, the first being
		// text and the others the five images

		assertEquals(myMultipart.getCount(), 6);

		// The content type must be a multipart

		assertTrue(myMessages[0].getContentType().contains("multipart/RELATED"));

		BodyPart myBodyPart = myMultipart.getBodyPart(0);

		assertTrue(myBodyPart.getContentType().contains("TEXT/HTML"));

		String contentTypeParts = "";

		for (int i = 0; i < 6; i++) {
			myBodyPart = myMultipart.getBodyPart(i);
			contentTypeParts += myBodyPart.getContentType();
		}

		/*
		 * We already checked we got the right number of parts in the email,
		 * let's know check whether we got the right types.
		 */

		assertTrue(contentTypeParts.contains("BMP"));
		assertTrue(contentTypeParts.contains("JPEG"));
		assertTrue(contentTypeParts.contains("PNG"));
		assertTrue(contentTypeParts.contains("GIF"));

		myReaderResponse.endReads();
	}
}
