/**
Mike Kozak
mwk24@drexel.edu
CS530:Project
*/
package cs530.project.datamodel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

/**
 * Class that tracks both user settings and provides the JPanel to be used in launching the settings
 * window. This ensures there is no chance of duplicate or out-of-sync settings values, as everything
 * is contained in a single class.
 * 
 * @author kozakm
 *
 */
public class UserSettings extends JPanel implements ActionListener {
	private static final long serialVersionUID = 5942952745202383236L;
	
	//default settings
	private double neutralEEGLevel = 950.00;
	private Color attentionColor = Color.red;
	private String audioFile = "";
	private KeyWindowType priorityWindow = KeyWindowType.ALERTS_TYPE;
	private boolean isColorblindMode = false;
	private boolean isAudioEnabled = false;
	

	//this will be used for the integer fields in shapes to force valid input
	protected NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);

    //flag to make sure we only build the panel once
    private boolean panelBuilt = false;
	
	//panel components
    private static Dimension DEFAULT_SIZE = new Dimension(500,500);
    private JFormattedTextField eegField = new JFormattedTextField(formatter);
    private JButton colorSelector = new JButton("Select...");//launches JColorChooser
    private JButton audioFileSelector = new JButton("Select...");//launches JFileChooser
    private JComboBox<KeyWindowType> keyWindowBox = new JComboBox<KeyWindowType>(KeyWindowType.values());
	private JCheckBox colorBlindCheckbox = new JCheckBox();
    private JCheckBox audioCheckbox = new JCheckBox();
    
    //File chooser to help the user find and select an audio file
    final JFileChooser fc = new JFileChooser();
	
    /**
     * Constructor. Adds itself as a listenre to the selectors
     */
    public UserSettings() {
    	//private Color attentionColor = Color.red;
    	//private String audioFile = "";
    	
    	eegField.setText(String.valueOf(neutralEEGLevel));
        //private JButton colorSelector = new JButton("Select...");//launches JColorChooser
        //private JButton audioFileSelector = new JButton("Select...");//launches JFileChooser
        keyWindowBox.setSelectedIndex(priorityWindow.ordinal());
    	colorBlindCheckbox.setSelected(isColorblindMode);
        audioCheckbox.setSelected(isAudioEnabled);

    	colorBlindCheckbox.addActionListener(this);
        audioCheckbox.addActionListener(this);
        colorSelector.addActionListener(this);
        audioFileSelector.addActionListener(this);
        
    }
	
	/**
	 * @return the neutralEEGLevel
	 */
	public double getNeutralEEGLevel() {
		return neutralEEGLevel;
	}
	/**
	 * @param neutralEEGLevel the neutralEEGLevel to set
	 */
	public void setNeutralEEGLevel(double neutralEEGLevel) {
		this.neutralEEGLevel = neutralEEGLevel;
	}
	/**
	 * @return the attentionColor
	 */
	public Color getAttentionColor() {
		return attentionColor;
	}
	/**
	 * @param attentionColor the attentionColor to set
	 */
	public void setAttentionColor(Color attentionColor) {
		this.attentionColor = attentionColor;
	}
	/**
	 * @return the audioFile
	 */
	public String getAudioFile() {
		return audioFile;
	}
	/**
	 * @param audioFile the audioFile to set
	 */
	public void setAudioFile(String audioFile) {
		this.audioFile = audioFile;
	}
	/**
	 * @return the priorityWindow
	 */
	public KeyWindowType getPriorityWindow() {
		return priorityWindow;
	}
	/**
	 * @param priorityWindow the KeyWindowType to set
	 */
	public void setPriorityWindow(KeyWindowType priorityWindow) {
		this.priorityWindow = priorityWindow;
	}
	/**
	 * @return the isColorblindMode
	 */
	public boolean isColorblindMode() {
		return isColorblindMode;
	}
	/**
	 * @param isColorblindMode the isColorblindMode to set
	 */
	public void setColorblindMode(boolean isColorblindMode) {
		this.isColorblindMode = isColorblindMode;
	}
	/**
	 * @return the isAudioEnabled
	 */
	public boolean isAudioEnabled() {
		return isAudioEnabled;
	}
	/**
	 * @param isAudioEnabled the isAudioEnabled to set
	 */
	public void setAudioEnabled(boolean isAudioEnabled) {
		this.isAudioEnabled = isAudioEnabled;
	}
	
	/**
	 * Returns the settings panel. Will build it if the panel hasn't been created yet
	 * @return JPanel settings panel
	 */
	public JPanel getSettingsPanel() {
		if(this.panelBuilt) {
			return this;
		} else {
			this.panelBuilt = true;
			return buildSettingsPanel();
		}
	}

	/**
	 * Builds the settings panel for use by the frame
	 * @return JPanel
	 */
	private JPanel buildSettingsPanel() {
		this.setLayout(new GridBagLayout());
		this.setSize(DEFAULT_SIZE);
		this.setMinimumSize(DEFAULT_SIZE);
		
		JLabel eegLabel = new JLabel("Neutral EEG Level: ");
		JLabel colorLabel = new JLabel("Attention Color: ");
		JLabel audioLabel = new JLabel("Audio Alert: ");
		JLabel priorityWindowLabel = new JLabel("Key Component: ");
		JLabel colorblindLabel = new JLabel("Enable Colorblind Mode: ");
		JLabel audioAlertLabel = new JLabel("Enable Audio Alerts: ");
		
		eegField.setColumns(6);
		
		audioFileSelector.setEnabled(false);
		
		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(0,0,10,10);  //top padding
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0; c.gridy = 0;
		this.add(eegLabel,c);
		c.gridx = 1; c.gridy = 0;
		this.add(eegField,c);

		c.gridx = 0; c.gridy = 1;
		this.add(colorLabel,c);
		c.gridx = 1; c.gridy = 1;
		this.add(colorSelector,c);

		c.gridx = 0; c.gridy = 2;
		this.add(audioLabel,c);
		c.gridx = 1; c.gridy = 2;
		this.add(audioFileSelector,c);
		
		c.gridx = 0; c.gridy = 3;
		this.add(priorityWindowLabel,c);
		c.gridx = 1; c.gridy = 3;
		this.add(keyWindowBox,c);

		c.gridx = 0; c.gridy = 4;
		this.add(colorblindLabel,c);
		c.gridx = 1; c.gridy = 4;
		this.add(colorBlindCheckbox,c);
		

		c.gridx = 0; c.gridy = 5;
		this.add(audioAlertLabel,c);
		c.gridx = 1; c.gridy = 5;
		this.add(audioCheckbox,c);
		
		return this;
	}
	
	public void save() {
		neutralEEGLevel = Double.valueOf(eegField.getText());
        priorityWindow = (KeyWindowType) keyWindowBox.getSelectedItem(); 
        isColorblindMode = colorBlindCheckbox.isSelected();
        isAudioEnabled = audioCheckbox.isSelected();
        
        //private JButton colorSelector = new JButton("Select...");//launches JColorChooser
        //private JButton audioFileSelector = new JButton("Select...");//launches JFileChooser
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == colorSelector) {
			//Use the color chooser for selecting the new alert color
			attentionColor = JColorChooser.showDialog(this, "Select an Alert Color", Color.RED);
			
		} else if (arg0.getSource() == audioFileSelector) {
			//Interact with the user to select an audio file
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            //This is where a real application would open the file.
	            if(file.exists()) {
	            	audioFile = file.getPath();
	            }
	        }
			
		} else if (arg0.getSource() == colorBlindCheckbox) {
			colorSelector.setEnabled(!colorBlindCheckbox.isSelected());
			if(colorBlindCheckbox.isSelected()) {
				attentionColor = Color.BLACK;
			} else {
				attentionColor = Color.RED;
			}
		} else if (arg0.getSource() == audioCheckbox) {
			audioFileSelector.setEnabled(audioCheckbox.isSelected());
		}
	}
}
