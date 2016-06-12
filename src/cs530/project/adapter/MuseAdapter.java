/**
Mike Kozak
mwk24@drexel.edu
CS530:Project
*/
package cs530.project.adapter;

import cs530.project.listeners.ISensorListener;
import oscP5.OscMessage;
import oscP5.OscP5;

/**
 * This class acts as the bridge between the MUSE EEG sensor and ANIMA. It is responsible
 * for connecting to the device on startup, maintaining a connection if necessary, and translating data from the device to the internal
 * representation.
 * @author mkozak
 *
 */
public class MuseAdapter  {

	//handle to this class to ensure only one port gets opened to the MUSE
    static MuseAdapter museOscServer = null;

    //handle to the class that is interested in callbacks when updates are received
    ISensorListener listener = null;

    //Direct connetion to the MUSE over bluetooth
    OscP5 museServer = null;

    /**
     * Connects to the MUSE
     * @param museAdapter Adapter to use in connecting to the server
     * @param port port to use
     * @param type UDP or TCP connection type
     */
    public void initializeServer(MuseAdapter museAdapter, int port, int type) {
    	museServer = new OscP5(museAdapter, port, type);
    }
    
    /**
     * Shuts down the server to minimize the chance of a port getting blocked on subsequent runs
     */
    public void disconnect() {
    	System.out.println("Disconnecting from MUSE Service");
    	museServer.dispose();
    }

    /**
	 * {@inheritDoc}
	 */
    void oscEvent(OscMessage msg) {
    	if(msg == null) {
    		System.out.println("Bad MSG!");
    	}
        //System.out.println("### got a message " + msg);
        if (msg.checkAddrPattern("/muse/eeg") == true) {	
            for (int i = 0; i < 4; i++) {
                //System.out.print("         EEG on channel " + i + ": " + msg.get(i).floatValue() + "\n");
            }

            if (listener != null) {
                listener.handleSensorData(msg);
            }
        } /*else if (msg.checkAddrPattern("/muse/acc") == true) {
            for (int i = 0; i < 4; i++) {
                //System.out.print("Acc on channel " + i + ": " + msg.get(i).floatValue() + "\n");
            }

            if (listener != null) {
                listener.handleSensorData(msg);
            }
        }*/
    }

	/**
	 * Function to register a class as a callback listener on updates
	 * @param listener ISensorListener implementation of the listener interface
	 */
    public void registerListener(ISensorListener listener) {
        this.listener = listener;
    }
}
