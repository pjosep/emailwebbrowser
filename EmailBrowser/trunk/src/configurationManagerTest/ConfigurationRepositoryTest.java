package configurationManagerTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import junit.framework.TestCase;
import configurationManager.ConfigurationRepository;

public class ConfigurationRepositoryTest extends TestCase {

	static final String testConfigFile = "." + File.separator + "src"
			+ File.separator + "configurationManagerTest" + File.separator
			+ "configTest.xml";

	/**
	 * In this test, we will check whether the ConfiguratioRepository save
	 * method works properly. We have a file, configTest.xml to which we will
	 * compare the file that the repository creates when calling the save
	 * method.
	 * 
	 */

	public void testConfigurationSave() throws Exception {
		ConfigurationRepository myConfigurationRepository = new ConfigurationRepository(
				true);

		myConfigurationRepository.emailSubject = "foo";
		myConfigurationRepository.frequency = "10.0";
		myConfigurationRepository.username = "foo";
		myConfigurationRepository.password = "bar";

		boolean wasSaved = myConfigurationRepository.save();

		assertEquals(wasSaved, true);

		Reader savedFileReader = new FileReader(ConfigurationRepository
				.getConfigFile());

		BufferedReader savedBufferReader = new BufferedReader(savedFileReader);

		String buffer = "";

		String savedString = "";

		while ((buffer = savedBufferReader.readLine()) != null) {
			savedString = savedString + buffer;
		}

		Reader testFileReader = new FileReader(testConfigFile);
		BufferedReader testBufferReader = new BufferedReader(testFileReader);

		buffer = "";

		String testString = "";

		while ((buffer = testBufferReader.readLine()) != null) {
			testString = testString + buffer;
		}

		assertEquals(savedString, testString);

	}
}
