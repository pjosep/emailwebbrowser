package requestManager;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * EmailReader is responsible for reading emails and parsing them to determine
 * the webpages that are being requested.
 * 
 * Upon determining the webpages that are being requested, the EmailReader then
 * facilitates the retrieval of the actual page, and the populating of the
 * response email with the html and associated data.
 * 
 */
public class EmailReader {

	private IMAPStoreConnector myConnector;

	private Message[] cachedMessages;

	/**
	 * Constructor - sets up the email store connector
	 */
	public EmailReader() {
		myConnector = new IMAPStoreConnector();
	}

	/**
	 * Retrieves messages from the email store connector
	 * 
	 * @param user
	 *            user login
	 * @param password
	 *            password
	 * @param subject
	 *            subject of email
	 * @return true if messages were able to be retrieved
	 */
	public boolean getMessages(String user, String password, String subject) {
		if (myConnector == null) {
			return false;
		}
		if (myConnector.connect(user, password) == false) {
			return false;
		}

		cachedMessages = myConnector.getMessages(subject);

		return true;

	}

	/**
	 * Delete all messages in Inbox
	 * 
	 * @return true if successful
	 */
	public boolean deleteAllMessagesInInbox() {
		return myConnector.deleteAllMessagesInInbox();
	}

	/**
	 * Populates cached messages with unseen messages that match subject
	 * 
	 * @param subject
	 *            subject of emails to retrieve
	 * @return true if successful
	 */
	public boolean getUnseenMessages(String subject) {
		if (myConnector == null) {
			return false;
		}

		cachedMessages = myConnector.getUnseenMessages(subject);

		return true;

	}

	/**
	 * 
	 * @return cached messages
	 */
	public Message[] getCachedMessages() {
		return cachedMessages;
	}

	/**
	 * Handles the responses to emails received
	 * 
	 * @return true if email store connector logouts
	 */
	public boolean triggerResponses() {

		System.out.println(cachedMessages.length
				+ " messages found with specified subject.");

		processMessages(cachedMessages);

		return true;

	}

	/**
	 * Terminates connection to email
	 * 
	 * @return true if successful
	 */
	public boolean endReads() {
		return myConnector.Terminate();

	}

	/**
	 * For each message in the inbox, process its contents
	 * 
	 * @param cachedMessages
	 *            input messages
	 */
	void processMessages(Message[] cachedMessages) {

		if (!(cachedMessages.length > 0)) {
			return;
		}

		for (int i = 0; i < cachedMessages.length; i++) {
			try {
				processMessageContents(cachedMessages[i]);
			} catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());
			}
		}
	}

	/**
	 * Pull out all the text from the email text and process the text.
	 * 
	 * @param message
	 *            email message
	 * @throws IOException
	 * @throws MessagingException
	 * @throws Exception
	 * @throws InterruptedException
	 */
	private void processMessageContents(Message message) throws IOException,
			MessagingException, Exception {
		MimeMessage cmsg = new MimeMessage((MimeMessage) message);
		Object obj = cmsg.getContent();
		Set<String> strings = new HashSet<String>();

		if (obj instanceof Multipart) {
			Multipart mp1 = (Multipart) obj;
			for (int n = 0; n < mp1.getCount(); n++) {
				String body = mp1.getBodyPart(n).getContent().toString();
				System.out.println("This is the body: " + body);
				strings.add(body);
			}
		} else if (obj instanceof String) {
			strings.add((String) obj);
			System.out.println("This is the body: " + (String) obj);
		}

		for (String str : strings) {
			processMessageString(message, str);
		}

	}

	/**
	 * Retrieve all the requested URLS and compose and send a response email
	 * 
	 * @param message
	 *            email message
	 * @param emailBody
	 *            email text
	 * @throws Exception
	 * @throws MessagingException
	 */
	private void processMessageString(Message message, String emailBody) {
		EmailBodyProcessor emailBodyProcessor = new EmailBodyProcessor(
				emailBody);

		try {
			HashSet<URL> pageRequests = emailBodyProcessor.parseEmailBody();
			for (URL url : pageRequests) {
				composeAndSendReponse(message, emailBodyProcessor, url);
			}
		} catch (MalformedEWBRequest malEWBR) {
			System.out
					.println("EmailReader: malformed EmailWebBrowser request received");
			composeAndSendFailResponse(message);
		} catch (Exception eX) {

		}
	}

	private void composeAndSendFailResponse(Message message) {
		String pageHTML = "<html> Malformed EmailWebBrowser request received, please visit http://www.emailwebbrowser.org request email formating for more information";

		MimeMultipart multipart = new MimeMultipart("related");

		try {
			MimeBodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setContent(pageHTML, "text/html");

			MimeMessage returnMessage = (MimeMessage) message.reply(false);

			returnMessage.setContent(multipart);

			multipart.addBodyPart(messageBodyPart);

			myConnector.sendMessage(returnMessage);
		} catch (Exception eX) {

		}
	}

	/**
	 * Retrieve all web data (html, images, etc...), rewrite the html tags,
	 * attach the html to the images, and send the response message
	 * 
	 * @param message
	 *            message being responded to
	 * @param emailBodyProcessor
	 *            email body processor
	 * @param browseURL
	 *            url being returned
	 * @throws Exception
	 * @throws MessagingException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void composeAndSendReponse(Message message,
			EmailBodyProcessor emailBodyProcessor, URL browseURL)
			throws Exception, MessagingException, IOException,
			InterruptedException {

		emailBodyProcessor.processUrl(browseURL);

		HashMap<String, UrlPathCssData> pathCssHash = new HashMap<String, UrlPathCssData>();

		HashMap<String, UrlPathImageData> pathImageHash = new HashMap<String, UrlPathImageData>();

		String pageHTML = rewriteHtmlTags(message, emailBodyProcessor,
				pathImageHash, pathCssHash);

		MimeMessage returnMessage = attachHtmlAndImages(message, pathImageHash,
				pathCssHash, pageHTML);

		myConnector.sendMessage(returnMessage);
	}

	/**
	 * Attach the rewritten HTML to the email and then attach the images to the
	 * email.
	 * 
	 * @param message
	 *            email message being responded to
	 * @param pathImageHash
	 *            hashmap of the string to the urlPathImageData
	 * @param pathCssHash
	 *            hashmap of the string to the urlPathCssData
	 * @param pageHTML
	 *            html data
	 * @return response message to be sent to the user
	 * @throws MessagingException
	 * @throws IOException
	 */
	private MimeMessage attachHtmlAndImages(Message message,
			HashMap<String, UrlPathImageData> pathImageHash,
			HashMap<String, UrlPathCssData> pathCssHash, String pageHTML)
			throws MessagingException, IOException, InterruptedException {
		EmailImageAttacher imageAttacher = new EmailImageAttacher();
		EmailCssAttacher cssAttacher = new EmailCssAttacher();

		MimeMultipart multipart = new MimeMultipart("related");

		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(pageHTML, "text/html");

		System.out.println(pageHTML);

		multipart.addBodyPart(messageBodyPart);

		imageAttacher.attachImages(pathImageHash.values(), multipart);

		cssAttacher.attachExternals(pathCssHash.values(), multipart);

		MimeMessage returnMessage = (MimeMessage) message.reply(false);

		System.out.println("MULTIPART SIZE: " + multipart.getCount());

		returnMessage.setContent(multipart);
		return returnMessage;
	}

	/**
	 * Rewrite the HTML Image tags with references to the attached images & Css
	 * 
	 * @param emailBodyProcessor
	 *            email body processor
	 * @param pathImageHash
	 *            hashmap of the string to the urlPathImage data
	 * @param pathCssHash
	 *            hashmap of the string to the urlPathCss data
	 * @return the rewritten HTML
	 */
	private String rewriteHtmlTags(Message message,
			EmailBodyProcessor emailBodyProcessor,
			HashMap<String, UrlPathImageData> pathImageHash,
			HashMap<String, UrlPathCssData> pathCssHash)
			throws MessagingException {

		HashMap<String, byte[]> thePageImages = emailBodyProcessor
				.getPageImages();

		HashMap<String, String> thePageCss = emailBodyProcessor.getThePageCss();

		// rewrite HTML tags for Images
		rewriteImageTags(emailBodyProcessor, thePageImages, pathImageHash);

		// rewrite HTML tags for CSS
		rewriteCssTags(emailBodyProcessor, thePageCss, pathCssHash);

		// rewrite HTML tags for mailTo
		rewriteMailToTags(message, emailBodyProcessor);

		// rewrite HTML tags for Forms
		rewriteFormTags(message, emailBodyProcessor);

		// return rewritten HTML
		return emailBodyProcessor.getFullList().toHtml();
	}

	/**
	 * 
	 * @return the number of messages in the inbox
	 */
	public int getMessageCount() {
		return cachedMessages.length;
	}

	/**
	 * Rewrite the HTML Form tags with references to the attached images
	 * 
	 * @param message
	 *            email message
	 * @param emailBodyProcessor
	 *            email body processor
	 */
	private void rewriteFormTags(Message message,
			EmailBodyProcessor emailBodyProcessor) throws MessagingException {

		NodeList formList = emailBodyProcessor.getFullList()
				.extractAllNodesThatMatch(new NodeClassFilter(FormTag.class),
						true);
		for (SimpleNodeIterator nodeIter = formList.elements(); nodeIter
				.hasMoreNodes();) {
			FormTag node = (FormTag) nodeIter.nextNode();
			String formMethod = node.getFormMethod();
			String formLocation = node.getFormLocation();
			Address[] addresses = message
					.getRecipients(Message.RecipientType.TO);
			if (formLocation != "") {
				if (addresses.length > 0) {
					node.setFormLocation("mailto:" + addresses[0] + "?subject="
							+ message.getSubject() + "&body=" + formLocation
							+ " --" + formMethod + ":");
					node.setAttribute("enctype",
							"application/x-www-form-urlencoded");
					node.setAttribute("method", "post");
				}
			}
		}
	}

	/**
	 * Rewrite the HTML anchor tags <a href="http://maps.google.com"> tags with
	 * mailTo tags <a
	 * href="mailTo:emailAddress@gmail.com&subject=foo&body="http://maps.google.com"
	 * 
	 * @param message
	 *            email message
	 * @param emailBodyProcessor
	 *            email body processor
	 * 
	 */
	private void rewriteMailToTags(Message message,
			EmailBodyProcessor emailBodyProcessor) throws MessagingException {
		NodeList anchorList = emailBodyProcessor.getFullList()
				.extractAllNodesThatMatch(new TagNameFilter("a"), true);
		for (SimpleNodeIterator nodeIter = anchorList.elements(); nodeIter
				.hasMoreNodes();) {
			TagNode tempNode = (TagNode) nodeIter.nextNode();
			String originalTagText = tempNode.getAttribute("href");
			if (originalTagText != null) {
				if (!originalTagText.startsWith("#")) {
					originalTagText = tempNode.getPage().getAbsoluteURL(
							originalTagText);
					Address[] addresses = message
							.getRecipients(Message.RecipientType.TO);
					if (addresses.length > 0) {
						String updatedTagText = "mailto:" + addresses[0]
								+ "?subject=" + message.getSubject() + "&body="
								+ originalTagText;
						tempNode.setAttribute("href", updatedTagText);
					}
				}
			}
		}
	}

	/**
	 * Rewrite the HTML Image tags with references to the attached images
	 * 
	 * @param emailBodyProcessor
	 *            email body processor
	 * @param thePageImages
	 *            hashmap of the original HTML tag to the Image
	 * @param pathImageHash
	 *            hashmap of the string to the urlPathImage data
	 */
	private void rewriteImageTags(EmailBodyProcessor emailBodyProcessor,
			HashMap<String, byte[]> thePageImages,
			HashMap<String, UrlPathImageData> pathImageHash) {

		NodeList imageList = emailBodyProcessor.getImageList();
		int current = 0;
		for (SimpleNodeIterator nodeIter = imageList.elements(); nodeIter
				.hasMoreNodes();) {
			ImageTag node = (ImageTag) nodeIter.nextNode();
			String nodeTxt = node.getText();
			if (thePageImages.containsKey(nodeTxt)) {
				String imageName = node.getImageURL();
				UrlPathImageData pathImageData = new UrlPathImageData(
						imageName, "img." + current, thePageImages.get(nodeTxt));
				pathImageHash.put(nodeTxt, pathImageData);

				node.setImageURL("cid:" + pathImageData.getRenamed());
				current++;
			}
		}
	}

	/**
	 * Rewrite the HTML Image tags with references to the attached Css
	 * 
	 * @param emailBodyProcessor
	 *            email body processor
	 * @param thePageCss
	 *            hashmap of the original HTML tag to the CSS data
	 * @param pathCssHash
	 *            hashmap of the string to the urlPathCss data
	 * 
	 */
	private void rewriteCssTags(EmailBodyProcessor emailBodyProcessor,
			HashMap<String, String> thePageCss,
			HashMap<String, UrlPathCssData> pathCssHash) {
		NodeList cssList = emailBodyProcessor.getCssList();
		int current = 0;
		for (SimpleNodeIterator nodeIter2 = cssList.elements(); nodeIter2
				.hasMoreNodes();) {
			TagNode tempNode = (TagNode) nodeIter2.nextNode();
			String tagText = tempNode.getAttribute("type");

			if ((tagText != null) && (tagText.contains("text/css"))) {
				String nodeTxt = tempNode.getText();

				System.out.println(tempNode.getText());

				if (thePageCss.containsKey(tempNode.getText())) {
					String cssUrlText = tempNode.getAttribute("href");
					UrlPathCssData pathCssData = new UrlPathCssData(cssUrlText,
							"css." + current, thePageCss
									.get(tempNode.getText()));
					pathCssHash.put(nodeTxt, pathCssData);

					tempNode.setAttribute("href", ("cid:" + pathCssData
							.getRenamed()));
					current++;
				}
			}
		}
	}

}
