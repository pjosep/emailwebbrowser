package requestManager;

import javax.mail.Message;

/**
 * 
 * EmailStoreConnector acts as an interface so that we can use different email
 * store connector programs to communicate via email.
 * 
 */
public interface EmailStoreConnector {

	/**
	 * Connect to email store and get the number of messages in the inbox
	 * 
	 * @param user
	 *            username to log into the email store
	 * @param password
	 *            password to log into the email store
	 * @return true if connection is successful
	 */
	public abstract boolean connect(String user, String password);

	/**
	 * Terminate connection to the email store
	 * 
	 * @return true if disconnection is successful
	 */
	public abstract boolean Terminate();

	/**
	 * Retrieve messages from inbox that are unread and that have subject in
	 * their title. Mark all such messages as read
	 * 
	 * @param subject
	 *            subject of the email to match on
	 * @return messages from the inbox
	 */
	public abstract Message[] getMessages(String subject);

	/**
	 * Send a message using the email store
	 * 
	 * @param theMessage
	 *            email message to send
	 */
	public abstract void sendMessage(Message theMessage);

}