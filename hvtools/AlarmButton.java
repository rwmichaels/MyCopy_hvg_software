package hvtools;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Font;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import java.util.*;
import java.lang.*;

import hvframe.*;


public class AlarmButton extends JFrame {
    
    private  HVframe hvf = null;
    private JButton saButton = null; // alarm silence button
    private Color acolor = Color.red.brighter();
    private Color bcolor = Color.white;;
    javax.swing.Timer ctimer;
    private ImageIcon bicon;
    public VoiceAlarm voiceAlarm ;
    public VoiceAlarm signalAlarm ;
    private String voiceAlarmFile = "hvalarm.wav" ;
    private String signalAlarmFile = "harp.wav" ;
    private static boolean sound;
    

    public AlarmButton(String title, HVframe f) {
	super(title);
	this.sound = sound;
	bicon = new ImageIcon("./images/middle.gif");
	saButton = new JButton("Press to "+title ,bicon);
	saButton.setBackground(bcolor);
	saButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    stopAnimation();
		    setVisible(false);
		}
	    });

	hvf = f;		


	saButton.setBorderPainted(true);
	saButton.setFont(new Font("Hevletica", Font.BOLD, 20));
	saButton.setPreferredSize(new Dimension(600, 100));
	
	// voice alarm;
	voiceAlarm = new VoiceAlarm(voiceAlarmFile);
	signalAlarm = new VoiceAlarm(signalAlarmFile);
			
	ctimer = new javax.swing.Timer(1000, new ActionListener() {
		boolean blink = false;
		boolean first = true;
		int cnt = 0;
		int cnt2 = 0;
		public void actionPerformed(ActionEvent evt) {	
		    if(first) {
			if(isSoundON()) {
			    signalAlarm.play();
			    voiceAlarm.play();
			}
			first = false;
			cnt = 0;
			cnt2 = 0;
		    }
		    if (blink=!blink) {
			saButton.setBackground(acolor);
		    } else {
			saButton.setBackground(bcolor);
			cnt ++;
			cnt2++;
			if(cnt2 == 4) {
			    if(isSoundON())  signalAlarm.play();	
			  cnt2 = 0;
			}
			
			if(cnt == 20) {
			    if(isSoundON())   voiceAlarm.play();		
			 cnt = 0;
			}
		    }
		}
	    });
	
	
	
	JPanel panel = new JPanel();
	panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	panel.add(saButton, BorderLayout.CENTER);

        Container contentPane = getContentPane();
	
	//contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	
	contentPane.add(panel, BorderLayout.CENTER);
	//setSize(400, 300);
	//Component comp = null;
	//setLocationRelativeTo(comp);
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE );

	// centering on the screen
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxX = screenSize.width ; 
        int maxY = screenSize.height ; 
	setLocation(maxX/2 - 200, maxY/2 - 50);
	pack();
	setVisible(true);
	toFront();
	
	delay(1000);
	startAnimation();

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    //		    stopAnimation(); 
		}
	    });
	
    }
    
    //Can be invoked from any thread.
    public synchronized void startAnimation() {
	//Start animating!
	if (!ctimer.isRunning()) {
	    ctimer.start();
	}
    }
    
    //Can be invoked from any thread.
    public synchronized void stopAnimation() {
        //Stop the animating thread.
        if (ctimer.isRunning()) {
            ctimer.stop();
	    if (voiceAlarm!=null) voiceAlarm.stop();
	    if (signalAlarm!=null) signalAlarm.stop();
	    hvf.alarmHVOFF = false;
	    hvf.updateLabelStatus();
	}
    }
    

    public void delay(int msec) {
	try {
	    Thread.sleep(msec);
	} catch (InterruptedException ie) {
	}
	
    }
    
    public boolean isSoundON() {
	return hvf.soundOn;
    } 
    
static void main(String[] args) {
	AlarmButton ab = new AlarmButton("Alarm Silence of hv06", null);
    }
    
    
}
