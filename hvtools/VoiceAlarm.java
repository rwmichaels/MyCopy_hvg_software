//** Last update 28-Jun-2018
package hvtools;

import java.applet.*;
import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import javax.sound.midi.*;
import java.io.*;

public class VoiceAlarm  {
    URL completeURL;
    AudioClip audioClip;
    URL baseURL;
    String relativeURL;
    String wavFile = "1-welcome.wav";
    // String wavFile = "alarm_3.wav";
    private boolean loaded = false;
    SoundList soundList;
    File alarm_file;
	
    public VoiceAlarm(String file) {
	try {
	    baseURL = new URL("file:" + System.getProperty("user.dir") + "/");
	} catch (MalformedURLException e){
            System.err.println(e.getMessage());
        }
	wavFile = file;
        soundList = new SoundList(baseURL);
        soundList.startLoading(wavFile);
	System.out.println(baseURL+file);
	alarm_file= new File("../bin/beep/hv_remote_beep");
    }
    
    public void add(String file) {
	soundList.startLoading(file);
   
    } 

    public void stop() {
       if(audioClip != null)
	   audioClip.stop();        //Cut short the one-time sound.
    }
   
 
    public void play() {
	audioClip = soundList.getClip(wavFile);
	if(audioClip != null) {
	    final SwingWorker worker = new SwingWorker() {
		    public Object construct() {
			System.out.println("Playing...");
			audioClip.play();     //Play it once.
                        try {
			     final String alarm_script = alarm_file.getCanonicalPath(); 

//                             Process p = Runtime.getRuntime().exec("ssh adaq@hapc7 /usr/bin/play /home/adaq/alarms/harp.wav");
//**                            Process p = Runtime.getRuntime().exec("~/slowc/bin/beep/hv_remote_beep");
                             Process p = Runtime.getRuntime().exec(alarm_script);

                        }
                        catch (IOException e) {
                            System.out.println("Exception in remote voice alarm happened - here's what I know: ");
                            e.printStackTrace();
                        }
			return "done";
		    }
		    public void finished() {
			System.out.println("PlayEnd");
		    }
		};
	    worker.start();
	}
    }
    
    public void playF(String file) {
	audioClip = soundList.getClip(file);
	if(audioClip != null) {
	    final SwingWorker worker = new SwingWorker() {
		    public Object construct() {
			System.out.println("Playing...");
			audioClip.play();     //Play it once.
			return "done";
		    }
		    public void finished() {
			System.out.println("PlayEnd");
		    }
		};
	    worker.start();
	}

    }
    
    static void main(String args[]) {
	VoiceAlarm va = new VoiceAlarm("1-welcome.wav");
	System.out.println("Loading...");

	
	try {
	    Thread.sleep(1000);
	    
	} catch (InterruptedException ie) {
	}   
	System.out.println("Start Play...");
	va.play();

	System.out.println("Playing...");
	try {
	    Thread.sleep(7000);
	    
	} catch (InterruptedException ie) {
	}   
	va.stop();
	System.out.println("Stop...");
	
	
	System.exit(0);  
    }
    
}
    
