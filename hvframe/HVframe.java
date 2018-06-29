// Last update: 13-Dec-2011
// Modd&Add: 02-Oct-01 add progressBar while init state
//    Dec 01:  add alarm handler for processing channel trips,
//                  panicoff state, lost connection state.
//                  add timer for beepper,
//                  add processing of 'config' command,
//                  add view of panicoff, remote/local,
//                      bad battery, bad EEPROM, bad 24V states
// 17-Jan-02: add flashing for alarmed channel, pwrerr icon is set to the tabb when frame is in alarm condition 
// 27-Jan-02: add alarm for monitoring HVON to HVOFF changing not from GUI
// 04-Feb-02: add alarm masking and move voice alarm to AlarmButton class. 
// 14-Feb-02: add variables for telnet client
// 14-Feb-02: add variables for telnet client
// 27-May-05: optimizing by time of modules initialization, lookup module info from previouse modules to init current modile.
// 22-Jun-05: add 'close()' method; set default delay for monitor to 5sec.  
// 03-Sep-05: change return type of getModuleSlot() from integer to String to support submodule
//            addressing in form(module in slot 11 with 2 submodules) S11S0,S11S1.
//            Change input parameter for getModuleIndex() from integer to String.  
// 16-Oct-05: replace parameter in command.exec() from 'name'=servername to 'host'=servername:port
//            replace parameter in init HVMonitor() from 'ID' to 'host'=servername:port
// 26-Oct-05: add method setRemoteColor(Color) to set color of remote label from monitor

// 13-Dec-2011: MAXUNIT has changed from 16 to 32 take to account of submodules in module

package hvframe;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.net.*;
import java.io.*;

import hvtools.*;

/**
 * class HVframe - implements 
 *               initialization of HVmainframe, starts 'client' to communicate
 *               with HVmainframe, starts 'monitor' to periodicaly looking of changing
 *               global summary numbers, asking new values for changing data
 *               (measurement voltage(MV),demand voltage(DV),
 *               measurement current(MC),channels status(ST) and others) 
 *               from mainframe. Also includes JTabbedPannel for 
 *               representation of installed HVmodules.
 *  @version 1.2
 *  Last update: 24-Dec-01
 */
public class HVframe  extends JPanel  implements Cmd, Printable, Runnable  {
    public int id = 0; 
    public String name = null; // name of HVframe(realy hostname)
    public int    port = 0; // port of HVframe
    public String host =null; // full name of HVframe: "name"+":"+"port"
    public String sysdef = null;
    public Vector enet = new Vector();
    public Vector sysinfo = new Vector(); // store sysinfo from HVframe
    public String pupstatus = null; 
    public int    nunit = 0; // number of modules installed in HVframe
    public String unames = null; //names of modules(slot number like: SN)
    public Vector ltos = new Vector(); // store slot numbers
    /* Dec 13 2011 changed MAXUINT from 16 to 32 (extended for submodules) */
    public final int MAXUNIT = 32; // max number of units per HVframe
    private String command = new String(); // store command string
    public String[] moduleInfo = null; // store modules info(returned ID comand)
    public HVMessage mess = null;
    public HVCommand hvcom = null; 
    public HVmodule[] hvm = null; // array of HVmodules objects 
    public HVclient hvcli = null; // client object
    public HVmonitor hvmon = null; // monitor object
    final public HVListener notifys;  // interface to update Status window
    ImageIcon ipwron  = new ImageIcon("./images/pwron.gif"); //power on icon
    ImageIcon ipwroff = new ImageIcon("./images/pwroff.gif"); // power off icon
    ImageIcon ipwrup  = new ImageIcon("./images/pwrup.gif"); // power off icon
    ImageIcon ipwrdown = new ImageIcon("./images/pwrdown.gif"); // power off icon
    ImageIcon ipwrerr = new ImageIcon("./images/pwrerr.gif"); // power error icon
    ImageIcon ipwrrmp = new ImageIcon("./images/pwrramp.gif"); // power ramping icon
    public JTabbedPane ftabb = null;
    public JTabbedPane tabb = new JTabbedPane(); // jtabbed pannel to represent 
    // of HVmodules
    JRadioButton onButton = null;  // HV ON button    
    JRadioButton offButton = null; // HV OFF button
    private JPanel radioPanel = new JPanel();
    private JPanel buttonPanel = null;
    private JPanel statusPanel = null;
    public boolean HVONstatus = false; // curent status of buttons
    private String status = new String("OK");
    private boolean errStatus = false;
    private String GSnumber = null;
    private String LSnumber = null;
    private String[] PSnumber = new String[MAXUNIT];
    private String Config = null;
    private String response = null;
    private Thread frameThread = null;
    private boolean initdone = false;
    public JPanel wpanel = new JPanel();
    public JLabel label = null;
    public ImageIcon initicon = new ImageIcon("./init.gif");
    public JProgressBar progressBar = null;
    public JLabel panicLabel = null;
    public JLabel hvonLabel = null;
    public JLabel remoteLabel = null;
    public JLabel eepromLabel = null;
    public JLabel psLabel = null;
    public JLabel batteryLabel = null;

    private ImageIcon hvonenic = null;
    private ImageIcon hvondisic = null;
    private ImageIcon hvoninvic = null;
    private ImageIcon hvonic = null;
    private ImageIcon hvpanicen = null;
    private ImageIcon hvpanicdis = null;
    private ImageIcon hvpanic = null;
    private ImageIcon hvoffb = null;
    private ImageIcon hvoffbpr = null;
    private ImageIcon hvoffbrl = null;
    private ImageIcon hvonb = null;
    private ImageIcon hvonbpr = null;
    private ImageIcon hvonbrl = null;
    private ImageIcon hvonbs = null;

  

    //** store status of mainframe EEPROM, if false - bad EEPROM status
    private boolean statusEEPROM = true; 
    //** store status of front panel switch, if false - in local mode (true - remote)
    private boolean remoteSwitch = true;
    //** store status of mainframe Battery, if false - bad Battery status
    private boolean statusBattery = true;
    //** store status of mainframe 24V power supply, if false - bad 24V status
    private boolean status24V = true;
    //** store status of mainframe Panic OFF switch, if true - Panic OFF is active
    private boolean statusPanicOFF = false;
    //
    public boolean alarmHVOFF = false; // set to true if frame power OFF not from GUI

    private boolean statusHVOFF = false; //store previouse power state of mainframe
                                         //if true mainframe is HVOFF

    public boolean soundOn = true;

    private boolean alarmSet = false;
    private boolean moduleAlarmSet = false;
    private Vector modulesTrip = new Vector();
    javax.swing.Timer ramptimer = null;
    javax.swing.Timer alarmBeepper = null;
    public final static int ONE_SECOND = 1000;
    private Color tabbColor = null; // default hvframe tabb bg color
    private Color tabbAlarmColor = Color.red.brighter(); // hvframe tabb alarm background(bg) colo
    private Color tabbSelColor = null; // hvframe tabb selected color
    // private String voiceAlarmFile = "hvalarm.wav" ;
    // private String signalAlarmFile = "harp.wav" ;
    //    public VoiceAlarm voiceAlarm ;
    //public VoiceAlarm signalAlarm ;
    private AlarmButton alButton;

    // alarm masks
    private boolean chTripMask = true;
    private boolean hvErrorMask = true;
    private boolean panicOffMask = true;
    private boolean noConnectionMask = true;
    private boolean hvOffMask = true;

    //add for telnet client
    static final int TCP_IP_PROTOCOL = 1;
    static final int TELNET_PROTOCOL = 2;

    public String login;
    public String password;

    public int protocol = TCP_IP_PROTOCOL;
    public static final String TCP = "tcp";
    public static final String TELNET = "telnet";
    public int monitorDelay = 5000; // 5sec delay of monitor scanning
    
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor crossCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    public Color remloc = new Color(0,200,0);
    public Color remhic = new Color(50,250,50);
    public Color remc = new Color(20,120,0);
  

     // constructors
    public HVframe(String server,int hvport, String protcl, String login, String password, 
		   JTabbedPane ftabb, HVListener notifys ) {
	super(false);
	this.name = server;
	this.port = hvport;
	this.host = name+":"+port; // Jun 6,2005
	this.id = id;
	this.protocol = TCP_IP_PROTOCOL;
	if(protcl!=null)
	    if(protcl.equalsIgnoreCase(TELNET)) this.protocol = TELNET_PROTOCOL;
	this.login = login;
	this.password = password;
	this.mess = new HVMessage();
	this.notifys = notifys;
	this.ftabb = ftabb;
	frameThread = new Thread(this, host);
	hvonenic = new ImageIcon("./images/hvong.gif");
	hvondisic = new ImageIcon("./images/hvoffg.gif");
	hvoninvic = new ImageIcon("./images/hvonbl.gif");
	hvpanicdis = new ImageIcon("./images/panicg.gif");
	hvpanicen = new ImageIcon("./images/panicr.gif");
	hvoffb = new ImageIcon("./images/hvoffb.gif");
	hvoffbpr = new ImageIcon("./images/hvoffbpr.gif");
	hvoffbrl = new ImageIcon("./images/hvoffbrl.gif");
	hvonb = new ImageIcon("./images/hvonb.gif");
	hvonbpr = new ImageIcon("./images/hvonbpr.gif");
	hvonbrl = new ImageIcon("./images/hvonbrl.gif");
	hvonbs = new ImageIcon("./images/hvonbs.gif");

	hvonic = hvondisic;
	hvpanic = hvpanicdis;
	onButton = new JRadioButton(hvonb);
	onButton.setHorizontalAlignment(SwingConstants.CENTER);
	onButton.setBorderPainted(true);

	offButton = new JRadioButton(hvoffb);
	offButton.setHorizontalAlignment(SwingConstants.CENTER);
	offButton.setBorderPainted(true);

	offButton.setPressedIcon(hvoffbpr);
	offButton.setRolloverIcon(hvoffbrl);

	onButton.setPressedIcon(hvonbpr);
	onButton.setRolloverIcon(hvonbrl);
	onButton.setSelectedIcon(hvonbs);
	
	// voice alarm;
	// voiceAlarm = new VoiceAlarm(voiceAlarmFile);
	// signalAlarm = new VoiceAlarm(signalAlarmFile);

	label = new JLabel(host+" :INITIALIZATION IN PROGRESS...",initicon,SwingConstants.CENTER);

	monitorDelay = MONITOR_DELAY; // delay in msec of monitors scan

	// start hvclient thread	
	if(protocol == TCP_IP_PROTOCOL)	
	    this.hvcli = new HVclient(this, notifys );

	if(protocol == TELNET_PROTOCOL) {
	    this.hvcli = new HVTelnetClient(this, notifys );
	    monitorDelay = 5000; // default 5sec monitor scan delay for telnet protocol
	}

	if(!hvcli.getErrStatus()) {
	    hvcli.start();
	    status = "HV client started ("+protocol+")-> "+server;	    
	    
	    this.setLayout(new BorderLayout()); 

	    wpanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	    //	    wpanel.setBackground(new Color(200,200,255)); 
	    wpanel.setBorder(BorderFactory.createCompoundBorder(
		  BorderFactory.createTitledBorder(
		    "High Voltage Mainframe LeCroy-1458"),
		  BorderFactory.createEmptyBorder(10,10,10,10)));
	    wpanel.add(label,BorderLayout.CENTER);
	    wpanel.setPreferredSize(new Dimension(700,400));
	    //	    this.add(wpanel,BorderLayout.CENTER);


	    // create command obj
	    this.hvcom = new HVCommand(mess, notifys);
	    command = CONFIG ;
	    // ask hvframe about config information
	    if(hvcom.exec(command,host)) {
		// get response
		while(!hvcom.isEmpty()) {
		    //System.out.println("in while response.isEmpty:"+hvcom.isEmpty());
		    Config =  hvcom.getResponse();
		}
	    } else { 

	    }

	    //	    System.out.println("...response.isEmpty:"+hvcom.isEmpty());
	    // unpack configuration word (it's use only first one)
	    parseConfigWord(Config);
	    alarmHVOFF = false ; //reset alarm after init GUI

	    command = SYSINFO;
	    sysinfo.clear();
	    // ask hvframe about system information
	    if(hvcom.exec(command,host)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		sysinfo.add((String) hvcom.getResponse());
	    }
	    }

	    command = PUPSTATUS;
	    // ask hvframe about system information
	    if(hvcom.exec(command,host)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		pupstatus = hvcom.getResponse();
	    }
	    }

	    command = ENET;
	    enet.clear();
	    // ask hvframe about system information
	    
	    if(hvcom.exec(command,host)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		enet.add((String) hvcom.getResponse());
	    }
	    }
	  
	    command = HVSTATUS;
	    // ask hvframe about current status
	    if(hvcom.exec(command,host)) {
	    // get response
	    String st = new String("HV?");
	    while(!hvcom.isEmpty()) {
		       st = hvcom.getResponse();
	    }
	    st=st.trim();
	    notifys.updateStatus(host+":"+st);
	    if(st.equalsIgnoreCase("HVON")) {
		HVONstatus = true;
	    }
	    }

	    command = LL;
	    // ask hvframe about installed modules(units)
	    if(hvcom.exec(command,host)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		unames =  hvcom.getResponse();
	    System.out.println("UNAMES:"+unames);
	    
	    StringTokenizer s = new StringTokenizer(unames.trim());
	    nunit = s.countTokens();
	    //	    System.out.println("NUNIT: "+nunit);
	    
	    for(int i = 0; i< nunit;i++) {
	        String sub=s.nextToken();
		ltos.add(sub);
	    } 
	    }
	    progressBar = new JProgressBar(0, nunit);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    progressBar.setBorderPainted(true);
	    progressBar.setForeground(new Color(0,180,0));
	    //	    progressBar.setBackground(Color.blue);

	    wpanel.add(progressBar,BorderLayout.CENTER);
	    this.add(wpanel, BorderLayout.CENTER);	
	    Cursor cur = getCursor();
	    wpanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));

	    //start initilization thread
	    //    final SwingWorker worker = new SwingWorker() {
	    //    public Object construct() {
	    //runInit();
	    ///	return "Init done"; //return value not used by this program
	    //    }
	    //    //Runs on the event-dispatching thread.
	    //      public void finished() {
	    //    }
	    //};
	    //worker.start();  //required for SwingWorker 3
	    frameThread.start();
	    ftabb.setCursor(cur);
	    
	    }
	    

	} else {
	    status = hvcli.getStatus();
	    errStatus = hvcli.getErrStatus(); 
	    notifys.updateStatus(status);
	    //	    hvcli.stopClient();
	    //notifys.updateStatus("Stop hvclient for "+server);
 	}
   }
    
    public void startMonitor(int delay) {
	
	// start monitors
	hvmon = new HVmonitor(host, mess, delay, this, notifys);
	hvmon.start();
	notifys.updateStatus("HV monitor started -> "+host);
    }


    public void run() {
	    // create hvmodules that installed in frame 
	    this.hvm = new HVmodule[nunit];
	    moduleInfo = new String[nunit];
	    //	    ftabb.setCursor(waitCursor);

	    // ask about modules info
	    for(int i=0 ;i<nunit;i++) {		
		command = ID +" "+"L"+i;
		// ask hvframe about  modules info
		if(hvcom.exec(command,host)) {
		    // get response
		    while(!hvcom.isEmpty()) {
			moduleInfo[i]  = new String(hvcom.getResponse());
		    }		
		} 
	    }

	    // init modules
	    boolean init_flag = true;	    
	    for(int i=0 ;i<nunit;i++) {
		String slot = (String)ltos.elementAt(i);
		if (i==0) hvm[i] = new HVmodule(mess,host,i,slot,moduleInfo[i],notifys);
		if (i>0) {
		    // lookup, if  next module has the same model as one of  previouse, 
		    // then take parameters and attributes for 
		    // initialization of next module from this previouse module.

		    init_flag = true;
		    for(int k=0;k<i;k++) {
			// check Model and SubModule number
			if(( hvm[k].getModel().equalsIgnoreCase(hvm[k].getword(moduleInfo[i],0)) )
			   &( hvm[k].getSubChnNum().equalsIgnoreCase(hvm[k].getword(moduleInfo[i],1)) ) ) 
{
			//init next module with parameters of previouse module
			 hvm[i] = new HVmodule(mess,host,i,slot,moduleInfo[i],hvm[k].getParameters(),hvm[k].getAttributes(),notifys);
			 init_flag = false;
			 break;
		    } 
		    
		    }
		if(init_flag) {
			// use default initialization (ask module about parameters and attributes)
			hvm[i] = new HVmodule(mess,name,i,slot,moduleInfo[i],notifys);

		    }
		}

		updateInitStatus(i);
	    }
	    // get summary numbers and store its as Strings
	    command = GS;
	    // ask hvframe about Global Summary Numbers
	    if(hvcom.exec(command,host)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		response =  hvcom.getResponse();
	    }
	        GSnumber = response.trim();
	    }

	    command = LS;
	    // ask hvframe about Logical Units Summary Numbers
	    if(hvcom.exec(command,host)) {
	    // get response
	    response =  hvcom.getResponse();
	    LSnumber =  response.trim();
	    while(!hvcom.isEmpty()) {
		  response  = LSnumber +" "+ hvcom.getResponse();
		  LSnumber =  response.trim();		  
       	    }
	    
	    }
	    
	    for(int i=0 ;i<nunit;i++) {
		command = "PS "+hvm[i].lu;
		// ask hvframe about Properties Summary Numbers
		if(hvcom.exec(command,host)) {
		// get response
		while(!hvcom.isEmpty()) {
		    PSnumber[i] =  hvcom.getResponse();
		}
		}
	    }
	    
	                 
	                // create ON/OFF buttons panel
			onButton.setMnemonic(KeyEvent.VK_O);
			onButton.setActionCommand("hv_ON");
			//onButton.setForeground(Color.green);
			onButton.setVerticalTextPosition(SwingConstants.TOP);
			onButton.setHorizontalTextPosition(SwingConstants.CENTER);
			onButton.setAlignmentX(Component.CENTER_ALIGNMENT);

			//	    JRadioButton offButton = new JRadioButton("OFF");
			offButton.setMnemonic(KeyEvent.VK_K);
			offButton.setActionCommand("hv_OFF");
			offButton.setVerticalTextPosition(SwingConstants.TOP);
			offButton.setHorizontalTextPosition(SwingConstants.CENTER);
			offButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			//offButton.setIconTextGap(1);
			if(HVONstatus) {
			    onButton.setSelected(true);	
			    hvonic = hvonenic;
			} else {
			    offButton.setSelected(true);
			    hvonic = hvondisic;
			}
			
			onButton.setToolTipText("Click button to turn ON High Voltage");
			offButton.setToolTipText("Click button to turn OFF High Voltage");
			
			ButtonGroup group = new ButtonGroup();
			group.add(onButton);
			group.add(offButton);
			RadioListener myListener = new RadioListener();
			onButton.addActionListener(myListener);
			offButton.addActionListener(myListener);

			//create labels
			panicLabel = new JLabel(hvpanic); 			
			panicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			hvonLabel = new JLabel(hvonic); 
			hvonLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

			Color bg = radioPanel.getBackground();
			remoteLabel = new JLabel("remote"); 
			if(remoteSwitch) remoteLabel.setForeground(Color.green);
			else remoteLabel.setForeground(bg.darker());
									
			eepromLabel = new JLabel("EEPROM"); 
			//eepromLabel.setFont(new Font("Hevletica", Font.PLAIN, 10));
			eepromLabel.setForeground(bg);
			psLabel = new JLabel("24V"); 
			psLabel.setForeground(bg);
			//psLabel.setFont(new Font("Hevletica", Font.PLAIN, 10));
			batteryLabel = new JLabel("Battery"); 
			batteryLabel.setForeground(bg);		
			//batteryLabel.setFont(new Font("Hevletica", Font.PLAIN, 10));
	
			// Put the radio buttons in a column in a panel
			radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
			//radioPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		        //radioPanel.setLayout(new GridLayout(0, 1));
			radioPanel.setBorder(BorderFactory.createCompoundBorder(
		                BorderFactory.createTitledBorder(
		                        "1458"),
		             BorderFactory.createEmptyBorder(5,5,5,5)));
			

			buttonPanel = new JPanel();
			buttonPanel.setBorder(BorderFactory.createCompoundBorder(
		                BorderFactory.createLoweredBevelBorder(),
				BorderFactory.createEmptyBorder(5,5,5,5)));
			

			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
			buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
			//buttonPanel.setBackground(radioPanel.getBackground().darker());
			//hvonLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

			buttonPanel.add(onButton);
			buttonPanel.add(Box.createVerticalStrut(3));
			buttonPanel.add(hvonLabel);
			buttonPanel.add(Box.createVerticalStrut(3));
			buttonPanel.add(offButton);

			statusPanel = new JPanel(new GridLayout(0,1));
			statusPanel.setBorder(BorderFactory.createCompoundBorder(
		                BorderFactory.createTitledBorder(
		                        "status"),
		             BorderFactory.createEmptyBorder(3,3,3,3)));
			statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

			statusPanel.add(remoteLabel);
			statusPanel.add(eepromLabel);
			statusPanel.add(psLabel);
			statusPanel.add(batteryLabel);

			radioPanel.add(panicLabel);
			radioPanel.add(Box.createVerticalStrut(10));
			radioPanel.add(buttonPanel);

			//radioPanel.add(onButton);
			//radioPanel.add(hvonLabel);
			//radioPanel.add(offButton);
			radioPanel.add(Box.createVerticalStrut(10));
			radioPanel.add(statusPanel);


			// create tabbed panel to show modules tables
			Component[] panel = new Component[nunit];
			StringTokenizer s = new StringTokenizer(unames.trim());
			ImageIcon icon = new ImageIcon("./m.gif");
			for (int i=0;i<nunit;i++) {
			    String ss = s.nextToken();
			    panel[i] = makeTabPanel(hvm[i].table);
			    tabb.addTab(ss, icon, panel[i], "Click to select Slot:"+ss);
			}
			
			remove(wpanel);
			add(radioPanel, BorderLayout.WEST);
			setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			add(tabb,BorderLayout.CENTER );
			//	    setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			
			
			startMonitor(monitorDelay);
			
   			//Create Ramping timer.
			ramptimer = new javax.swing.Timer(2*ONE_SECOND, new ActionListener() {
				boolean first = true; 
				boolean blink = false;
				Icon icon1 = null;
				Icon icon2 = null;
				public void actionPerformed(ActionEvent evt) {	
				     if(first) {
					icon1 = hvonLabel.getIcon();
					icon2 = ftabb.getIconAt(ftabb.indexOfComponent(HVframe.this));
					first = false;
				    }
				    if(blink = !blink) {
					hvonLabel.setIcon(hvoninvic);
					ftabb.setIconAt(ftabb.indexOfComponent(HVframe.this), ipwrrmp);
				    } else {
					hvonLabel.setIcon(icon1);
					ftabb.setIconAt(ftabb.indexOfComponent(HVframe.this), icon2);
					if(soundOn) Toolkit.getDefaultToolkit().beep();
				    }
				    
				}
				
			    });

   			//Create Alarm Beepper timer.
			alarmBeepper = new javax.swing.Timer(ONE_SECOND, new ActionListener() {
				boolean first = true; 
				boolean blink = false;
				Icon icon1 = null;
				Icon icon2 = null;
				int cnt = 0;
				int cnt2 = 0;
				Color bgcol = null;
				public void actionPerformed(ActionEvent evt) {	
				    if(first) {
					icon1 = hvonLabel.getIcon();
					icon2 = ftabb.getIconAt(ftabb.indexOfComponent(HVframe.this));
					bgcol = ftabb.getBackgroundAt(ftabb.indexOfComponent(HVframe.this));
					tabbColor = bgcol;
					first = false;
										
					//signalAlarm.play();					    
					//voiceAlarm.play();					    
					cnt2 = 0;
					cnt = 0;
				    }
				    if(blink = !blink) {					
										
					setAlarmedBackground(tabbAlarmColor);
					if(statusPanicOFF) panicLabel.setIcon(hvpanicen);
					ftabb.setIconAt(ftabb.indexOfComponent(HVframe.this), ipwrerr);
					if(soundOn) Toolkit.getDefaultToolkit().beep();
				    } else {
					setAlarmedBackground(bgcol);
					ftabb.setIconAt(ftabb.indexOfComponent(HVframe.this), icon2);
					if(statusPanicOFF) panicLabel.setIcon(hvpanicdis);
								 
					cnt ++;
					cnt2++;
					if(cnt2 == 4) {
					    //  signalAlarm.play();					    
					    cnt2 = 0;
					}

					if(cnt == 20) {
					    //voiceAlarm.play();					    
					    cnt = 0;
					}
				    }
				}
				
			    
			    });
								

			updateLabelStatus();			
			initdone = true;
			ftabb.setCursor(defaultCursor);
			
   } // ----------------- end run()


    /** sets color of remote label
     * @param c - color to set
     */
    public void setRemoteColor(Color c) {
	if(remoteSwitch) 
	    remoteLabel.setForeground(c);
    }

    private void setAlarmedBackground(Color c) {
	ftabb.setBackgroundAt(ftabb.indexOfComponent(HVframe.this), c);
	if(!modulesTrip.isEmpty()) {
	    for(Enumeration e=modulesTrip.elements();e.hasMoreElements() ;) {
		HVmodule m = (HVmodule)e.nextElement() ;
		tabb.setBackgroundAt(m.id, c);
		Vector chv = m.getChannelsTrip();
		for(Enumeration ce = chv.elements(); ce.hasMoreElements() ;) {
		    int ch = ((Integer) ce.nextElement()).intValue() ;
		    m.table.setCellSelected(ch,0,tabbAlarmColor);
		    //   m.setChannelBackgroundAt(ch, 0, c);
		}
	    }
	}
    }


    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with 
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
    void updateInitStatus(final int i) {
        Runnable doSetProgressBarValue = new Runnable() {
            public void run() {
                progressBar.setValue(i);
            }
        };
        SwingUtilities.invokeLater(doSetProgressBarValue);
    }

    


    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with 
     * SwingUtilities.invokeLater().  In this case we're just
     * changing labels color.
     */
    public void updateLabelStatus() {
        Runnable doSetLabel = new Runnable() {
            public void run() {
		Color errColor = Color.red; 
		Color bgColor = radioPanel.getBackground();
		if(remoteSwitch) 
		    remoteLabel.setForeground(Color.green);
		else  
		    remoteLabel.setForeground(bgColor.darker());
 		if(statusPanicOFF) {
		    panicLabel.setIcon(hvpanicen);
		    notifys.updateStatus("ALARM :"+host+":PANICOFF is ACTIVATED!!!");
		    if(panicOffMask) setAlarm();
		} else { 
		    panicLabel.setIcon(hvpanicdis);				    
		    clearAlarm();
		}
		
		if(statusEEPROM) {
		    eepromLabel.setForeground(bgColor);
		    clearAlarm();
		} else {  
		    eepromLabel.setForeground(errColor);		
		    notifys.updateStatus("ALARM :"+host+":ERROR - BAD EEPROM!!!");
		    if(hvErrorMask)  setAlarm();
		}
		
		if(statusBattery) { 
		    batteryLabel.setForeground(bgColor);
		} else {
		    batteryLabel.setForeground(errColor);			    
		    notifys.updateStatus("ALARM :"+host+":ERROR - BAD BATTERY!!!");
		    if(hvErrorMask)  setAlarm();
 		}
		if(status24V) { 
		    psLabel.setForeground(bgColor);
		    clearAlarm();
		} else {  
		    psLabel.setForeground(errColor);
		    notifys.updateStatus("ALARM :"+host+":ERROR - BAD POWER SUPPLY 24V!!!");
		    if(hvErrorMask)  setAlarm();
		}	    
		
		if(alarmHVOFF) { 
		    notifys.updateStatus("ALARM :"+host+" :POWER OFF not from GUI!!!(This state is cleared by cycling HVON/HVOFF status)");
		   if(hvOffMask) setAlarm();
		} else {  
		    clearAlarm();
		}	    
		if(HVONstatus&remoteSwitch) {
		    ftabb.setIconAt(ftabb.indexOfComponent(HVframe.this), ipwron);
		    onButton.setSelected(true);
		    hvonic = hvonenic;
		    notifys.updateStatus(host+":HVON");	
		} else {
		    ftabb.setIconAt(ftabb.indexOfComponent(HVframe.this), ipwroff);
		    offButton.setSelected(true);
		    hvonic = hvondisic;
		    notifys.updateStatus(host+":HVOFF");	
		}
		hvonLabel.setIcon(hvonic);
	    }
        };
        SwingUtilities.invokeLater(doSetLabel);
    }

    public void setHVONstatus(boolean stat) {
	HVONstatus = stat;
	if(stat&remoteSwitch) {
	    ftabb.setIconAt(ftabb.indexOfComponent(this), ipwron);
	    onButton.setSelected(true);
	    hvonic = hvonenic;
	    notifys.updateStatus(host+":HVON");	
	} else {
	    ftabb.setIconAt(ftabb.indexOfComponent(this), ipwroff);
	    offButton.setSelected(true);
	    hvonic = hvondisic;
	    notifys.updateStatus(host+":HVOFF");	
	}
	hvonLabel.setIcon(hvonic);
    }
  
    public String getConfigWord() {
	String ret = null;
	StringTokenizer cnf = new StringTokenizer(Config);
	if(cnf.hasMoreTokens()) {
	    ret = cnf.nextToken();
	}
	return ret;
    }
    

  /** unpack configuration words  and sets corresponded variables
   */
    public void parseConfigWord(String s) {
	if(s!=null) {
	try {
	    StringTokenizer cnf = new StringTokenizer(s);
	    if(cnf.hasMoreTokens()) {
		
		// using only first configuration word
		long hexw1 = Long.parseLong(cnf.nextToken(),16);
		if(((hexw1>>2)&1)==1) remoteSwitch = true;
		else remoteSwitch = false;
		if(((hexw1>>4)&1)==1) statusEEPROM = true;
		else statusEEPROM = false;
		if(((hexw1>>5)&1)==1) statusBattery = true;
		else statusBattery = false;
		if(((hexw1>>6)&1)==1) status24V = true;
		else status24V = false;
		if(((hexw1>>13)&3)==1) {
		    HVONstatus = true;
		} else {
		    HVONstatus = false;
		    // check if HVOFF not from GUI, and set alarm
		    if(statusHVOFF == HVONstatus) { 
			alarmHVOFF = true;
		    }

		}
		//statusHVOFF = !HVONstatus;

		if(((hexw1>>15)&1)==1) statusPanicOFF = true;
		else statusPanicOFF = false;
		
		//      System.out.println("Config:"+s+" Long="+hexw1);
		//System.out.println("remote: "+remoteSwitch);
		//System.out.println("HVstatus: "+HVONstatus);
	    }
	    if(cnf.hasMoreTokens()) { 
		String word2 = cnf.nextToken(); // external serial settings
	    }
	    if(cnf.hasMoreTokens()) {
		String word3 = cnf.nextToken(); // arcnet settings
	    }
	    if(cnf.hasMoreTokens()) { 
		String word4 = cnf.nextToken(); // software limit summary number
	    }
	    if(cnf.hasMoreTokens()) {
	        sysdef = cnf.nextToken(); // system default word
		//System.out.println("SYSDEF:"+sysdef);
	    }   	    
	} catch (NumberFormatException en) {
	} catch (NullPointerException enp) {
	}       
	}
    
    }
    
    //Can be invoked from any thread.
    public synchronized void startRampAnimation() {
	//Start animating!
	if (!ramptimer.isRunning()) {
	    ramptimer.start();
	}
    }
    
    //Can be invoked from any thread.
    public synchronized void stopRampAnimation() {
        //Stop the animating thread.
        if (ramptimer.isRunning()) {
            ramptimer.stop();
	    updateLabelStatus();
        }
    }
    
    //Can be invoked from any thread.
    public synchronized void startAlarmBeepper() {
	//Start beeppering!
	if (!alarmBeepper.isRunning()) {
	    alarmBeepper.start();
	    
	    alButton = new AlarmButton("Alarm Silence of: \n"+host, this);
	}
    }
    
    //Can be invoked from any thread.
    public synchronized void stopAlarmBeepper() {
        //Stop the alarming thread.
        if (alarmBeepper.isRunning()&&!isAlarmSet()) {
            alarmBeepper.stop();
	    //	   if (voiceAlarm!=null) voiceAlarm.stop();
	    //if (signalAlarm!=null) signalAlarm.stop();
	    Color c = null;
	    if(alButton!=null) {
		if(alButton.isVisible()) {
		    alButton.stopAnimation();
		    alButton.setVisible(false);
		    alButton = null;
		}
	    }
	    ftabb.setBackgroundAt(ftabb.indexOfComponent(HVframe.this), c);
	    updateLabelStatus();
	    //	    soundOn = true;
	}
    }

    /** Listens to the radio buttons. */
     class RadioListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("hv_ON")) {
		//System.out.println("OnbuttonStatus:"+onButton.isSelected());
		if(!HVONstatus) {
		    if(soundOn) Toolkit.getDefaultToolkit().beep();
		    command = HVON;
		    if(hvcom.exec(command,host)) {
		    // get response
		    while(!hvcom.isEmpty()) {
			String response =  hvcom.getResponse();
		    }			
		    }
		    statusHVOFF = false;
		    alarmHVOFF = false;
		    //    HVONstatus = true;
		    //ftabb.setIconAt(ftabb.getSelectedIndex(), ipwrup);
		    // notifys.updateStatus(host+":HVON");	
		}
	    }
      
	    if (e.getActionCommand().equals("hv_OFF")) {	
		//System.out.println("OffbuttonStatus:"+offButton.isSelected());
		if(HVONstatus) {
		    if(soundOn) Toolkit.getDefaultToolkit().beep();
		    command = HVOFF;
		    if(hvcom.exec(command,host)) {
		    // get response
		    while(!hvcom.isEmpty()) {
			String response =  hvcom.getResponse();
		    }
		    }
		    statusHVOFF = true;
		    alarmHVOFF = false;
		    //HVONstatus = false; 
		    //ftabb.setIconAt(ftabb.getSelectedIndex(), ipwrdown);
		    //notifys.updateStatus(host+":HVOFF");	
		}
	    }
	}
     }

    protected Component makeTabPanel(HVmoduleTable table) {
        JPanel panel = new JPanel(false);
        //filler.setHorizontalAlignment(JLabel.CENTER);
	panel.setLayout(new GridLayout(1, 1));
        panel.add(table);
	return panel;
    }
    
    
    public String getStatus() {
	return status ;
    }

    public boolean getErrStatus() {
	return errStatus ;
    }

    // get/set Summary numbers methods
    public synchronized String getGSnumber() {
	return GSnumber;
    }
    public synchronized String getLSnumber() {
	return LSnumber;
    }
    public synchronized String getPSnumber(int ind) {
	return PSnumber[ind];
    }
    public synchronized void setGSnumber(String gs) {
	GSnumber = gs;
    }
    public synchronized void setLSnumber(String ls) {
	LSnumber = ls;
    }
    public synchronized void setPSnumber(String ps, int ind) {
	PSnumber[ind] = ps;
    }

    public void EnableAllChannels(String stat) {	
	for(int i=0 ;i<nunit;i++) {
	    hvm[i].EnableAllChannels(stat);
	} 
    }

    /* Returns the slot number of the module by its logical number
     * @param index - int logical number of the module
     * @return String slot number of the module
     */
    public String getModuleSlot(int index) {
	// 3-Sept-2005 change type of return from int to String, to support submodule addressing
	String is = null;
	//try {
	    if((index>=0)&&(index<nunit)) {
		String s = (String) ltos.elementAt(index); 
		is =  s.substring(1,s.length()); //remove "S" from beginning of slot number
		
		//is = Integer.parseInt(s);
	    }
	    //} catch (NumberFormatException ne) {
	    //System.err.println("getModuleSlot: "+ne);
	    // return is;
	    //}
	
	return is;
    }
    
    /* Returns the index of the module(logical unit number) installed in slot number 'slot'
     * @param slotnum - String slot number of the module
     * 
     */
    public int getModuleIndex(String slotnum) {
	// 3-Sept-2005 change type of parameter from int to String, to support submodule addressing	
	int cnt = 0;
        int ifind=-1; // Jul,16 2001 correction (E.Ch.) 
	String slot = "S"+slotnum;
	StringTokenizer st = new StringTokenizer(unames.trim());
	// check if slotnumber presents in unames
	int ind = unames.indexOf(slot);
	if(ind>=0) {
	    // calculate index of slotnumber in unames
	    while(st.hasMoreTokens()) {
		String sl = st.nextToken();
                // System.out.println(" getModuleIndex: "+slotnum+" "+ind+" "+sl+" "+slot);
		if(sl.equalsIgnoreCase(slot)) { 
		    ifind=cnt;   //Jul,16 2001 correction (E.Ch.) 
		    break;
		}
		cnt++;
	    }  
	} else { 
	    ifind = ind;  //Jul,16 2001 correction (E.Ch.) 
	}
	// System.out.println(" getModuleIndex: "+ifind);
 	return ifind;     //Jul,16 2001 correction (E.Ch.) 
    }


    public void save(FileWriter out) {
	String comment = new String("# "+ new Date()+" HVFrame-"+id+"("+host+")"+" demand voltage set(DV) \n");
	String sp = new String();
	String bl = new String(" ");
	String prop = new String("DV");
	StringTokenizer st = new StringTokenizer(unames.trim());
	int m = 0;
	// save comment
	    try {
		out.write(comment);
	    } catch (IOException ef) {
	    	System.err.println("HVframe.save(): " + ef);
	    }
	// save data

	while(st.hasMoreTokens()) {
	    String slot = st.nextToken();
	    if(hvm[m].hasParameter(prop)) {
		//System.err.println("HVModuleParam: " + m+":"+hvm[m].getParameters());   
		String sdv = new String();
		for(int i=0 ;i<hvm[m].nchn;i++) {
		    
		    sdv=sdv+hvm[m].ch[i].getValue(prop)+bl;
		}
		
		//	    sp = "F"+id+bl+st.nextToken()+bl+prop+bl+sdv+"\n" ;
		sp = name +":" +port +bl +slot +bl +prop +bl +sdv +"\n" ;
		//	    System.out.print(sp);
		try {
		    out.write(sp);
		} catch (IOException ef) {
		    System.err.println("HVframe.save(): " + ef);
		}
	      }
	    m=m+1;
	}
	
    }

    
    public Vector setEditableProperties(int ind) {
	int np = hvm[ind].numparam;
	Vector v = new Vector();
	for(int i=0; i< np ;i++) {
	    String b = hvm[ind].pattr[i].getAttr("protection");
	    //System.out.println("Prot:"+b);
	    if (!b.equalsIgnoreCase("M")) {
		v.addElement((String)hvm[ind].pattr[i].name);
		//   System.out.println("EditProp:"+m.pattr[i].name);
	    }
	}
	return v;
    }

    public void saveAll(FileWriter out) {

	String comment = new String("# HVFrame-"+id+"("+host+")"+" parameters set \n");
	String sp = new String();
	String bl = new String(" ");
	StringTokenizer st = new StringTokenizer(unames.trim());
	int m = 0;
	// save comment
	    try {
		out.write(comment);
	    } catch (IOException ef) {
	    	System.err.println("HVframe.save(): " + ef);
	    }
	// save data
	while(st.hasMoreTokens()) {
	    String slot = st.nextToken();
	    Vector editableProperties = setEditableProperties(m);

	    for (int p=0; p<hvm[m].numparam; p++) {
		String sdv = new String();
		String prop = new String(hvm[m].getParameterName(p));
		for(int i=0 ;i<hvm[m].nchn;i++) {
		    sdv=sdv+hvm[m].ch[i].getValue(prop)+bl;
		}
		//    System.out.print("prop:"+prop+" values:"+sdv);
		if (!editableProperties.contains(prop)) {
		    sp ="# "+ name +":"+port+bl+slot+bl+prop+bl+sdv+"\n" ;
		} else {
		    sp = name +":"+port+bl+slot+bl+prop+bl+sdv+"\n" ;
		}
		try {
		    out.write(sp);
		} catch (IOException ef) {
		    System.err.println("HVframe.save(): " + ef);
		}
	    } // end properties loop
	    m=m+1;
	} //end modules loop
	
    }

    public void load(String com) {	
	String command = new String("LD "+com);
	//	System.out.println(command);
	// suspend monitor while update modules properties
	hvmon.suspendMonitor(host);

	if (true) {
	    if(hvcom.exec(command,host)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		String response =  hvcom.getResponse();
	    }		
	}
	}
	// resume monitor
	hvmon.resumeMonitor(host);
    }

 
   public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
     return hvm[0].print(g, pf, pi);
   }

   //stop client and monitor
   public void remove() {
  	if (hvmon.isAlive()) hvmon.stopMonitor();
  	if (hvcli.isAlive()) hvcli.stopClient();
    }

    public boolean isAlive() {
	return frameThread.isAlive();
	//return !initdone;
    }


    /** Checks status of hvclient. If any errors are arrised,
     *	then disables tabb with current mainframe table and start alarm beepper. 
     *  @return - true if client is alive and false if any errors
     */ 
    public boolean checkClientStatus() {
	int indx=ftabb.indexOfComponent(this);
	if(hvcli.getErrStatus()) {	    
		
	    if(isEnabled()) {
		ftabb.setIconAt(indx, ipwrerr);
		msetEnabled( false);

		ftabb.getComponentAt(indx).setCursor(waitCursor);
		stopRampAnimation();
		if(noConnectionMask) setAlarm();
		System.out.println("Disable mainframe:"+host );
	    }
	    return false;
	} else {
	    if(!isEnabled()) {
	       setHVONstatus(HVONstatus);
	       if(!hvcli.isAlive()) {
		   hvcli.frozen = true;
		   hvcli.start();
	       }
	       
	       clearAlarm();
	       msetEnabled(true);
	       ftabb.getComponentAt(indx).setCursor(defaultCursor);
	       System.out.println("Enable mainframe"+host );
	    }
	    return true;
	}
    }

    public void msetEnabled(boolean en) {

	onButton.setEnabled(en);
	offButton.setEnabled(en);
	for (int i=0;i<nunit;i++) {
	    hvm[i].table.hvtable.setEnabled(en);
	    hvm[i].table.setEnabled(en);
	}
	super.setEnabled(en);
    }

    // Alarm process methods
    public boolean isAlarmSet() {
	return alarmSet||moduleAlarmSet;
    }
    
    public boolean isModuleAlarmSet() {
	return moduleAlarmSet;
    }

    public void setAlarm() {
	alarmSet = !status24V || !statusBattery || !statusEEPROM || statusPanicOFF || !mess.isConnected() || alarmHVOFF;
	startAlarmBeepper();	
    }

    public void clearAlarm() {
	alarmSet = !status24V || !statusBattery || !statusEEPROM || statusPanicOFF || !mess.isConnected() || alarmHVOFF ;
	stopAlarmBeepper();	
    }
     
    public void setModuleAlarm(int mod) {
	moduleAlarmSet = true;
	if(!modulesTrip.contains(hvm[mod])) {
	    modulesTrip.add(hvm[mod]);
	}
	startAlarmBeepper();
	
    }
    
    public void clearModuleAlarm(int mod) {
	
	if(modulesTrip.contains(hvm[mod])) {
	    modulesTrip.remove(hvm[mod]);
	    Color c = null;
	    tabb.setBackgroundAt(mod, c);
	    hvm[mod].table.restoreSelectionBackground();
	}    
	if(modulesTrip.isEmpty()) {
	    moduleAlarmSet = false;
	    stopAlarmBeepper();
	}
	
    }
    
    public void setTabbAlarmBackground(int ind) {
	tabb.setBackground(Color.red);
    }



    /** Sets mask for alarms (enable/disable)
     *  @param mask - int mask with masking bits :
     *   (if bit set - corresponded alarm is enabled)
     *   0 - sound, 1 - hvchannel trip, 2 - hvframe errors, 3 - panic OFF, 
     *   4 - lost connection, 5 - HVOFF not from GUI  
     */
    public void setAlarmMask(int mask) {
	if( (mask & 1) == 1) soundOn = true;
	else soundOn = false;
	if( ((mask>>1) & 1) == 1) chTripMask = true;
	else chTripMask = false;
	if( ((mask>>2) & 1) == 1) hvErrorMask = true;
	else hvErrorMask = false;
	if( ((mask>>3) & 1) == 1) panicOffMask = true;
	else panicOffMask = false;
	if( ((mask>>4) & 1) == 1) noConnectionMask = true;
	else noConnectionMask = false;
 	if( ((mask>>5) & 1) == 1) hvOffMask = true;
	else hvOffMask = false;
	
	System.out.println("alarmmask:"+mask);
   }

    /** Close clients sockets and stops all threads
     *
     */
    public void close(){
	
	try {
	    if(this.isAlive()) frameThread.stop();
	    // wait for thread stop
	    while(this.isAlive()) {
		try {
		    Thread.sleep(100) ;
		    
		} catch (InterruptedException ie) { 
		} 
	    }
	    this.hvcli.close();
	    if (hvmon.isAlive()) hvmon.stopMonitor();
	} catch (NullPointerException ne) {
	    ; 	
	} catch (IOException e) {
	    System.err.println("Couldn't close I/O for the connection:" +host);
	    //System.exit(1);
	}
    }


}



