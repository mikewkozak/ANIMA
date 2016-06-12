/**
Mike Kozak
mwk24@drexel.edu
CS530:Project
*/
package cs530.project;

import javax.swing.JFrame;

import cs530.project.adapter.MuseAdapter;
import cs530.project.display.ANIMAFrame;
import cs530.project.listeners.ISensorListener;
import oscP5.OscMessage;
import oscP5.OscProperties;

/**
 * Main class that kicks off the MUSE and frame. It also handles data transfer from the MUSE
 * via callbacks to the display as necessary and tracks what the "alertness level" of the user is
 * @author Mike Kozak
 *
 */
public class Administrator extends JFrame implements ISensorListener{
	private static final long serialVersionUID = 7856122828676112162L;

	//Adapter for connecting to the MUSE and getting streaming updates
	protected MuseAdapter museAdapter = null;
	
	//UI
	protected ANIMAFrame frame = null;
	
	//Atention Level metrics
	protected double currentEEGLevel = 0.0;
	protected double runningAverage = 0.0;
	protected double runningTotal = 0.0;
	protected long count = 0;
	
	/**
	 * Constructor. Initializes the adapter and kicks off the frame
	 */
	public Administrator() {
		//initialize the MUSE adapter to get input streaming
		museAdapter = new MuseAdapter();
		museAdapter.registerListener(this);
		museAdapter.initializeServer(museAdapter, 5000, OscProperties.TCP);
		
		//initialize main windows
		frame = new ANIMAFrame();
		
		//test
		//frame.setAttentionLevel(AttentionLevelType.LEVEL_ZERO);
		
		//Make sure we disconnect from the adapter on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    @Override
		    public void run()
		    {
		    	System.out.println("Disconnecting...");
		        museAdapter.disconnect();
		    }
		});
	} 
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void handleSensorData(Object data) {
		
		//Let's make sure this is the message we want
		if(data instanceof OscMessage) {
			final OscMessage msg = (OscMessage)data;
			
			//For now, only bother to get the EEG readings
			if (msg.checkAddrPattern("/muse/eeg") == true)
			{
				double average = 0.0;			
				for (int i = 0; i < 4; i++) {
					average += msg.get(i).floatValue();
					//System.out.print("EEG on channel " + i + ": " + msg.get(i).floatValue() + "\n");
				}
				
				//generate the average readings of the sensors
				average /= 4.0;
				runningTotal += average;
				count++;
				runningAverage = runningTotal / count;
				//System.out.println("Average EEG value = " + runningAverage);
				
				//Update the frame with the new level
				frame.setEEGLevel(average);
			}
		}
	}

	/**
	 * Main. Kicks off the Administrator
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Administrator admin = new Administrator();
	}
}
