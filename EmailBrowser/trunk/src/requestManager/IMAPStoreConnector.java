package requestManager;

import java.security.Security;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

/**
 * 
 * IMAPStoreConnector is used to communicate over email using the IMAP protocol
 * 
 */
public class IMAPStoreConnector implements EmailStoreConnector {

	private final String imapEmailHost = "imap.gmail.com";

	final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	private int messageCount = -1;

	private boolean debugOn = false;

	private Store store = null;

	private Folder folder = null;

	Session session = null;

	String accountUser;

	String accountPassword;

	/**
	 * Constructor -- sets up the session properties to connect to a gmail mail
	 * account
	 */

	public IMAPStoreConnector() {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		Properties sysProperties = new Properties();

		sysProperties.setProperty("mail.imap.port", "993");
		sysProperties.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);

		sysProperties.setProperty("mail.transport.protocol", "smtp");
		sysProperties.setProperty("mail.host", "smptp.gmail.com");

		sysProperties.put("mail.smtp.auth", "true");

		session = Session.getInstance(sysProperties, null);
		session.setDebug(debugOn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see requestManager.EmailStoreConnector#Connect(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean connect(String user, String password) {

		System.out.println("IMAPStoreConnector: connect()");

		accountUser = user;
		accountPassword = password;

		if (session == null) {

			return false;

		}
		try {
			if (store != null) {
				store.close();
			}

			this.initializeSession();

			store = session.getStore("imap");
			if (store.isConnected()) {
				return true;
			}

			store.connect(imapEmailHost, user, password);
		} catch (Exception ex) {
			System.out.println("IMAPStoreConnector: exception: "
					+ ex.toString());
			return false;
		}

		if (store == null) {
			return false;
		}

		try {

			folder = store.getFolder("INBOX");

			folder.open(Folder.READ_WRITE);

			System.out.println("Got this folder: " + folder.getFullName());

			messageCount = folder.getMessageCount();

			if (folder == null)
				return false;

		} catch (Exception ex) {
			return false;
		}
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see requestManager.EmailStoreConnector#Terminate()
	 */
	public boolean Terminate() {
		try {
			store.close();
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see requestManager.EmailStoreConnector#getMessages()
	 */
	public Message[] getMessages(String subject) {

		System.out.println("IMAMPEmailStoreConnector: getMessages()");

		Message[] myMessages = null;

		if (folder == null)
			return myMessages;
		try {
			SearchTerm st = new AndTerm(new SubjectTerm(subject), new FlagTerm(
					new Flags(Flags.Flag.SEEN), false));
			myMessages = folder.search(st);
			System.out.println("IMAPEmailStoreConnector: myMessages.length: "
					+ myMessages.length);
		} catch (Exception ex) {
			System.out.println("IMAPEmailStoreConnector: " + ex.getMessage());
			return myMessages;
		}

		for (int i = 0; i < myMessages.length; i++) {
			try {
				myMessages[i].setFlag(Flags.Flag.SEEN, true);
			} catch (Exception ex) {
				System.out.println("There was an error setting the SEEN flag: "
						+ ex.getMessage());
			}
		}

		return myMessages;

	}

	/**
	 * Retrieves unseen messages from the Inbox
	 * 
	 * @param subject
	 *            subject of emails to retrieve
	 * @return unseen messages that match subject in Inbox
	 */

	public Message[] getUnseenMessages(String subject) {

		System.out.println("IMAPEmailStoreConnector: getMessages2()");

		Message[] myMessages = null;

		if (folder == null)
			return myMessages;
		try {
			SearchTerm st = new AndTerm(new SubjectTerm(subject), new FlagTerm(
					new Flags(Flags.Flag.SEEN), true));
			myMessages = folder.search(st);
		} catch (Exception ex) {
			System.out.println("getMessages2: exception " + ex.getMessage());
			return myMessages;
		}

		return myMessages;
	}

	/*
	 * REALLY DANGEROUS METHOD, ONLY CALL ON TEST MAILBOXES
	 */

	/**
	 * Deletes all the messages in the Inbox
	 * 
	 * @return true if successful
	 */

	public boolean deleteAllMessagesInInbox() {

		try {

			Message[] myMessages = folder.getMessages();

			if (myMessages.length == 0)
				return true;

			for (int i = 0; i < myMessages.length; i++) {

				myMessages[i].setFlag(Flags.Flag.DELETED, true);
			}
		} catch (Exception ex) {
			System.out
					.println("exception raised while deleting messages from INBOX: "
							+ ex.toString());
			return false;

		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see requestManager.EmailStoreConnector#sendMessage(javax.mail.Message)
	 */
	public void sendMessage(Message theMessage) {
		try {
			System.out.println("1:");
			Properties props = System.getProperties();

			session = Session.getDefaultInstance(props, null);
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", String.valueOf("25"));
			props.put("mail.smtp.starttls.enable", "true");
			System.out.println("2:");

			Transport transport = session.getTransport("smtp");

			transport.connect("smtp.gmail.com", accountUser, accountPassword);
			transport.sendMessage(theMessage, theMessage
					.getRecipients(Message.RecipientType.TO));
			System.out.println("3:");
			transport.close();
			System.out.println("4:");
		} catch (Exception ex) {
			System.out.println("sendMessage: exception: " + ex.toString());
		}
	}

	/**
	 * 
	 * @return message count in the inbox
	 */
	public int getMessageCount() {
		return messageCount;
	}

	private void initializeSession() {

		Properties sysProperties = new Properties();

		sysProperties.setProperty("mail.imap.port", "993");
		sysProperties.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);

		sysProperties.setProperty("mail.transport.protocol", "smtp");
		sysProperties.setProperty("mail.host", "smptp.gmail.com");

		sysProperties.put("mail.smtp.auth", "true");

		session = Session.getInstance(sysProperties, null);
		session.setDebug(debugOn);
	}
}