package requestManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * 
 * EmailBodyProcessor handles processing the body of the request email, and
 * pulling out the URLs needed to process, and the related arguments.
 * 
 */
public class EmailBodyProcessor {

	/**
	 * 
	 * Structure used to hold information on Form Arguments passed into an email
	 * 
	 */
	public class ArgStruct {
		public String formMethod;

		public LinkedHashMap<String, String> hashMap;

		public ArgStruct(String _formMethod,
				LinkedHashMap<String, String> _hashMap) {
			formMethod = _formMethod;
			hashMap = _hashMap;
		}
	}

	private String body;

	private HashSet<URL> pageRequests;

	private HashMap<URL, ArgStruct> pageArguments;

	private HashMap<String, byte[]> thePageImages;

	private HashMap<String, String> thePageCss;

	private NodeList fullList;

	private NodeList imageList;

	private NodeList cssList;

	private Pattern urlPattern;

	private Pattern argPattern;

	private int URL_PAT_URL_GROUP;

	private int URL_PAT_FORM_TYPE_GROUP;

	private int URL_PAT_FORM_VALS_GROUP;

	private int ARG_PAT_FORM_ID_GROUP;

	private int ARG_PAT_FORM_VAL_GROUP;

	/**
	 * Constructor - sets the email body text, and initializes data
	 * 
	 * @param emailBody
	 *            text of in the email
	 */
	public EmailBodyProcessor(String emailBody) {
		body = emailBody;
		urlPattern = Pattern
				.compile(
						"^[^=]*?(https?://\\S+)(?:[\\r\\n ]+--(\\w+):[\\r\\n]*((?:(?!https?://).+?=.*[\\r\\n]*)*))?",
						Pattern.MULTILINE);
		URL_PAT_URL_GROUP = 1; // (https?://\\S+)
		URL_PAT_FORM_TYPE_GROUP = 2; // (\\w+)
		URL_PAT_FORM_VALS_GROUP = 3; // ((?:(?!https?://).+?=.*[\\r\\n]*)*)

		argPattern = Pattern.compile("((.+)=(.*))+");
		ARG_PAT_FORM_ID_GROUP = 2; // (.+)
		ARG_PAT_FORM_VAL_GROUP = 3; // (.*)
	}

	/**
	 * Finds valid URL requests in the email body
	 * 
	 * @return hash of the valid URLs being requested
	 * @throws Exception
	 */
	public HashSet<URL> parseEmailBody() throws MalformedEWBRequest {

		Matcher urlMatcher = urlPattern.matcher(body);

		pageRequests = new HashSet<URL>();
		pageArguments = new HashMap<URL, ArgStruct>();

		try {
			while (urlMatcher.find()) {
				URL url = new URL(urlMatcher.group(URL_PAT_URL_GROUP));
				pageRequests.add(url);
				if (urlMatcher.group(URL_PAT_FORM_TYPE_GROUP) != null) {
					String formMethod = urlMatcher
							.group(URL_PAT_FORM_TYPE_GROUP);
					LinkedHashMap<String, String> urlMap = new LinkedHashMap<String, String>();
					Matcher argMatcher = argPattern.matcher(urlMatcher
							.group(URL_PAT_FORM_VALS_GROUP));
					while (argMatcher.find()) {
						urlMap.put(argMatcher.group(ARG_PAT_FORM_ID_GROUP),
								argMatcher.group(ARG_PAT_FORM_VAL_GROUP));
					}
					pageArguments.put(url, new ArgStruct(formMethod, urlMap));
				}
			}
		} catch (MalformedURLException mfURL) {
			throw new MalformedEWBRequest();
		}

		if (pageRequests.size() == 0) {
			System.out.println("WARNING: No URLs found in request.");
			throw new MalformedEWBRequest();
		}

		return pageRequests;
	}

	/**
	 * 
	 * @return hash of the original page tag to the actual image
	 */
	public HashMap<String, byte[]> getPageImages() {
		return thePageImages;
	}

	/**
	 * 
	 * @return the URLs being requested
	 */
	public HashSet<URL> getPageRequests() {
		return pageRequests;
	}

	/**
	 * 
	 * @return the Page Arguments being requested
	 */
	public HashMap<URL, ArgStruct> getPageArguments() {
		return pageArguments;
	}

	/**
	 * 
	 * @return list that is directly linked to image tags in the HTML
	 */
	public NodeList getImageList() {
		return imageList;
	}

	/**
	 * 
	 * @return list that is directly linked to all nodes in the HTML
	 */
	public NodeList getFullList() {
		return fullList;
	}

	/**
	 * Retrieves the web data from the URL
	 * 
	 * @param browseURL
	 *            URL to get data for
	 * @throws WebPageObjectCreationException
	 */
	public void processUrl(URL browseURL) throws WebPageObjectCreationException {

		HttpURLConnection connection = null;

		ArgStruct formDetails = (pageArguments == null) ? null : pageArguments
				.get(browseURL);
		if ((formDetails != null)
				&& (formDetails.formMethod.toLowerCase().equals("get"))) {
			browseURL = rewriteGetURL(browseURL, formDetails);
		}

		try {
			if ((formDetails != null)
					&& (formDetails.formMethod.toLowerCase().equals("post"))) {
				connection = (HttpURLConnection) openPostConnection(browseURL,
						formDetails);
			} else {
				connection = (HttpURLConnection) browseURL.openConnection();
			}
		} catch (IOException ioEx) {

			throw new WebPageObjectCreationException(
					"Could not open connection to: " + browseURL.toString()
							+ ", reason: " + ioEx.toString());
		}

		Parser parser = new Parser();

		retrieveHtml(browseURL, connection, parser);

		processExternalLinks(browseURL);

	}

	private void processExternalLinks(URL browseURL)
			throws WebPageObjectCreationException {
		try {
			processImages();
		} catch (MalformedURLException malEx) {
			throw new WebPageObjectCreationException(
					"Malformed URL in image tags for: " + browseURL.toString()
							+ ", reason: " + malEx.toString());

		} catch (IOException ioEx) {
			throw new WebPageObjectCreationException(
					"IO error in getting images defined at: "
							+ browseURL.toString() + ", reason: "
							+ ioEx.toString());

		}

		try {
			processCss();
		} catch (IOException ioEx) {
			throw new WebPageObjectCreationException(
					"IO error in inprocessing CSS info from: "
							+ browseURL.toString() + ", reason: "
							+ ioEx.toString());

		}
	}

	private void retrieveHtml(URL browseURL, HttpURLConnection connection,
			Parser parser) throws WebPageObjectCreationException {
		try {
			parser.setConnection(connection);
			fullList = parser.parse(null);
		} catch (ParserException parEx) {
			throw new WebPageObjectCreationException(
					"Error parsing contents of: " + browseURL.toString()
							+ ", reason: " + parEx.toString());
		}
	}

	private URL rewriteGetURL(URL browseURL, ArgStruct formDetails) {
		boolean firstStr = true;
		String newUrl = browseURL.toString();
		for (String key : formDetails.hashMap.keySet()) {
			if (firstStr) {
				newUrl += "?" + key + "=" + formDetails.hashMap.get(key);
				firstStr = false;
			} else {
				newUrl += "&" + key + "=" + formDetails.hashMap.get(key);
			}
		}
		try {
			browseURL = new URL(newUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return browseURL;
	}

	private URLConnection openPostConnection(URL browseURL,
			ArgStruct formDetails) throws IOException {
		HttpURLConnection connection = null;

		// from the 'action' (relative to the referring page)
		connection = (HttpURLConnection) browseURL.openConnection();
		connection.setRequestMethod("POST");

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);

		// more or less of these may be required
		// see Request Header Definitions: http://www.ietf.org/rfc/rfc2616.txt
		connection.setRequestProperty("Accept-Charset", "*");
		connection.setRequestProperty("Referer", browseURL.toString());

		// 'input' fields separated by ampersands (&)
		StringBuffer buffer = new StringBuffer(1024);
		boolean firstStr = true;
		for (String key : formDetails.hashMap.keySet()) {
			if (firstStr) {
				firstStr = false;
			} else {
				buffer.append("&");
			}
			buffer.append(key);
			buffer.append("=");
			buffer.append(formDetails.hashMap.get(key));
		}

		PrintWriter out = new PrintWriter(connection.getOutputStream());
		out.print(buffer);
		out.close();

		return connection;
	}

	/**
	 * Gets the images from the webpage
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void processImages() throws MalformedURLException, IOException {

		NodeFilter imageFilter = new NodeClassFilter(ImageTag.class);
		imageList = fullList.extractAllNodesThatMatch(imageFilter, true);
		thePageImages = new HashMap<String, byte[]>();

		for (SimpleNodeIterator nodeIter = imageList.elements(); nodeIter
				.hasMoreNodes();) {
			ImageTag tempNode = (ImageTag) nodeIter.nextNode();
			String tagText = tempNode.getText();

			// Populate imageUrlText String
			String imageUrlText = tempNode.getImageURL();

			if (imageUrlText != "") {
				// Print to console, to verify relative link processing
				System.out.println("ImageUrl to Retrieve:" + imageUrlText);

				byte[] imgArray = downloadBinaryData(imageUrlText);

				thePageImages.put(tagText, imgArray);
			}
		}

	}

	/**
	 * Downloads a binary file from input URL
	 * 
	 * @param imageUrlText
	 *            url of binary file to be downloaded
	 * @return byte array of data
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private byte[] downloadBinaryData(String imageUrlText)
			throws MalformedURLException, IOException {
		// retrieve the image from the webpage and put it in HashMap
		URL url = new URL(imageUrlText);

		// input from image
		BufferedInputStream in = new BufferedInputStream(url.openStream());
		// downloaded bytes
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// download buffer
		final int BUFFERSIZE = 4096;
		byte[] buffer = new byte[BUFFERSIZE];
		int bytesToWrite;

		while ((bytesToWrite = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesToWrite);
		}

		byte[] imgArray = out.toByteArray();

		in.close();
		out.close();
		return imgArray;
	}

	/**
	 * Gets the css sheets from the webpage
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void processCss() throws MalformedURLException, IOException {

		cssList = fullList.extractAllNodesThatMatch(new NodeClassFilter(
				TagNode.class), true);
		thePageCss = new HashMap<String, String>();

		for (SimpleNodeIterator nodeIter = cssList.elements(); nodeIter
				.hasMoreNodes();) {
			TagNode tempNode = (TagNode) nodeIter.nextNode();
			String tagText = tempNode.getAttribute("type");

			if ((tagText != null) && (tagText.contains("text/css"))) {

				if (tempNode instanceof StyleTag) {
					processImportTypeCss(tempNode);
				} else {
					processLinkTypeCss(tempNode);
				}
			}
		}
	}

	private void processLinkTypeCss(TagNode tempNode)
			throws MalformedURLException, IOException {
		// Populate imageUrlText String

		List<String> cssUrlList = new ArrayList<String>();
		String cssUrlText = tempNode.getAttribute("href");
		String fullUrl = tempNode.getPage().getAbsoluteURL(cssUrlText);

		// Print to console, to verify relative link processing
		System.out.println("CSS URL to Retrieve:" + cssUrlText);
		cssUrlList.add(fullUrl);

		for (String urlStr : cssUrlList) {
			String cssStr = retrieveCssData(urlStr);
			thePageCss.put(tempNode.getText(), cssStr);
		}
	}

	/**
	 * Process css sheets that are referenced via the \@import tag
	 * 
	 * @param tempNode
	 *            node that contains the \@import tag
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void processImportTypeCss(TagNode tempNode)
			throws MalformedURLException, IOException {
		Pattern cssImportPattern = Pattern
				.compile("@import\\s*(?: url)?(?:(?:\\((?:'|\")?)|\"|')(\\S+?)(?:(?:(?:'|\")?\\))|\"|')");
		for (SimpleNodeIterator nodeIter3 = tempNode.getChildren().elements(); nodeIter3
				.hasMoreNodes();) {
			TextNode childNode = (TextNode) nodeIter3.nextNode();
			String styleTagText = childNode.toHtml();

			Matcher cssImportMatcher = cssImportPattern.matcher(styleTagText);
			boolean cssImportFound = false;
			String retrievedCssData = "";

			while (cssImportMatcher.find()) {
				String cssImportFullUrl = childNode.getPage().getAbsoluteURL(
						cssImportMatcher.group(1));

				String cssStr = retrieveCssData(cssImportFullUrl);
				retrievedCssData += cssStr;
				cssImportFound = true;
			}
			if (cssImportFound) {
				childNode.setText(retrievedCssData);
			} else {
				System.out.println("No URLs found.");
			}
		}
	}

	/**
	 * Retrieve the css from the webpage and put it in HashMap
	 * 
	 * @param urlStr
	 *            url to the CSS sheet to retrieve
	 * @return string of the CSS data
	 */
	private String retrieveCssData(String urlStr) throws MalformedURLException,
			IOException {
		// retrieve the image from the webpage and put it in
		// HashMap
		URL url = new URL(urlStr);
		BufferedReader cssData = new BufferedReader(new InputStreamReader(url
				.openConnection().getInputStream()));
		String cssStr = "";
		String tempStr;
		while ((tempStr = cssData.readLine()) != null) {
			cssStr += (tempStr + "\n");
		}
		cssData.close();
		return cssStr;
	}

	/**
	 * 
	 * @return a hash of the CSS page data
	 */
	public HashMap<String, String> getThePageCss() {
		return thePageCss;
	}

	/**
	 * 
	 * @return a list of the html css nodes
	 */
	public NodeList getCssList() {
		return cssList;
	}

}