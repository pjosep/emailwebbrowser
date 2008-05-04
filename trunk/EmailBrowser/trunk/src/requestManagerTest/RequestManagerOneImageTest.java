package requestManagerTest;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import requestManager.EmailReader;

/**
 * 
 * Tests the receiving an email request for a webpage (with one image) and the
 * return of that email.
 * 
 */
public class RequestManagerOneImageTest extends TestCase {

	static public String user = "emailbrowser2";

	static public String password = "cs428pass";

	static public String useJavaMockMail = "Use JavaMockMail";

	static public String subject = "foo";

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

		sendWebpageRequest("http://csil-projects.cs.uiuc.edu/~tjbrenna/emailbrowsertests/MonaOneImageTest.html");

		triggerResponse();

		EmailReader myReaderResponse = new EmailReader();
		Message[] myMessages = getResponseMessages(myReaderResponse);

		MimeMultipart myMultipart = (MimeMultipart) myMessages[0].getContent();

		// There should be two parts in this email, the first being
		// text and the other the image

		assertEquals(myMultipart.getCount(), 2);

		// The content type must be a multipart

		assertTrue(myMessages[0].getContentType().contains("multipart/RELATED"));

		BodyPart myBodyPart = myMultipart.getBodyPart(0);

		assertTrue(myBodyPart.getContentType().contains("TEXT/HTML"));

		// Now we will get the image (the second part in the
		// multipart message)

		myBodyPart = myMultipart.getBodyPart(1);

		assertTrue(myBodyPart.getContentType().contains("IMAGE/JPEG"));

		assertTrue(myBodyPart.getContentType().contains("DSCN0012.jpg"));

		System.out.println("part1: " + myBodyPart.getSize());

		myReaderResponse.endReads();
	}

	/**
	 * Get response messages from the email request
	 * 
	 * @param myReaderResponse
	 *            email reader used to get response
	 * @return
	 */
	static public Message[] getResponseMessages(EmailReader myReaderResponse) {

		boolean success = myReaderResponse.getMessages(user, password, subject);
		success = myReaderResponse.getUnseenMessages("Re: " + subject.trim());
		assertEquals(true, success);

		Message[] myMessages = myReaderResponse.getCachedMessages();

		assertEquals(myMessages.length, 1);

		return myMessages;
	}

	/**
	 * Call EmailReader's trigger method to cause a response to a webpage
	 * request
	 * 
	 * @param myReader
	 */
	static public void triggerResponse() {
		EmailReader myReader = new EmailReader();
		boolean success = myReader.getMessages(user, password, "foo");

		assertEquals(true, success);

		int count = myReader.getMessageCount();
		while (count == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			myReader.endReads();
			myReader.getMessages(user, password, "foo");
			count = myReader.getMessageCount();
		}

		success = myReader.triggerResponses();

		assertEquals(true, success);

		myReader.endReads();
	}

	/**
	 * Sends a webpage request via email
	 * 
	 * @param url
	 *            webpage being requested
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 */
	static public void sendWebpageRequest(String url)
			throws MessagingException, AddressException,
			NoSuchProviderException {
		EmailReader myReader = new EmailReader();

		boolean success = myReader.getMessages(user, password, "foo");

		assertEquals(true, success);

		// Let's first delete all messages in the Inbox
		// this way we make sure we are dealing with
		// the particular request we will send in a bit

		success = myReader.deleteAllMessagesInInbox();

		assertEquals(true, success);

		int msgCount = myReader.getMessageCount();

		assert (msgCount == 0);

		Session session = EmailReaderTest.setupEmail();
		Message message = new MimeMessage(session);
		message.setSubject("foo");

		// We now set the URL we are requesting

		message.setContent(url, "text/plain");

		EmailReaderTest.sendEmail(user, password, user + "@gmail.com", session,
				message);

		myReader.endReads();
	}
}
