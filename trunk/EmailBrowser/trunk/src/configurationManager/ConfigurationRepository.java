package configurationManager;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 
 * ConfigurationRepository stores/handles user configuration options
 * 
 */
public class ConfigurationRepository {
	final static String configFile = "." + File.separator + "config.xml";

	public String username = "";

	public String password = "";

	public String emailSubject = "";

	public String frequency = "";

	public ConfigurationRepository() {

	}

	/**
	 * Constructor -- sets up the configuration repository
	 */
	public ConfigurationRepository(boolean enable) {

		if (enable == false)
			return;

		if (!new File(configFile).exists()) {
			username = "foo";
			password = "bar";
			frequency = "1";
			emailSubject = "foo";

			save();
			System.out
					.println("Leaving ConfigurationRepository constructor...");
			return;
		}
		try {

			XMLDecoder xmlStore = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(configFile)));

			ConfigurationRepository result = (ConfigurationRepository) xmlStore
					.readObject();

			this.emailSubject = result.getEmailSubject();
			this.frequency = result.getFrequency();
			this.password = result.getPassword();
			this.username = result.getUsername();

		} catch (Exception eX) {

		}

	}

	/**
	 * Constructor that allows creating a ConfigurationRepository with values
	 * for its attributes
	 * 
	 * @param _username
	 * @param _password
	 * @param _frequency
	 * @param _emailSubject
	 */

	public ConfigurationRepository(String _username, String _password,
			String _frequency, String _emailSubject) {
		username = _username;
		password = _password;
		frequency = _frequency;
		emailSubject = _emailSubject;

	}

	/**
	 * Save the configuration file
	 * 
	 * @return true if the file is created successfully, false
	 *         otherwiseTrayInterface
	 */
	public boolean save() {

		System.out.println("File: " + configFile);

		try {

			XMLEncoder xmlStore = new XMLEncoder(new BufferedOutputStream(
					new FileOutputStream(configFile)));

			xmlStore.writeObject(this);
			xmlStore.close();

		} catch (Exception eX) {
			System.out
					.println("Exception thrown when trying to save configuration file using an XML encoder: ");
			System.out.println(eX.toString());
			System.out.println("File: " + configFile);
			return false;
		}

		return true;

	}

	/**
	 * username property getter method
	 * 
	 */

	public String getUsername() {
		return username;
	}

	/**
	 * username property setter
	 * 
	 * @param username
	 */

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * password property getter method
	 * 
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * password property setter
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * emailSubject property getter method
	 * 
	 * @return
	 */
	public String getEmailSubject() {
		return emailSubject;
	}

	/**
	 * emailSubject property setter
	 * 
	 * @param emailSubject
	 */
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	/**
	 * frequency property getter
	 * 
	 * @return
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * frequency property setter
	 * 
	 * @param frequency
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * configFile property getter method
	 * 
	 * @return
	 */
	public static String getConfigFile() {
		return configFile;
	}

}