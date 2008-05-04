package configurationManager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

/**
 * 
 * ConfigurationDialog is the GUI that is presented to the user to enter
 * configuration options.
 * 
 */
public class ConfigurationDialog extends JPanel implements ActionListener {
	/**
	 * Auto generated serial version UID
	 */
	private static final long serialVersionUID = 4871137810276994490L;

	JFrame frame;
	SpinnerModel frequencyModel;
	JSpinner spinner;
	JPanel accountPanel;
	JTextField userF;
	JTextField subjectF;
	JPasswordField passwordF;

	static ConfigurationRepository myRepository;

	/**
	 * Constructor
	 * 
	 * @param frame
	 *            frame in which to create the configuration dialog
	 */
	public ConfigurationDialog(JFrame frame) {

		super(new BorderLayout());

		this.frame = frame;

		accountPanel = createAccountDialogBox();

		Border padding = BorderFactory.createEmptyBorder(20, 20, 5, 20);

		accountPanel.setBorder(padding);

		JPanel mainPanel = new JPanel();

		mainPanel.add(accountPanel);

		this.add(mainPanel);
	}

	private JPanel createAccountDialogBox() {

		String[] labels = { "Gmail User Name: ", "Gmail Password: ",
				"Schedule (minutes): ", "Email Subject: " };
		int numPairs = labels.length;

		// Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			populateDialogPanel(labels[i], p);
		}

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(this);
		okButton.setActionCommand("OK");

		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("Cancel");

		p.add(okButton);

		p.add(cancelButton);

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p, numPairs + 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad

		return p;

	}

	private void populateDialogPanel(String label, JPanel p) {
		JLabel l = new JLabel(label, JLabel.TRAILING);
		p.add(l);

		JTextField textField = new JTextField(10);
		textField.addActionListener(this);

		if (label.equals("Gmail User Name: ")) {
			textField.setText(myRepository.username);
			textField.setActionCommand("username");
			textField.addActionListener(this);
			userF = textField;
			l.setLabelFor(textField);
			p.add(textField);

		} else if (label.equals("Email Subject: ")) {
			textField.setText(myRepository.emailSubject);
			textField.setActionCommand("emailSubject");
			textField.addActionListener(this);
			subjectF = textField;
			l.setLabelFor(textField);
			p.add(textField);
		} else if (label.equals("Gmail Password: ")) {
			textField = new JPasswordField(10);
			textField.setText(myRepository.password);
			textField.addActionListener(this);

			textField.setActionCommand("password");
			passwordF = (JPasswordField) textField;
			l.setLabelFor(textField);
			p.add(textField);
		} else if (label.equals("Schedule (minutes): ")) {
			try {
				frequencyModel = new SpinnerNumberModel(Float
						.parseFloat((myRepository.frequency)), 1, 60, 0.5);
			} catch (Exception eX) {
				frequencyModel = new SpinnerNumberModel(1, 1, 60, 0.5);

			}
			spinner = new JSpinner(frequencyModel);

			l.setLabelFor(spinner);
			p.add(spinner);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		System.out.println("action performed on: " + e.getActionCommand());

		if (e.getActionCommand().equals("OK")) {

			if (checkEmailFormat(userF.getText()) == false) {
				JOptionPane.showMessageDialog(frame,
						"Illegal text in username field");
				return;
			}

			myRepository.frequency = spinner.getValue().toString();
			myRepository.emailSubject = subjectF.getText();
			myRepository.password = new String(passwordF.getPassword());
			;
			myRepository.username = userF.getText();

			myRepository.save();
			this.frame.dispose();

		} else if (e.getActionCommand().equals("Cancel"))

		{
			this.frame.dispose();
		} else if (e.getActionCommand().equals("username")) {
			System.out.println("Username changed!");
			if (checkEmailFormat(((JTextField) e.getSource()).getText()) == false) {
				JOptionPane.showMessageDialog(frame,
						"Illegal text in username field");
				return;
			}
			myRepository.username = ((JTextField) e.getSource()).getText();
		} else if (e.getActionCommand().equals("password")) {
			myRepository.password = ((JTextField) e.getSource()).getText();
		} else if (e.getActionCommand().equals("emailSubject")) {
			myRepository.emailSubject = ((JTextField) e.getSource()).getText();
		} else if (e.getActionCommand().equals("frequency")) {
			myRepository.frequency = ((JSpinner) e.getSource()).getValue()
					.toString();
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		myRepository = new ConfigurationRepository(true);

		JFrame frame = new JFrame("Email Web Browser Configuration");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		ConfigurationDialog newContentPane = new ConfigurationDialog(frame);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Main entrance for the configuration dialog
	 * 
	 * @param args
	 *            Arguments (currently unused)
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private boolean checkEmailFormat(String toBeChecked) {

		try {
			new InternetAddress(toBeChecked + "@gmail.com", true);
		} catch (Exception eX) {
			return false;
		}

		return true;

	}

}
