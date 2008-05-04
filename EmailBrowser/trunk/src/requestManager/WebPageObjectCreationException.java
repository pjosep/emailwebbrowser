package requestManager;

/**
 * 
 * Exception thrown to indicate that there is a problem with the retrieval of
 * web data
 * 
 */
public class WebPageObjectCreationException extends Exception {

	/**
	 * Auto generated serial version UID
	 */
	private static final long serialVersionUID = 4461415087510847205L;

	String theReason;

	/**
	 * Constructor of the Exception
	 * 
	 * @param reason
	 *            error string
	 */
	WebPageObjectCreationException(String reason) {

		System.out.println("WebPageObjectCreation: " + reason);

		theReason = reason;

	}

	@Override
	public String toString() {
		return theReason;
	}

}