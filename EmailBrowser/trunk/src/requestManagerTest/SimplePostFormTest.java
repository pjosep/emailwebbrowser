package requestManagerTest;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import requestManager.EmailReader;

/**
 * 
 * Tests the receiving an email request for a webpage (with a simple POST form)
 * and the return of that email.
 * 
 */
public class SimplePostFormTest extends TestCase {

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
	 * Tests the response system for email requests using a webpage with a
	 * simple POST form images
	 * 
	 */
	public void testOperation() throws Exception {

		String testRequest = "http://csil-projects.cs.uiuc.edu/~pjoseph2/cgi-bin/mycgi.pl --POST:";
		String testForms[][][] = { { { "field1", "testVal" } },
				{ { "field1", "testVal" }, { "field2", "testVal2" } } };

		for (int i = 0; i < testForms.length; i++) {
			String request = testRequest;
			for (int j = 0; j < testForms[i].length; j++) {
				request += testForms[i][j][0] + "=" + testForms[i][j][1]
						+ "\r\n";
			}
			RequestManagerOneImageTest.sendWebpageRequest(request);

			RequestManagerOneImageTest.triggerResponse();

			EmailReader myReaderResponse = new EmailReader();
			Message[] myMessages = RequestManagerOneImageTest
					.getResponseMessages(myReaderResponse);

			MimeMultipart myMultipart = (MimeMultipart) myMessages[0]
					.getContent();

			// There should be one parts in this email

			assertEquals(1, myMultipart.getCount());

			// The content type must be a multipart

			assertTrue(myMessages[0].getContentType().contains(
					"multipart/RELATED"));

			BodyPart myBodyPart = myMultipart.getBodyPart(0);
			String content = (String) myBodyPart.getContent();

			assertTrue(myBodyPart.getContentType().contains("TEXT/HTML"));

			for (int j = 0; j < testForms[i].length; j++) {
				assertTrue(content.contains(testForms[i][j][0]));
				assertTrue(content.contains(testForms[i][j][1]));
			}
			/*
			 * We already checked we got the right number of parts in the email,
			 * let's know check whether we got the right types.
			 */

			myReaderResponse.endReads();
		}
	}
}
