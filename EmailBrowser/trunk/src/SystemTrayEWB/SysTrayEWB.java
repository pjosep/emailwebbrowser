package SystemTrayEWB;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import scheduleManager.Activator;
import configurationManager.ConfigurationDialog;

/**
 * 
 * SysTrayEWB contains the entry point for the EmailWebBrowser. It also creates
 * the system tray icon that the user uses to interface with the browser.
 * 
 */
public class SysTrayEWB {
	static Activator myActivator;

	/**
	 * The main function of the EmailWebBrowser
	 * 
	 * @param args
	 *            arguments (currently unused)
	 */
	public static void main(String[] args) {
		// Use an appropriate Look and Feel
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		// Turn off metal's use of bold fonts
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		// Schedule a job for the event-dispatching thread:
		// adding TrayIcon.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI() {
		// Check the SystemTray support
		myActivator = new Activator();

		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage("process.png",
				"tray icon"));
		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a popup menu components
		MenuItem runItem = new MenuItem("Run Once");
		CheckboxMenuItem enableItem = new CheckboxMenuItem("Enable Service");// CheckboxMenuItem

		MenuItem configItem = new MenuItem("System Setup");
		MenuItem warningItem = new MenuItem("Warning");
		MenuItem infoItem = new MenuItem("Info");
		MenuItem noneItem = new MenuItem("None");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(runItem);
		popup.addSeparator();
		popup.add(enableItem);
		popup.addSeparator();
		popup.add(configItem);
		popup.add(exitItem);

		try {
			configureTrayIcon(popup, trayIcon, tray);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		configureRunItem(runItem);

		configureEnableItem(enableItem);

		ActionListener listener = configureActionListener(trayIcon);

		configItem.addActionListener(listener);
		warningItem.addActionListener(listener);
		infoItem.addActionListener(listener);
		noneItem.addActionListener(listener);

		configureExitItem(trayIcon, tray, exitItem);
	}

	private static ActionListener configureActionListener(
			final TrayIcon trayIcon) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MenuItem item = (MenuItem) e.getSource();
				System.out.println(item.getLabel());

				if ("System Setup".equals(item.getLabel())) {

					String[] mySA;

					mySA = new String[0];

					ConfigurationDialog.main(mySA);

				}

				else if ("Error".equals(item.getLabel())) {
					trayIcon.displayMessage("EmailWebBrowser TrayIcon",
							"This is an error message",
							TrayIcon.MessageType.ERROR);

				} else if ("Warning".equals(item.getLabel())) {
					trayIcon.displayMessage("EmailWebBrowser TrayIcon",
							"This is a warning message",
							TrayIcon.MessageType.WARNING);

				} else if ("Info".equals(item.getLabel())) {
					trayIcon.displayMessage("EmailWebBrowser TrayIcon",
							"This is an info message",
							TrayIcon.MessageType.INFO);

				} else if ("None".equals(item.getLabel())) {
					trayIcon.displayMessage("EmailWebBrowser TrayIcon",
							"This is an ordinary message",
							TrayIcon.MessageType.NONE);
				}
			}
		};
		return listener;
	}

	private static void configureExitItem(final TrayIcon trayIcon,
			final SystemTray tray, MenuItem exitItem) {
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		});
	}

	private static void configureEnableItem(CheckboxMenuItem enableItem) {
		enableItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int enableItem = e.getStateChange();
				if (enableItem == ItemEvent.SELECTED) {
					Activator.start();
				} else {
					Activator.stop();
				}
			}
		});
	}

	/**
	 * When the Run item is clicked
	 * 
	 * @param runItem
	 */
	private static void configureRunItem(MenuItem runItem) {
		runItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					Activator.run();
				} catch (Exception eX) {
					JOptionPane.showMessageDialog(null,
							"EmailWebBrowser service failed to start");

				}

			}
		});
	}

	private static void configureTrayIcon(final PopupMenu popup,
			final TrayIcon trayIcon, final SystemTray tray) throws AWTException {
		trayIcon.setPopupMenu(popup);

		tray.add(trayIcon);

		// when the trayicon is double clicked you get to see this in a pop up.

		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"This dialog box is run from System Tray");
			}
		});
	}

	/**
	 * Obtain the image URL
	 * 
	 * @param path
	 *            the path of the image to create
	 * @param description
	 *            the description of the image being created
	 * @return the created image
	 */
	protected static Image createImage(String path, String description) {
		URL imageURL = SysTrayEWB.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}
