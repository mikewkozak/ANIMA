# ANIMA
Adaptive Neurological Interface for Multimodal Applications (ANIMA) code repository

Mike Kozak
mwk24@drexel.edu

Overview:
---------
ANIMA combines industry standard guidelines on interface design with a wireless EEG sensor to determine user attention levels. Based on those levels it selects from one of a series of intervention strategies, such as color shifting, tonal alerts, and window restructuring. The GUI is continually modified until attention is restored, at which point it reverts to the original layout to preserve context. Experimental results show this approach is effective at reducing attention loss during extended operations.


Running:
--------
1. Double click on ANIMA.jar to begin the program
3. The system should start and run. All necessary libraries and files are packaged into the JAR

Running from Eclipse:
--------------------
1. Import the project into Eclipse
2. Run Adminstrator.java as a Java Application (no arguments needed) 
3. The system should start and run. All necessary JARs and files are in the lib and resources directories


During Execution:
--------------------
*You can enable/disable the debug panel at the bottom of the frame by setting the DEBUG_MODE flag at the top of the ANIMAFrame.java class
*To toggle attention level, slide the slider to the desired level and observe the effect. Simply slide back to level zero to restore to the default settings

NOTE: If you have a MUSE EEG connected via bluetooth with muse-io running in the background, ANIMA will automatically begin processing EEG data (make sure MUSE Lab isn't running as it blocks the connection port) 


Known issues
------------
v1.0:
*With DEBUG_MODE disabled, there is no way to force the system back into the neutral state from levels 3 and 4 if the neutral EEG level is set too high and is unreachable via sensing. Enable DEBUG_MODE to avoid.
*Flicker at attention levels 3 and 4 is inconsistent when moving the mouse across the screen
*Audio Playback is not currently implemented but not disabled in the settings

