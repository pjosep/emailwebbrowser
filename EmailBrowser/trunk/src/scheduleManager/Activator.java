package scheduleManager;

import configurationManager.ConfigurationRepository;

/**
 * 
 * Activator runs the EmailReaderExecutor
 * 
 */
public class Activator {

	static EmailReaderExecutor myExec;

	/**
	 * Executes the EmailReaderExecutor
	 * 
	 * @throws Exception
	 */
	public static void run() throws Exception {

		ConfigurationRepository myRepository = new ConfigurationRepository(true);

		EmailReaderExecutor.execute(myRepository);

	}

	/**
	 * Creates a new EmailReaderExecutor and starts it
	 */
	public static void start() {
		ConfigurationRepository myRepository = new ConfigurationRepository(true);
		myExec = new EmailReaderExecutor(myRepository);
		myExec.start();
	}

	/**
	 * Stops the EmailReaderExecutor
	 */
	public static void stop() {
		myExec.running = false;
	}
}
