/**
Mike Kozak
mwk24@drexel.edu
CS530:Project
*/
package cs530.project.listeners;

/**
 * Callback listener for for Sensor Manager
 * @author mkozak
 *
 */
public interface ISensorListener {

	/**
	 * Callback to a listener when data is received from a hardware sensor
	 * @param data translated Object of an internal data type
	 */
	public void handleSensorData(Object data);
}
