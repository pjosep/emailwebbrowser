package requestManagerTest;

import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;

import junit.framework.TestCase;
import requestManager.EmailBodyProcessor;
import requestManager.MalformedEWBRequest;
import requestManager.EmailBodyProcessor.ArgStruct;

/**
 * 
 * Tests all public functions of the EmailBodyProcessor
 * 
 */
public class EmailBodyProcessorTest extends TestCase {

	/**
	 * Tests the case of single line emails with valid content
	 */
	public void testNormalCase() throws Exception {

		String[] testStrs = { "getWebPage http://www.google.com",
				"getWebPage http://google.com", "http://google.com getWebPage",
				"http://google.com", "http://www.google.com",
				"https://www.google.com", "getWebPage https://www.google.com",
				"http://www.g-goe1234567890.com/%20goo.2~dkf&=?ds",
				"http://www.ggoe3456sd90.com/%25goo.2~dkf&=?ds#test" };

		for (String str : testStrs) {

			EmailBodyProcessor eBodPro = new EmailBodyProcessor(str);

			eBodPro.parseEmailBody();
			assertEquals("1 web page should have been returned", eBodPro
					.getPageRequests().size(), 1);

			Iterator<URL> iter = eBodPro.getPageRequests().iterator();
			assertTrue("expected to find URL but didn't", str.toLowerCase()
					.contains(iter.next().getHost().toLowerCase()));

		}
	}

	/**
	 * Tests the case of single line emails with invalid content
	 */
	public void testInvalidFormatCase() throws Exception {

		String[] testStrs = { "htap://www.google.com", "www.google.com",
				"GetWebPage htap://www.google.com", "google",
				"getWebPage google", "", "getWebPage", };

		for (String str : testStrs) {

			EmailBodyProcessor eBodPro = new EmailBodyProcessor(str);

			try {
				eBodPro.parseEmailBody();
			} catch (MalformedEWBRequest eX) {

				assertEquals("0 web page should have been returned", eBodPro
						.getPageRequests().size(), 0);
			}
		}
	}

	/**
	 * Tests the case of multiple line emails with all valid content
	 */
	public void testNormalMultiLine() throws Exception {

		String[] testStrs = {
				"getWebPage http://www.google.com \r\n"
						+ "http://www.firefox.com",
				"http://www.google.com \r\n"
						+ "getWebPage http://www.firefox.com",
				"getWebPage http://www.google.com \r\n"
						+ "http://www.firefox.com \r\n"
						+ "http://www.yahoo.com \r\n"
						+ "http://www.uiuc.edu\r\n"
						+ "http://www.northropgrumman.com \r\n"
						+ "http://www.mytestpage.com \r\n"
						+ "http://www.dell.com \r\n"
						+ "http://www.bestbuy.com \r\n"
						+ "http://www.nbc.com \r\n" + "http://www.aol.com",
				"http://www.google.com \r\n" + "http://www.firefox.com",
				"http://www.google.com \r\n" + "http://www.firefox.com \r\n"
						+ "http://www.yahoo.com \r\n"
						+ "http://www.uiuc.edu\r\n"
						+ "http://www.northropgrumman.com \r\n"
						+ "http://www.mytestpage.com \r\n"
						+ "http://www.dell.com \r\n"
						+ "http://www.bestbuy.com \r\n"
						+ "http://www.nbc.com \r\n" + "http://www.aol.com", };

		for (String str : testStrs) {
			Integer numLines = 0;

			LineNumberReader reader = new LineNumberReader(
					new StringReader(str));
			EmailBodyProcessor eBodPro = new EmailBodyProcessor(str);

			while (reader.readLine() != null)
				numLines++;

			eBodPro.parseEmailBody();
			assertEquals(numLines.toString()
					+ "web page should have been returned", eBodPro
					.getPageRequests().size(), numLines.intValue());
		}
	}

	/**
	 * Tests the case of multiple line emails with no valid content
	 */
	public void testInvalidMultiLine() throws Exception {

		String[] testStrs = {
				"https://www.google.com \r\n" + "https://www.firefox.com",
				"getWebPage https://www.google.com \r\n"
						+ "https://www.firefox.com", };

		for (String str : testStrs) {

			EmailBodyProcessor eBodPro = new EmailBodyProcessor(str);
			try {
				eBodPro.parseEmailBody();
			} catch (MalformedEWBRequest eX) {
				assertEquals("0 web page should have been returned", eBodPro
						.getPageRequests().size(), 0);
			}
		}
	}

	/**
	 * Tests the case of form arguments
	 */
	public void testValidFormArguments() throws Exception {

		String[] testStrs = {
				"http://uiuc.edu/resources/results.html --GET:cx=006549799505564222509:-8lddip9q2g\r\n"
						+ "cof=FORID:11\r\n"
						+ "q=Computer+Science\r\n"
						+ "sa=go",
				"http://www.google.com --get:queryinput=newtest\r\n"
						+ "http://www.yahoo.com",
				"http://www.google.com --post:queryinput=newtest",
				"http://www.google.com --get:queryinput=newtest\r\n"
						+ "ie=ISO-8859-1\r\n" + "q=test2\r\n"
						+ "btnI=I'm+Feeling+Lucky\r\n" + "http://www.yahoo.com",
				"http://www.google.com --post:queryinput=newtest\r\n"
						+ "ie=\r\n" + "q=test2",
				"http://www.google.com --post:queryinput=newtest\r\n"
						+ "ie=ISO-89\r\n" + "q=",
				"http://www.google.com/search?q=h&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a",
				"http://www.google.com --post:queryinput=newtest\r\n"
						+ "ie=ISO-89\r\n"
						+ "q=test\r\n"
						+ "http://www.google.com/search?q=h&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a",
				"https://agora.cs.uiuc.edu/dosearchsite.action --POST:quickSearch=true\r\n"
						+ "searchQuery.spaceKey=conf_global\r\n"
						+ "searchQuery.queryString=testsearch\r\n",
				"http://www.google.com --post:queryinput=newtest\r\n"
						+ "ie=ISO-89\r\n"
						+ "q=http://www.www.com\r\n"
						+ "https://www.google.com/search?q=h&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a", };

		for (String str : testStrs) {

			int numLines = 0;
			LineNumberReader reader = new LineNumberReader(
					new StringReader(str));
			while (reader.readLine() != null)
				numLines++;

			EmailBodyProcessor eBodPro = new EmailBodyProcessor(str);

			eBodPro.parseEmailBody();
			HashMap<URL, ArgStruct> args = eBodPro.getPageArguments();

			Pattern urlPattern = Pattern.compile("^https?://",
					Pattern.MULTILINE);
			Matcher urlMatcher = urlPattern.matcher(str);
			int count = 0;
			while (urlMatcher.find()) {
				count++;
			}
			assertEquals(count + " web page should have been returned", eBodPro
					.getPageRequests().size(), count);

			for (URL url : eBodPro.getPageRequests()) {
				assertTrue("expected to find URL but didn't", str.toLowerCase()
						.contains(url.getHost().toLowerCase()));

				ArgStruct arg = args.get(url);
				if (arg != null) {
					assertTrue(str.contains(arg.formMethod));
					for (String key : arg.hashMap.keySet()) {
						numLines--;
						assertTrue(str.contains(key + "="));
						assertTrue(str.contains("=" + arg.hashMap.get(key)));
					}
				} else {
					numLines--;
				}
			}
			assertTrue(numLines == 0);

		}
	}

	/**
	 * Tests the retrieval of CSS sheets from \@import tags
	 */
	public void testCssImportSheets() {
		EmailBodyProcessor ebodPro = new EmailBodyProcessor("");
		TagNode tempNode = new TagNode();
		String testUrlPath = System.getProperty("user.dir") + File.separator
				+ EmailImageAttacherTest.testUrlPath;
		Page page = new Page(testUrlPath + "cssimporttest.html");
		String testUrl = "file:///" + testUrlPath.replace('\\', '/');
		page.setBaseUrl(testUrl);
		page.setUrl(testUrl + "cssimporttest.html");

		TextNode childNode[] = { new TextNode("@import url(ex1.css);"),
				new TextNode("@import url(\"ex1.css\");"),
				new TextNode("@import url('ex1.css');"),
				new TextNode("@import \"ex1.css\";"),
				new TextNode("@import 'ex1.css';"),
				new TextNode("@import'ex1.css';"),
				new TextNode("@import\"ex1.css\";"), };
		NodeList nodelist = new NodeList();
		for (int i = 0; i < childNode.length; i++) {
			childNode[i].setPage(page);
			nodelist.add(childNode[i]);
		}

		tempNode.setChildren(nodelist);

		try {
			ebodPro.processImportTypeCss(tempNode);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Tests the case of valid URLs passed to the EmailBodyProcessor. The HTML
	 * and images of the desired page should match what is downloaded via the
	 * EmailBodyProcessor.
	 */
	public void testNormalRetrieveAllUrlData() throws Exception {
		class CompareDataClass {
			String url;

			String html;

			String[] images;

			CompareDataClass(String _url, String _html, String[] _images) {
				url = _url;
				html = _html;
				images = _images;
			}
		}
		String testUrlPath = EmailImageAttacherTest.testUrlPath;
		String testImagePath = EmailImageAttacherTest.testImagePath;

		CompareDataClass[] websites = {
				new CompareDataClass(
						"http://csil-projects.cs.uiuc.edu/~tjbrenna/emailbrowsertests/MonaOneImageTest.html",
						testUrlPath + "MonaOneImageTest.html",
						new String[] { testImagePath + "DSCN0012.jpg" }),
				new CompareDataClass(
						"http://csil-projects.cs.uiuc.edu/~tjbrenna/emailbrowsertests/MonaFiveImageTest.html",
						testUrlPath + "MonaFiveImageTest.html", new String[] {
								testImagePath + "DSCN0012.jpg",
								testImagePath + "DSCN0044.gif",
								testImagePath + "DSCN0054.png",
								testImagePath + "DSCN0115.bmp",
								testImagePath + "DSCN0163.jpg" }) };

		for (CompareDataClass website : websites) {
			EmailBodyProcessor ebodPro = new EmailBodyProcessor("");

			ebodPro.processUrl(new URL(website.url));
			HashMap<String, byte[]> hash = ebodPro.getPageImages();

			assertEquals(
					"Number of images received is not equal to the number in the page",
					hash.size(), website.images.length);
			for (String imagePath : website.images) {
				boolean found = false;
				byte[] localImage = EmailImageAttacherTest
						.read2ByteArray(imagePath);
				for (byte[] image : hash.values()) {
					if (localImage.length == image.length) {
						found = Arrays.equals(image, localImage);
						if (found) {
							image = new byte[0];
							break;
						}
					}
				}
				assertTrue(found);
			}

			String HTML = ebodPro.getFullList().toHtml();
			String fileHTML = new String(EmailImageAttacherTest
					.read2ByteArray(website.html));

			assertEquals("Downloaded HTML should be the same", HTML, fileHTML);
		}
	}

	/**
	 * Tests the case where an invalid URL is passed into the EmailBodyProcessor
	 */
	public void testInvalidRetrieveAllUrlData() {

		String[] testStrs = { "", "http://www.failedtestnotavalidurl.com/",
				"http://www.google.oo/" };

		for (String url : testStrs) {
			EmailBodyProcessor ebodPro = new EmailBodyProcessor("");

			try {
				ebodPro.processUrl(new URL(url));
				assertTrue("Code should never get here", false);
			} catch (Exception e) {

			}

		}
	}

}
