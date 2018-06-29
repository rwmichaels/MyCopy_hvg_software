
package hvframe;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;
import javax.swing.JTabbedPane;
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
 *  @version 1.1
 *  Last update: 17-May-01
 */
public class HVframe  extends JPanel  implements Cmd, Printable, Runnable {
    public int id = 0; 
    public String name = null; // name of HVframe(realy hostname)
    public int    port = 0; // port of HVframe
    public String sysdef = null;
    public Vector sysinfo = new Vector(); // store sysinfo from HVframe
    public String pupstatus = null; 
    public String config = null; 
    public int    nunit = 0; // number of modules installed in HVframe
    public String unames = null; //names of modules(slot number like: SN)
    public final int MAXUNIT = 16; // max number of units per HVframe
    private String command = new String(); // store command string
    public String[] moduleInfo = null; // store modules info(returned ID comand)
    private HVMessage mess = null;
    public HVCommand hvcom = null; 
    public HVmodule[] hvm = null; // array of HVmodules objects 
    public HVclient hvcli = null; // client object
    public HVmonitor hvmon = null; // monitor object
    public HVListener notifys;  // interface to update Status window
    ImageIcon ipwron  = new ImageIcon("./images/pwron.gif"); //power on icon
    ImageIcon ipwroff = new ImageIcon("./images/pwroff.gif"); // power off icon
    ImageIcon ipwrup  = new ImageIcon("./images/pwrup.gif"); // power off icon
    ImageIcon ipwrdown = new ImageIcon("./images/pwrdown.gif"); // power off icon
    ImageIcon ipwrerr = new ImageIcon("./images/pwrerr.gif"); // power error icon
    public JTabbedPane ftabb = null;
    public JTabbedPane tabb = new JTabbedPane(); // jtabbed pannel to represent 
                                          // of HVmodules
    JRadioButton onButton = new JRadioButton("ON"); // HV ON button

    JRadioButton offButton = new JRadioButton("OFF"); // HV OFF button

    public boolean HVONstatus = false; // cuurent status of buttons
    private String status = new String("OK");
    private boolean errStatus = false;
    private String GSnumber = null;
    private String LSnumber = null;
    private String[] PSnumber = new String[MAXUNIT];
    private String response = null;
    private Thread frameThread = null;
    private boolean initdone = false;
    JPanel wpanel = new JPanel();
    JProgressBar progressBar= null;
    SwingWorker worker;
    JLabel label = null;
    ImageIcon initicon = new ImageIcon("./init.gif");

    // constructors
    public HVframe(String server,int hvport, JTabbedPane ftabb, 
		HVListener notifys ) {
	super(false);
	this.name = server;
	this.port = hvport;
	this.id = id;
	this.mess = new HVMessage();
	this.notifys = notifys;
	this.ftabb = ftabb;
	frameThread = new Thread(this, name);

	label = new JLabel(name+" :INITILIZATION IN PROGRESS...",initicon,SwingConstants.CENTER);

	// start hvclient thread	
	this.hvcli = new HVclient(server, hvport, mess, notifys );
	
	if(!hvcli.getErrStatus()) {
	    hvcli.frozen=true;
	    hvcli.start();
	    status = "HV client started -> "+server;	    


	    this.setLayout(new BorderLayout()); 

	    wpanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	    //	    wpanel.setBackground(new Color(200,200,255));
	    wpanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	    wpanel.add(label,BorderLayout.CENTER);
	    wpanel.setPreferredSize(new Dimension(700,400));
	    this.add(wpanel,BorderLayout.CENTER);



	    // create command obj
	    this.hvcom = new HVCommand(mess, notifys);

	    command = SYSINFO;
	    sysinfo.clear();
	    // ask hvframe about system information
	    hvcom.exec(command,name);
	    // get response
	    while(!hvcom.isEmpty()) {
		sysinfo.add((String) hvcom.getResponse());
	    }

	    command = HVSTATUS;
	    // ask hvframe about current status
	    hvcom.exec(command,name);
	    // get response
	    String st = new String("HV?");
	    while(!hvcom.isEmpty()) {
		       st = hvcom.getResponse();
	    }
	    st=st.trim();
	    notifys.updateStatus(name+":"+st);
	    if(st.equalsIgnoreCase("HVON")) {
		HVONstatus = true;
	    }

	    command = LL;
	    // ask hvframe about installed modules(units)
	    hvcom.exec(command,name);
	    // get response
	    while(!hvcom.isEmpty()) {
		unames =  hvcom.getResponse();
		//		System.out.println("UNAMES:"+unames);
	    }
	    nunit= new StringTokenizer(unames.trim()).countTokens();
	    //	    System.out.println("NUNIT: "+nunit);

	    progressBar = new JProgressBar(0, nunit);
	    this.add(progressBar,BorderLayout.CENTER);


            /* Invoking start() on the SwingWorker causes a new Thread
             * to be created that will call construct(), and then
             * finished().  Note that finished() is called even if
             * the worker is interrupted because we catch the
             * InterruptedException in doWork().
             */
            worker = new SwingWorker() {
                public Object construct() {
                    return doInit();
                }
                public void finished() {
		    if(initdone) notifys.updateStatus(name+":All done");
                }
            };
            worker.start();

	    //start initilization thread
		     //	    frameThread.start();

	} else {
	    status = hvcli.getStatus();
	    errStatus = hvcli.getErrStatus(); 
	    notifys.updateStatus(status);
 	}
   }
    
    Object doInit() {

	    // create hvmodules that installed in frame 
	    this.hvm = new HVmodule[nunit];
	    moduleInfo = new String[nunit];

	    // ask about modules info
	    for(int i=0 ;i<nunit;i++) {		
		command = ID +" "+"L"+i;
		// ask hvframe about  modules info
		hvcom.exec(command,name);
		// get response
		while(!hvcom.isEmpty()) {
		    moduleInfo[i]  = new String(hvcom.getResponse());
		}		
	    }

	    // init modules
	    for(int i=0 ;i<nunit;i++) {
		if (i==0) hvm[i] = new HVmodule(mess,name,i,moduleInfo[i],notifys);
		if (i>0) {
		    //if  next module has the same model as previouse, then take parameters and attributes for 
		    // initialization of next module from previouse module. 
		    if( hvm[i-1].getModel().equalsIgnoreCase(hvm[i-1].getword(moduleInfo[i],0)) ) {
			//init next module with parameters of previouse module
			 hvm[i] = new HVmodule(mess,name,i,moduleInfo[i],hvm[i-1].getParameters(),hvm[i-1].getAttributes(),notifys);
		    } else {
			// use default initialization (ask module about parameters and attributes)
			hvm[i] = new HVmodule(mess,name,i,moduleInfo[i],notifys);
			updateStatus(i);
		    }
		}
	    }
	    // get summary numbers and store its as Strings
	    command = GS;
	    // ask hvframe about Global Summary Numbers
	    hvcom.exec(command,name);
	    // get response
	    while(!hvcom.isEmpty()) {
		response =  hvcom.getResponse();
	    }
	        GSnumber = response.trim();

	    command = LS;
	    // ask hvframe about Logical Units Summary Numbers
	    hvcom.exec(command,name);
	    // get response
	    response =  hvcom.getResponse();
	    LSnumber =  response.trim();
	    while(!hvcom.isEmpty()) {
		  response  = LSnumber +" "+ hvcom.getResponse();
		  LSnumber =  response.trim();		  
       	    }

	    for(int i=0 ;i<nunit;i++) {
		command = "PS "+hvm[i].lu;
		// ask hvframe about Properties Summary Numbers
		hvcom.exec(command,name);
		// get response
		while(!hvcom.isEmpty()) {
		    PSnumber[i] =  hvcom.getResponse();
		}
	    }

	    // create ON/OFF buttons panel
	    onButton.setMnemonic(KeyEvent.VK_O);
	    onButton.setActionCommand("hv_ON");
	    //	    onButton.setBackground(Color.green);
	    onButton.setVerticalTextPosition(SwingConstants.TOP);
	    onButton.setHorizontalTextPosition(SwingConstants.CENTER);

	    //	    JRadioButton offButton = new JRadioButton("OFF");
	    offButton.setMnemonic(KeyEvent.VK_K);
	    offButton.setActionCommand("hv_OFF");
	    offButton.setVerticalTextPosition(SwingConstants.TOP);
	    offButton.setHorizontalTextPosition(SwingConstants.CENTER);
	    if(HVONstatus) {
		onButton.setSelected(true);	
	    } else {
		offButton.setSelected(true);
	    }
		
	    onButton.setToolTipText("Click this button to turn ON High Voltage");
	    offButton.setToolTipText("Click this button to turn OFF High Voltage");

	    ButtonGroup group = new ButtonGroup();
	    group.add(onButton);
	    group.add(offButton);
	    RadioListener myListener = new RadioListener();
	    onButton.addActionListener(myListener);
	    offButton.addActionListener(myListener);

	    // Put the radio buttons in a column in a panel
	    JPanel radioPanel = new JPanel();
	    radioPanel.setLayout(new GridLayout(0, 1));
	    radioPanel.add(onButton);
	    radioPanel.add(offButton);

	    // create tabbed panel to show modules tables
	    Component[] panel = new Component[nunit];
	    StringTokenizer s = new StringTokenizer(unames.trim());
		ImageIcon icon = new ImageIcon("./m.gif");
	    for (int i=0;i<nunit;i++) {
		String ss = s.nextToken();
		panel[i] = makeTabPanel(hvm[i].table);
		tabb.addTab(ss, icon, panel[i], "Click to select Slot:"+ss);
	    }

	    this.remove(wpanel);
	    this.add(radioPanel, BorderLayout.WEST);
	    setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	    this.add(tabb,BorderLayout.CENTER );
	    //	    setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
	
	// start monitors
	    int delay = MONITOR_DELAY; // delay in msec of monitors scan
	    hvmon = new HVmonitor(Integer.toString(id), mess, delay, this, notifys);
	    hvmon.start();
	    notifys.updateStatus("HV monitor started -> "+name);
 
	    initdone = true;
	    return initdone;
   } // ----------------- end doInit()


    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with 
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
    void updateStatus(final int i) {
        Runnable doSetProgressBarValue = new Runnable() {
            public void run() {
                progressBar.setValue(i);
            }
        };
        SwingUtilities.invokeLater(doSetProgressBarValue);
    }

    public void setHVONstatus(boolean stat) {
	HVONstatus = stat;
	if(stat) {
	    ftabb.setIconAt(ftabb.indexOfComponent(this), ipwron);
	    notifys.updateStatus(name+":HVON");	
	} else {
	    ftabb.setIconAt(ftabb.indexOfComponent(this), ipwroff);
	    notifys.updateStatus(name+":HVOFF");	
	}
    }

    /** Listens to the radio buttons. */
     class RadioListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("hv_ON")) {
		//System.out.println("OnbuttonStatus:"+onButton.isSelected());
		if(!HVONstatus) {
		    command = HVON;
		    hvcom.exec(command,name);
		    // get response
		    while(!hvcom.isEmpty()) {
			String response =  hvcom.getResponse();
		    }			
		    //    HVONstatus = true;
		    ftabb.setIconAt(ftabb.getSelectedIndex(), ipwrup);
		    // notifys.updateStatus(name+":HVON");	
		}
	    }
      
	    if (e.getActionCommand().equals("hv_OFF")) {	
		//System.out.println("OffbuttonStatus:"+offButton.isSelected());
		if(HVONstatus) {
		    command = HVOFF;
		    hvcom.exec(command,name);
		    // get response
		    while(!hvcom.isEmpty()) {
			String response =  hvcom.getResponse();
		    }
		    //HVONstatus = false; 
		    ftabb.setIconAt(ftabb.getSelectedIndex(), ipwrdown);
		    //notifys.updateStatus(name+":HVOFF");	
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


    /* Returns the index of the module(logical unit number) installed in slot number 'slot'
     * @param slotnum - slot number of the module
     * 
     */
    public int getModuleIndex(int slotnum) {
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
	String comment = new String("# HVFrame-"+id+"("+name+")"+" demand voltage set(DV) \n");
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
	    String sdv = new String();
	    for(int i=0 ;i<hvm[m].nchn;i++) {
		sdv=sdv+hvm[m].ch[i].getValue(prop)+bl;
	    }

//	    sp = "F"+id+bl+st.nextToken()+bl+prop+bl+sdv+"\n" ;
	    sp = name +":" +port +bl +st.nextToken() +bl +prop +bl +sdv +"\n" ;
	    //	    System.out.print(sp);
	    try {
	    	out.write(sp);
	    } catch (IOException ef) {
	    	System.err.println("HVframe.save(): " + ef);
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

	String comment = new String("# HVFrame-"+id+"("+name+")"+" parameters set \n");
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
	hvmon.suspendMonitor();
	if (true) {
	    hvcom.exec(command,name);
	    // get response
	    while(!hvcom.isEmpty()) {
		String response =  hvcom.getResponse();
	    }		
	}
	// resume monitor
	hvmon.resumeMonitor();
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
    }

    public void checkClientStatus() {
	if(hvcli.getErrStatus()) {	    
	    if(ftabb.isEnabledAt(ftabb.indexOfComponent(this))) {
		ftabb.setIconAt(ftabb.indexOfComponent(this), ipwrerr);
		ftabb.setEnabledAt(ftabb.indexOfComponent(this), false);
	    }
	} else {
	    if(!ftabb.isEnabledAt(ftabb.indexOfComponent(this))) {
	       setHVONstatus(HVONstatus);
	       ftabb.setEnabledAt(ftabb.indexOfComponent(this), true);
	    }
	}
    }

}



