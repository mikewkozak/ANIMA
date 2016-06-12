/**
Mike Kozak
mwk24@drexel.edu
CS530:Project
*/
package cs530.project.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import cs530.project.datamodel.AttentionLevelType;
import cs530.project.datamodel.KeyWindowType;
import cs530.project.datamodel.UserSettings;
import cs530.project.util.JOutlookBar;

/**
 * The main UI for ANIMA. This component both displays a mock interface meant to demonstrate
 * the attention management system, but also provides developer debug options, settings, and
 * performs the alterations to the UI based on EEG levels
 * @author kozakm
 *
 */
public class ANIMAFrame extends JFrame implements ActionListener, ChangeListener {
	private static final long serialVersionUID = -425929870366825931L;

	//Statics
	private static boolean DEBUG_MODE = true;
	private static Dimension MIN_DEBUG_SIZE = new Dimension(630,600);
	private static Dimension MIN_SIZE = new Dimension(630,560);
	private static ImageIcon STATUS_GOOD = new ImageIcon("resources/status/green.png");
	private static ImageIcon STATUS_MODERATE = new ImageIcon("resources/status/yellow.png");
	private static ImageIcon STATUS_BAD = new ImageIcon("resources/status/red.png");
	private static ImageIcon ALERT = new ImageIcon("resources/images/warning.png");
	
	//Menu items
	private JMenuItem exit = null;
	private JMenuItem settings = null;
	private JMenuItem guide = null;
	private JMenuItem about = null;
	
	//Main panels
	private ANIMAFrame frame = this;
	private JPanel controlsPanel = null;
	private JPanel statusPanel = null;
	private JPanel alertsPanel = null;
	private JPanel viewportPanel = null;
	private JPanel debugPanel = null;
	
	//Status Panel
	private JLabel batStatus = new JLabel(STATUS_GOOD);
	private JLabel motorStatus = new JLabel(STATUS_MODERATE);
	private JLabel connStatus = new JLabel(STATUS_BAD);
	
	//Alerts Panel
	private JTable alertsTable = null;
	
	//Settings
	private UserSettings settingsPanel = new UserSettings();
	private AttentionLevelType currentAttentionLevel = AttentionLevelType.LEVEL_ZERO;

	//Level 4 alert items
	protected JLabel alert = new JLabel(ALERT);
	protected JDialog alertDialog = new JDialog(this, "Child", false);
	
	//Debug
	private JSlider attentionSlider = null;
	private JLabel eegLevel = new JLabel("-1");
	private JTextField desiredEEG = new JTextField();
	
	//Alert timer
	final Timer flickerTimer;
	
	//My layout
	GridBagLayout layout = new GridBagLayout();
	
	/**
	 * Private thread class meant to generate demo UI events on a periodic basis
	 * @author kozakm
	 *
	 */
	private class DemoEventThread extends Thread {
		public void run() {
			Random rand = new Random();
			int currInt = rand.nextInt(10);
			while(true) {
				try {
					sleep(5000);//5sec
				} catch (InterruptedException e) {
					//escaped!
					return;
				}

				currInt = rand.nextInt(10);
				if(currInt < 1) {//go bad
					batStatus.setIcon(STATUS_BAD);
					DefaultTableModel model = (DefaultTableModel) alertsTable.getModel();
					model.addRow(new Object[]{"Low Battery",10,"HIGH","Surface"});
				} else if(currInt > 7) {//go moderate
					batStatus.setIcon(STATUS_MODERATE);
				} else {//stay good
					batStatus.setIcon(STATUS_GOOD);
				}
				
				currInt = rand.nextInt(10);
				if(currInt < 1) {//go bad
					connStatus.setIcon(STATUS_BAD);
					DefaultTableModel model = (DefaultTableModel) alertsTable.getModel();
					model.addRow(new Object[]{"Low Bandwidth",10,"MEDIUM","Dismiss"});
				} else if(currInt > 7) {//go moderate
					connStatus.setIcon(STATUS_MODERATE);
				} else {//stay good
					connStatus.setIcon(STATUS_GOOD);
				}
				
				currInt = rand.nextInt(10);
				if(currInt < 1) {//go bad
					motorStatus.setIcon(STATUS_BAD);
					DefaultTableModel model = (DefaultTableModel) alertsTable.getModel();
					model.addRow(new Object[]{"High Engine Temp",8,"HIGH","Acknowledge"});
				} else if(currInt > 7) {//go moderate
					motorStatus.setIcon(STATUS_MODERATE);
				} else {//stay good
					motorStatus.setIcon(STATUS_GOOD);
				}
			}
		}
	}
	
	/**
	 * Private thread class that is started when attention lapses and triggers
	 * increasing alert levels as time goes on or until attention is restored
	 * @author kozakm
	 *
	 */
	private class AlertThread extends Thread {
		private int seconds = 0;
		public boolean run = false;
	    AlertThread(String name) {
	      super(name);
	    }

	    public void run() {
	    	System.out.println("Starting AlertThread");
	      while (run) {
	    	  System.out.println("Time elapsed: " + seconds);
	    	if(seconds >= 10 && seconds < 15 && currentAttentionLevel != AttentionLevelType.LEVEL_ONE) {
	    		setAttentionLevel(AttentionLevelType.LEVEL_ONE);
	    	} else if (seconds >= 15 && seconds < 20 && currentAttentionLevel != AttentionLevelType.LEVEL_TWO) {
	    		setAttentionLevel(AttentionLevelType.LEVEL_TWO);
	    	} else if (seconds >= 20 && seconds < 30 && currentAttentionLevel != AttentionLevelType.LEVEL_THREE) {
	    		setAttentionLevel(AttentionLevelType.LEVEL_THREE);
	    	} else if (seconds >= 30 && currentAttentionLevel != AttentionLevelType.LEVEL_FOUR) {
	    		setAttentionLevel(AttentionLevelType.LEVEL_FOUR);
	    	}
	    	
	        try {
	          sleep(1000);//1sec
	          seconds++;
	        } catch (InterruptedException e) {
	          //escaped!
	        	return;
	        }
	      }
	    }
	  }
	
	//Execution Threads
	private AlertThread alertThread = new AlertThread("AT");
	private DemoEventThread eventThread = new DemoEventThread();

	/**
	 * Constructor. Initializes the panels and also created the intervention threads
	 * where necessary
	 */
	public ANIMAFrame() {
		//for testing. Needs to happen outside of init function
		debugPanel = initializeDebugPanel();
		
		//rendering tricks to improve redraw speed
	    System.setProperty("sun.java2d.noddraw", Boolean.TRUE.toString());
	    setDefaultLookAndFeelDecorated(true);
	    
		
		//initialize the Level 4 alert dialog
		alertDialog.setSize(alert.getPreferredSize());
    	alertDialog.add(alert);
    	alertDialog.setAlwaysOnTop(true);
    	alertDialog.setUndecorated(true);
    	alertDialog.getRootPane().setOpaque(false);
    	alertDialog.getContentPane ().setBackground (new Color (0, 0, 0, 0));
    	alertDialog.setBackground (new Color (0, 0, 0, 0));
    	alertDialog.setLocationRelativeTo(null);

    	//Initialize the level 3 timer that will handle the flickering of level 3 and 4 components
		flickerTimer = new Timer(400, new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
		    	Dimension size = frame.getSize();
		    	//resize the component to make it pop
		    	frame.setSize(size.width + 200, size.height + 200);
		    	
		    	if(currentAttentionLevel == AttentionLevelType.LEVEL_FOUR) {
		    		SwingUtilities.invokeLater(new Runnable() {
		                public void run() {
		                    // this executes on-EDT and causes the "!" to appear
		                	alertDialog.setVisible(true);
		                }
		            });
			    	
		    	}

		    	//rest for a moment
		    	try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	
		    	//resize the component back to the original size to create the full "flicker effect"
		    	frame.setSize(size);
		    	alertDialog.setVisible(false);
		    }    
		});
		
		//We want to keep flickering until we're told to stop
		flickerTimer.setRepeats(true);

		//Initialize us to the default attention level
		initialize(AttentionLevelType.LEVEL_ZERO);
		
		//initialize the menu
		initializeMenu();

		//start the event thread. Do this AFTER initializing the whole frame
		eventThread.start();
		
		//this.pack();
		this.setTitle("ANIMA - v1.0 - Demonstration System");
		this.setVisible(true);
	}
	
	/**
	 * Initializes the display to the desired alert level
	 * @param level AttentionLevelType severity of attention need
	 */
	public void initialize(AttentionLevelType level) {
		System.out.println("Initializing UI at attention level " + level);
		this.setLayout(layout);
		if(DEBUG_MODE) {
			this.setMinimumSize(MIN_DEBUG_SIZE);
		} else {
			this.setMinimumSize(MIN_SIZE);
		}
		this.setPreferredSize(frame.getSize());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//need to remove main panels if they already exist since this gets called more than once
		if(controlsPanel != null) this.remove(controlsPanel);
		if(statusPanel != null) this.remove(statusPanel);
		if(alertsPanel != null) this.remove(alertsPanel);
		if(viewportPanel != null) this.remove(viewportPanel);
		
		GridBagConstraints c = new GridBagConstraints();
		
		//Special consideration for if we're in level 3
		if(level == AttentionLevelType.LEVEL_THREE) {
			JPanel keyPanel = getKeyPanel(); 	
			System.out.println("Key Panel = " + keyPanel.getName());
			c.gridx = 0; c.gridy = 0; 
			c.gridwidth = 5; c.gridheight = 5;
			c.fill = GridBagConstraints.BOTH;
			this.add(keyPanel,c);
			this.revalidate();
			this.repaint();
			flickerTimer.start();
			return;
		}
		
		//init controls
		controlsPanel = initializeControls();
		
		//init status
		statusPanel = initializeStatus();
		
		//init alerts
		alertsPanel = initializeAlerts();
		
		//init viewport
		viewportPanel = initializeViewport();
		
		c.gridx = 0; c.gridy = 0; 
		c.gridwidth = 1; c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		this.add(controlsPanel,c);
		
		c.gridx = 0; c.gridy = 4;
		c.gridwidth = 1; c.gridheight = 2;
		this.add(statusPanel,c);
		
		c.gridx = 1; c.gridy = 0;
		c.gridwidth = 4; c.gridheight = 4;
		this.add(viewportPanel,c);

		c.gridx = 1; c.gridy = 4;
		c.gridwidth = 4; c.gridheight = 2;
		this.add(alertsPanel,c);

		if(DEBUG_MODE) {
			c.gridx = 0; c.gridy = 6;
			c.gridwidth = 5; c.gridheight = 1;
			this.add(debugPanel,c);
		}
		
		this.revalidate();
		this.repaint();
	}
	
	private JPanel getKeyPanel() {
		if(settingsPanel.getPriorityWindow() == KeyWindowType.ALERTS_TYPE) {
			return alertsPanel;
		} else if(settingsPanel.getPriorityWindow() == KeyWindowType.CONTROLS_TYPE) {
			return controlsPanel;
		} else if(settingsPanel.getPriorityWindow() == KeyWindowType.STATUS_TYPE) {
			return statusPanel;
		} else if(settingsPanel.getPriorityWindow() == KeyWindowType.VIEWPORT_TYPE) {
			return viewportPanel;
		} else {
			return null;
		}
	}
	
	private void initializeMenu() {
		JMenuBar menu = new JMenuBar();
		
		JMenu file = new JMenu("File");
		exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		exit.addActionListener(this);
		file.add(exit);
		
		JMenu edit = new JMenu("Edit");
		settings = new JMenuItem("Settings");
		settings.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		settings.addActionListener(this);
		edit.add(settings);
		
		JMenu help = new JMenu("Help");
		guide = new JMenuItem("Guide");
		guide.setAccelerator(KeyStroke.getKeyStroke('G', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		guide.addActionListener(this);
		
		about = new JMenuItem("About");
		about.addActionListener(this);
		help.add(guide);
		help.add(about);

		
		menu.add(file);
		menu.add(edit);
		menu.add(help);
		
		this.setJMenuBar(menu);
	}
	
	private JPanel initializeControls() {
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.setName("Controls");
		panel.setBorder(BorderFactory.createTitledBorder(panel.getName()));
		
		JOutlookBar bar = new JOutlookBar();
		JLabel test1 = new JLabel("Controls Go Here");
		JLabel test2 = new JLabel("Utilities Go Here");
		JLabel test3 = new JLabel("Preferences Go Here");
		
		//bar.addBar("Debug",debugPanel);
		bar.addBar("Controls",test1);
		bar.addBar("Preferences",test3);
		bar.addBar("Utilities",test2);
		
		if(!DEBUG_MODE) {
			bar.addBar("Demo",debugPanel);
		}
		
		panel.add(bar);
		return panel;
	}

	private JPanel initializeDebugPanel() {
		JPanel panel = new JPanel();
		panel.setName("Debug");
		
		attentionSlider = new JSlider(JSlider.HORIZONTAL,0,4,0);
		attentionSlider.setMajorTickSpacing(1);
		attentionSlider.setPaintTicks(true);
		attentionSlider.setPaintLabels(true);
		attentionSlider.addChangeListener(this);
		attentionSlider.setPreferredSize(new Dimension(125,50));
		
		panel.add(new JLabel("Current EEG Level: "));
		panel.add(eegLevel);
		
		desiredEEG.setColumns(4);
		desiredEEG.setText(String.valueOf(settingsPanel.getNeutralEEGLevel()));
		desiredEEG.addActionListener(this);
		panel.add(desiredEEG);
		
		panel.add(attentionSlider);
		
		return panel;
	}
	
	private JPanel initializeStatus() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setName("Status");
		panel.setBorder(BorderFactory.createTitledBorder(panel.getName()));
		
		panel.add(batStatus);
		panel.add(new JLabel("Battery"));
		
		panel.add(motorStatus);
		panel.add(new JLabel("Motor"));

		panel.add(connStatus);
		panel.add(new JLabel("Connection"));

		return panel;
	}
	
	private JPanel initializeAlerts() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setName("Alerts");
		panel.setBorder(BorderFactory.createTitledBorder(panel.getName()));
		
		String[] columns = {"Alert Description","Age","Priority","Actions"};
		Object[][] data = {{"Test1",120,"LOW","Dismiss"},
						{"Test2",30,"MEDIUM","View"},
						{"Test3",5,"HIGH","Surface"}};
		alertsTable = new JTable(new DefaultTableModel(data,columns));
		JScrollPane alertsPane = new JScrollPane(alertsTable);
		panel.add(alertsPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel initializeViewport() {
		JPanel panel = new JPanel();
		panel.setName("Viewport");
		panel.setBorder(BorderFactory.createTitledBorder(panel.getName()));
		
		JLabel player = new JLabel(new ImageIcon("./resources/images/video_screenshot.jpg"));
		panel.add(player);
		
		return panel;
	}
	
	/**
	 * Sets the EEG level of the user and triggers attention management if below the desired value
	 * @param level
	 */
	public void setEEGLevel(double level) {
		eegLevel.setText(String.valueOf(level));
		
		//if our attention has lapsed, start management
		if(level < settingsPanel.getNeutralEEGLevel()) {
			if(!alertThread.run) {
				alertThread.run = true;
				alertThread.start();
			}
		} else {
			//if we weren't already attentive, restore the display 
			if(currentAttentionLevel != AttentionLevelType.LEVEL_ZERO) {
				setAttentionLevel(AttentionLevelType.LEVEL_ZERO);
			}
			
			//stop the alert thread to reset and pause the timer
			if(alertThread.run) {
				System.out.println("Stopping AlertThread");
				alertThread.run = false;
				try {
					alertThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				alertThread = new AlertThread("AT");
		    	alertDialog.setVisible(false);
			}
		}
	}
	
	/**
	 * A direct way to set the attention level. Used primarily for debug purposes
	 * @param lvl
	 */
	public void setAttentionLevel(AttentionLevelType lvl) {
		System.out.println("Updating attention alert level to " + lvl);
		attentionSlider.removeChangeListener(this);
		switch(lvl) {
		case LEVEL_ONE:
			if(flickerTimer.isRunning()) {
				flickerTimer.stop();
		    	alertDialog.setVisible(false);
			}
			initialize(AttentionLevelType.LEVEL_ONE);
			getKeyPanel().setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(settingsPanel.getAttentionColor(),5), 
					BorderFactory.createTitledBorder(getKeyPanel().getName())));
			attentionSlider.setValue(1);
			break;
		case LEVEL_TWO:
			if(flickerTimer.isRunning()) {
				flickerTimer.stop();
		    	alertDialog.setVisible(false);
			}
			initialize(AttentionLevelType.LEVEL_TWO);
			getKeyPanel().setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(settingsPanel.getAttentionColor(),10), 
					BorderFactory.createTitledBorder(getKeyPanel().getName())));
			attentionSlider.setValue(2);
			break;
		case LEVEL_THREE:
	    	alertDialog.setVisible(false);
			initialize(AttentionLevelType.LEVEL_THREE);
			getKeyPanel().setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(settingsPanel.getAttentionColor(),10), 
					BorderFactory.createTitledBorder(getKeyPanel().getName())));
			attentionSlider.setValue(3);
			break;
		case LEVEL_FOUR:
			initialize(AttentionLevelType.LEVEL_THREE);
			getKeyPanel().setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(settingsPanel.getAttentionColor(),10), 
					BorderFactory.createTitledBorder(getKeyPanel().getName())));
			attentionSlider.setValue(4);
			//handled in level 3 timer
			break;
		default://LEVEL_ZERO
			attentionSlider.setValue(0);
			if(flickerTimer.isRunning()) {
				flickerTimer.stop();
			}
			initialize(AttentionLevelType.LEVEL_ZERO);
	    	alertDialog.setVisible(false);
		}
		attentionSlider.addChangeListener(this);
		
		currentAttentionLevel = lvl;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == exit) {
			System.exit(0);
		} else if(e.getSource() == settings) {
			int decision = JOptionPane.showConfirmDialog(this, settingsPanel.getSettingsPanel(), "User Settings", JOptionPane.OK_CANCEL_OPTION);
			if(decision == JOptionPane.OK_OPTION) {
				settingsPanel.save();
			}
		} else if(e.getSource() == guide) {
			JOptionPane.showMessageDialog(this, "In Version 2.0 this will launch an interactive browser");
		} else if(e.getSource() == about) {
			String copyright = 
					"Copyright 2016 held by Michael Kozak Permission to make digital or hard copies of \n" +
					"part or all of this work for personal or classroom use is granted without fee provided\n" +
					"that copies are not made or distributed for profit or commercial advantage and that \n" +
					"copies bear this notice and the full citation on the first page. Copyrights for \n" +
					"components of this work owned by others than ACM must be honored. Abstracting with \n" +
					"credit is permitted. To copy otherwise, to republish, to post on servers, or to \n" +
					"redistribute to lists, contact Michael Kozak mwk24@drexel.edu";
			JOptionPane.showMessageDialog(this, copyright);
		} else if (e.getSource() == desiredEEG) {
			settingsPanel.setNeutralEEGLevel(Double.valueOf(desiredEEG.getText()));
		}
	}

	@Override
	/**
	 * Only used by the debug slider
	 */
	public void stateChanged(ChangeEvent arg0) {
		JSlider source = (JSlider)arg0.getSource();
	    if (!source.getValueIsAdjusting()) {
	    	int alvl = (int)source.getValue();
	    	switch(alvl) {
	    	case 0:
	    		setAttentionLevel(AttentionLevelType.LEVEL_ZERO);
	    		break;
	    	case 1:
	    		setAttentionLevel(AttentionLevelType.LEVEL_ONE);
	    		break;
	    	case 2:
	    		setAttentionLevel(AttentionLevelType.LEVEL_TWO);
	    		break;
	    	case 3:
	    		setAttentionLevel(AttentionLevelType.LEVEL_THREE);
	    		break;
	    	case 4:
	    		setAttentionLevel(AttentionLevelType.LEVEL_FOUR);
	    		break;
	    	default:
	    		setAttentionLevel(AttentionLevelType.LEVEL_ZERO);
	    	}
	    }
	}

}
