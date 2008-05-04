package requestManagerTest;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;
import requestManager.EmailReader;

/**
 * 
 * Tests all public functions in the EmailReader
 * 
 */
public class EmailReaderTest extends TestCase {

	static public String user = "emailwebbrowser";

	static public String password = "cs428pass";

	/**
	 * Verify that we are able to read and retrieve emails successfully
	 * 
	 * @throws Exception
	 */
	public void testConnectSuccess() throws Exception {

		EmailReader myReader = new EmailReader();

		boolean success = myReader.getMessages(user, password, "foo");
		assertEquals(true, success);

		int msgCount = myReader.getMessageCount();
		assert (msgCount >= 1);

		Session session = setupEmail();
		Message message = new MimeMessage(session);
		message.setSubject("foo");
		message
				.setContent(
						"getWebPage http://tantek.com/CSS/Examples/codeisfreespeech.html \r\n http://www.cnn.com \r\n http://www.google.com",
						"text/plain");

		sendEmail(user, password, "emailwebbrowser@gmail.com", session, message);

		success = myReader.getMessages(user, password, "foo");
		assertEquals(true, success);

		msgCount = myReader.getMessageCount();
		assert (msgCount >= 1);

		success = myReader.triggerResponses();
		assertEquals(true, success);

		assert (myReader.getMessageCount() >= msgCount + 1);
	}

	/**
	 * Sends an email
	 * 
	 * @param from
	 *            the sender's google username
	 * @param pass
	 *            password
	 * @param to
	 *            the receiver's email address
	 * @param session
	 *            email session
	 * @param message
	 *            message to be sent
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 */
	static public void sendEmail(String from, String pass, String to,
			Session session, Message message) throws MessagingException,
			AddressException, NoSuchProviderException {

		message.setFrom(new InternetAddress(from + "@gmail.com"));
		message.setRecipients(Message.RecipientType.TO,
				new InternetAddress[] { new InternetAddress(to) });

		Transport transport = session.getTransport("smtp");

		transport.connect("smtp.gmail.com", from, password);
		transport.sendMessage(message, message
				.getRecipients(Message.RecipientType.TO));
		transport.close();
	}

	/**
	 * Sets up the email account Session to read and write emails from/to
	 * 
	 * @return Session
	 */
	static public Session setupEmail() {
		Session session = null;
		Properties props = System.getProperties();

		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", String.valueOf("25"));
		props.put("mail.smtp.starttls.enable", "true");

		// create a new Session object
		session = Session.getInstance(props, null);
		return session;
	}
}
