package scheduleManager;

import requestManager.EmailReader;
import configurationManager.ConfigurationRepository;

/**
 * 
 * EmailReaderExecutor executes the EmailReader
 * 
 */
class EmailReaderExecutor extends Thread {

	ConfigurationRepository myRepository;

	public boolean running = false;

	/**
	 * Constructor sets the internal repository to the repository param
	 * 
	 * @param repository
	 *            configuration repository
	 */
	public EmailReaderExecutor(ConfigurationRepository repository) {
		myRepository = repository;
	}

	@Override
	public void run() {

		Float seconds = Float.parseFloat(myRepository.frequency) * 60 * 1000;

		EmailReader myReader = new EmailReader();

		running = true;

		while (running == true) {

			execute(myRepository, myReader);
			try {
				sleep(seconds.longValue());
			} catch (Exception eX) {
				running = false;
				return;
			}
		}

	}

	/**
	 * Gets messages from the EmailReader and then triggers a response based on
	 * the email.
	 * 
	 * @param myRepository
	 *            configuration repository
	 */
	public static void execute(ConfigurationRepository myRepository) {
		EmailReader myReader = new EmailReader();
		execute(myRepository, myReader);
	}

	private static void execute(ConfigurationRepository myRepository,
			EmailReader myReader) {
		System.out.println("Executing...");

		System.out.println("username: " + myRepository.username);
		System.out.println("password: " + myRepository.password);
		System.out.println("subject: " + myRepository.emailSubject);

		boolean success = myReader.getMessages(myRepository.username,
				myRepository.password, myRepository.emailSubject);

		if (!success) {
			System.out.println("Not successful!");
			return;
		}

		myReader.triggerResponses();

	}
}
