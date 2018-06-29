//  Last update: 24-Apr-2014
//
//  Modd&updates: 02-Oct-01: in initHVF():
//                           add&remove mainframes to/from HVFrameList and
//                           hvft moveout to outside of cycles 'for'. 
//                           Change HVStatus constructor parameters( add name
//                           of log file).
//  05-Oct-01: in addFileMenu(): call to load voltage using SwingWoker thread. 
//  22-Nov-01: in init(): add hostNames vector from FrameList as parameter for HVServerProtocol. 
//  03-Dec-01: in initHVF(): add sorting tabbs by hostname order (using method insertTab() anstead addTab())
// 04-Jan-02: fixed bug in JDK1.3(Bug Id 4284488) with JFileChoser 
//        JFileChooser.showOpenDialog() returns wrong value if 
//        dialog is closed by pressing X in window conner
// 12-Jan-02: add menu for operation with database (store and load)
// 04-Feb-02: add menu for operation with alarm (masking)
// 27-Feb-02: change hvframe initilization for two protocols (tcp, telnet) 
// 27-May-05: optimization by time during initialization of frames using telnet protocol 
// 22-Jun-05: in'quit()' function add closing all frames; 
//            set width of frame to screen width;
//            in status messages change 'host' to 'name'='host:port'
// 29-Jul-05: add new menus for Map creation 
// 03-Sep-05: add to loadMap() submodule addressing in form S0,S1,S2,... 
//            So channel address in hvmap table presents as: hostname:portnum.moduleslot.channelnum,
//            where 'moduleslot' may be present in two forms: one for module without submodules: 
//                                                          two digits - slot number,
//            and another for module with submodules: slot number + submodulename in form "S0","S1",..
// 16-Oct-05: add to 'update()' call to 'initMap()' after hvframe Edit/Add
//            add filter for file chooser in 'Load Settings' menu
//            add to File menu: :Save Selected HVFrame Settings"
//            moved submenu 'Edit->Add/Remove Frame List' to 'Tools' menu.
//            add to Edit menu: new submenu : Enable Channels-> "All Channels", "Selected Frame Channels","Selected Module Channels" 
//            add to Edit menu: new submenu : Disable Channels-> "All Channels", "Selected Frame Channels","Selected Module Channels" 
// 26-Oct-05: add parameter HVStatus in constructor HVServer()
// 25-Jan-06: Many little changes in the files and directory names. The new structure:
//               HV_XX/
//                  HVframes.conf
//                  HVmaps.conf
//                  hv_set/
//                    XX_all_001.set
//                    XX_dv_001.set
//                    .....
//                  hv_maps/
//                  hv_log
// 13-Dec-2011: replaced SwingWorker to hvtools.SwingWorker due to new java version has this class as standard.
// 24-Apr-2014: add settings of server port 'serverPort' from configuration file (configFile="HVframes.conf")
//              add check of server status in function 'startServer()'.
//              change syntax  definition of Hashtable and Vector for new version of Java.
//

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Container;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Font;
import javax.swing.*;
import javax.swing.JSplitPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.filechooser.*;
import javax.swing.border.*;


import java.io.*;
import java.util.*;
import java.lang.*;
import java.sql.*;

import hvframe.*;
import hvtools.*;
import hvserver.*;
import hvmap.*;

@SuppressWarnings("serial") //this class doesn't implement Serializable interface
/**
 *  class HVmainMenu - implements window to control High Voltage System with
 *                     menu and tables 
 *  @version 2.1 24-Apr-2014
 */ 
public class HVmainMenu extends JFrame implements ActionListener, ItemListener {
    private Runtime rt = null;
    public JPanel[] pan;  
    protected JMenuBar menuBar;
    JMenu menu, submenu;   
    JMenuItem menuItem;
    JCheckBoxMenuItem  cbmenuItem;
    JTabbedPane mtabb;
    ImageIcon ipwron = null;
    ImageIcon ipwroff = null;
    ImageIcon ipwrerr = null;
    ImageIcon ipwrrmp = null;
    ImageIcon pwricon = null;
    JSplitPane splitPane;
    public HVStatus statusArea;
    final static int INIT = 1;
    final static int RUN = 2;
    final static int UPDATE = 3;
    final static int EXIT = 0;
    final static int ERROR = -1;

    int progstate = INIT;
    static private final String newline = "\n";
    private String status = new String("OK");
    private int monitordelay = 3; // monitor delay in sec
    private boolean errStatus = false;
    private String MapFileKey = "map.file";
    private String VMapFileKey = "vmap.file";
    private String ProgDirKey = "prog.dir";
    private String TaskDirKey = "task.dir";
    private String DataDirKey = "data.dir";
    private String MapDirKey = "map.dir";
    private String LogDirKey = "log.dir";

    private String HelpFile = "HVhelp.html";
    private String AboutFile = "HVabout.html";
    private String LogFileName = new String("hvstatus.log");
    public HVListDialog listDialog ; // hvframe configuration dialog
    public HVMapDialog mapDialog ; // map configuration dialog

    public HVFrameList frameList;// frame list with parameters
    public HVMapList mapList ; // map list class with parameters

    public int numFrames = 0;
    public HVMessage mess = new HVMessage();
    public HVframe hvf ; 
    public Hashtable<String, HVframe> hvft = new Hashtable<String, HVframe>(); // frames table

    //* public Hashtable hvft = new Hashtable(); // frames table
    public Hashtable<String, HVMapTable> hvmt = new Hashtable<String, HVMapTable>(); //maps table
    //* public Hashtable hvmt = new Hashtable(); //maps table
    public Vector<String> hvmap = new Vector<String>(); // calorimeter map    
    //*    public Vector vmap = new Vector();    // veto map
    private boolean isMapLoaded = false;
    private boolean isVMapLoaded = false;
    private HVMapTable mapTable = null;
    private HVMapMonitor mapMonitor = null;
    public int serverPort = 5555; // 24-Apr-2014 default server port
    final String  configFile="HVframes.conf"; // 24-Apr-2014 configuration file name
 
    String urldb = null;
    String driverdb = null;
    String user = null;
    String password = null;
    String tabledb = null;

    private int alarmMask = 0;
    
    private long time_start;
    private long time_init;
 
    
    // constructor 
    public HVmainMenu(String title) {
	super(title);
	rt = Runtime.getRuntime();
	time_start = System.currentTimeMillis();
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        //Build the File  menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription(
                "The File only menu in this program that has menu items");
        menuBar.add(menu);
        //Create a file chooser
	addFileMenu(menu);

        //Build the Edit menu.
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription(
                "The Edit only menu in this program that has menu items");
        menuBar.add(menu);
	addEditMenu(menu);

        //Build the View menu.
        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription(
                "The View only menu in this program that has menu items");

	// disable this menu while initHVFrame
	menu.setEnabled(false);
 
	menuBar.add(menu);
	addViewMenu(menu);

       //Build the Map menu.
        menu = new JMenu("Map");
 	menu.setEnabled(false);
	menu.setMnemonic(KeyEvent.VK_M);
        menu.getAccessibleContext().setAccessibleDescription(
                "The Map only menu in this program that has menu items");
        menuBar.add(menu);
	//	addMapMenu(menu);

       //Build the DataBase menu.
	//        menu = new JMenu("DataBase");
	// menu.setMnemonic(KeyEvent.VK_D);
	// menu.getAccessibleContext().setAccessibleDescription(
	//            "The DataBase only menu in this program that has menu items");
	// menuBar.add(menu);
	//addDBMenu(menu);

       //Build the Alarm menu.
        menu = new JMenu("Alarm");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "The Alarm only menu in this program that has menu items");
        menuBar.add(menu);
	addAlarmMenu(menu);

       //Build the Tools menu.
        menu = new JMenu("Tools");
	menu.setEnabled(false);
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription(
                "The Tools only menu in this program that has menu items");
        menuBar.add(menu);
	addToolsMenu(menu);


        menuBar.add(Box.createHorizontalGlue());
        addHelpMenu("Help");		

	// create status window
        String logDir = System.getProperty(LogDirKey);
        String filesep = System.getProperty("file.separator");
        String LogFileNameFull = new String(logDir + filesep + "hvstatus.log");
 
	statusArea = new HVStatus("Begin initialization...", 200, LogFileNameFull);
	JScrollPane areaScrollPane = new JScrollPane(statusArea);
	areaScrollPane.setVerticalScrollBarPolicy(
		       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	areaScrollPane.setPreferredSize(new Dimension(1000, 100));
	areaScrollPane.setBorder(
	    BorderFactory.createCompoundBorder(
		  BorderFactory.createCompoundBorder(
			        BorderFactory.createTitledBorder("Status"),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
                areaScrollPane.getBorder()));
       
	mtabb = new JTabbedPane();
	//mtabb.setTabPlacement(SwingConstants.LEFT);	
	mtabb.setTabPlacement(SwingConstants.TOP);
	ipwron = new ImageIcon("./images/pwron.gif");
	ipwroff = new ImageIcon("./images/pwroff.gif");
	ipwrerr = new ImageIcon("./images/pwrerr.gif");
	ipwrrmp = new ImageIcon("./images/pwrramp.gif");
	pwricon = ipwron;

	
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		quit(HVmainMenu.this);
		//		System.exit(0);
            }
	});


        Container contentPane = getContentPane();
 
	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                   mtabb,areaScrollPane );

        splitPane.setOneTouchExpandable(true);
	//        splitPane.setDividerLocation(20);

	init();

	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	//	contentPane.setFont(new Font("Serif", Font.PLAIN, 16));

	//contentPane.add(mtabb, BorderLayout.CENTER);
	//contentPane.add(areaScrollPane, BorderLayout.CENTER);
	contentPane.add(splitPane, BorderLayout.CENTER);
	
	setSize(1000, 500);
	Rectangle screenRect = getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                    getGraphicsConfiguration());
 
            // Make sure we don't place the frame off the screen.
            int centerWidth = screenRect.width < getSize().width ?
                    screenRect.x :
                    screenRect.x + screenRect.width/2 - getSize().width/2;
            int centerHeight = screenRect.height < getSize().height ?
                    screenRect.y :
                    screenRect.y + screenRect.height/2 - getSize().height/2;
 
            centerHeight = centerHeight < screenInsets.top ?
                    screenInsets.top : centerHeight;
 
            setLocation(centerWidth, screenInsets.top);

	pack();
	setVisible(true);

	// waiting for the end of the hvframe  initilization
	waitEndInit();
	// init map stuff
	initMap(false);
	setMenuEnabled();
	time_init=(System.currentTimeMillis()- time_start)/1000;
	System.out.println("INIT TIME:" + time_init+ " (sec)" );
	statusArea.updateStatus("INIT TIME:" + time_init+ " (sec)");
	startServer();
        progstate = RUN;	
	System.gc();
	long tm = rt.totalMemory();
	long fm = rt.freeMemory();
	System.out.println("Memory total (bytes):"+tm);
	System.out.println("Memory free  (bytes):"+fm);
    	System.out.println("Memory in use(bytes):"+(tm-fm));
	//try {
	//    Process p = rt.exec("cp hvset.last hvset.prev");
	//    System.out.println("Save done:"+p.exitValue());
	//} catch (IllegalThreadStateException ite) {
	//}catch (IOException ioe) {
	//	}
    }


    public void setDBparameters(String dburl, String dbdriver, String dbuser, String dbpasswd, String dbtable) {
	urldb =dburl ;
	driverdb =dbdriver;
	user = dbuser;
	password = dbpasswd;
	tabledb = dbtable;
    }
 

    /**
     *  Initialization of HVframes by calling initHVF() method and creates
     *  of tabbedpane to represent tables with data from HVframes. 
     *  Load map file if it defined by
     *  system propery "map.file"
     *  @param none
     *  @return none
     */
    public void init() {
	//get list of HVFrames by host names from file 'HVframes.conf'
	//	frameList = new HVFrameList();
	//get list of HVFrames by host names from file 'HVframes.conf'
	frameList = new HVFrameList(configFile);

	if(frameList.getStatus()) {
	    serverPort = frameList.getServerPort(); //24-Apr-2014 server port or set default
	    numFrames = frameList.getHostNum(); // get number of HV hosts (HVFrames)
	    
	    //init HVFrames
      	    initHVF();
	    
	    numFrames = frameList.getHostNum();
	
	    if(!(progstate == RUN)) {

		//		initMap();


		//		setMenuEnabled();
		
	    
		splitPane.setDividerLocation(300);
	    //    progstate = RUN;
	    }
	} else {
	    splitPane.setDividerLocation(20);
	    statusArea.updateStatus("ERROR : No HVFrame List defined."
				    + " Select: 'Edit'-> 'Add Frame List'");  
	    //	    menuBar.getMenu(1).getItem(0).doClick();
    
	}
    }

    /**
     * init HVFrames and add(or remove) its to(from) hashtable 'hvft'
     * @param none
     * @return none
     */
    // Modd: 2-Oct-01
    public void initHVF() {

	String hostname = new String();
	int portnum = 0;	
	int rcount = 0; //2-Oct-01
	String rstore[] = new String[30]; //2-Oct-01

	// first check if hostnames in hvft coresponded to hostnames in frameList
	for (Enumeration e = hvft.keys() ; e.hasMoreElements() ;) {
	    String hs =(String)e.nextElement() ;
	    if ( !frameList.regionMatches(hs) ) {		
		rstore[rcount] = hs;
		rcount++;
	    }
	    
	}
	//2-Oct-01
	for (int i = 0; i < rcount; i++) {
	    // remove mainframe with hostname from hvft and mtabb 
	    // not founded in frameList
	    ((HVframe)hvft.get(rstore[i])).remove(); 
	    hvft.remove(rstore[i]);
	    mtabb.removeTabAt(mtabb.indexOfTab(rstore[i]));
	    // ask to run fnalize methods (unsafe call???)
	    //	System.runFinalization();
	
	}
	// second check if hostnames in frameList coresponded to hostnames in hvft
	rcount = 0;
	

	for (Enumeration e = frameList.elements() ; e.hasMoreElements() ;) {
	    String lhs =(String)e.nextElement() ;
	 
	    hostname = HVFrameList.getToken(1,lhs);
	    try {
		portnum =  Integer.parseInt(HVFrameList.getToken(2,lhs));
	    
	    String protocol = HVFrameList.getToken(3,lhs);
	    String login = HVFrameList.getToken(4,lhs);
	    String password = HVFrameList.getToken(5,lhs);
	    
	    // make short form of hostname=host:port
	    String hs = hostname + ":" + portnum ;

	    if ( !hvft.containsKey((String)hs) ) {
		// ad dmainframe with hostname to hvft and mtabb 
		
		//StringTokenizer toks = new StringTokenizer(hs, ": ");
		//while (toks.hasMoreTokens()) {
		//    hostname = toks.nextToken();
		//    portnum =  Integer.parseInt(toks.nextToken());
		//	}
		
		hvft.put((String)hs,new HVframe(hostname, portnum, protocol, login, password, mtabb, statusArea) );
		//System.out.println(" Put:" + (String)hs );
		errStatus =  ((HVframe) hvft.get((String) hs)).getErrStatus();
		status =     ((HVframe) hvft.get((String) hs)).getStatus();
		if(!errStatus) {
		    String ftip = new String("Click to select HVframe:") + hs ;
		    if ( ((HVframe) hvft.get((String) hs)).HVONstatus) { 
			pwricon = ipwron;
		    } else {
			pwricon = ipwroff;
		    }
		    int ntab = mtabb.getTabCount();
		    int i = 0;
		    // sorting tabb view
		    for( i=0;i<ntab;i++) {
			int res = hs.compareToIgnoreCase(mtabb.getTitleAt(i));
			if(res < 0) break;
		    }	
		    //mtabb.addTab(hs, pwricon, (HVframe)hvft.get((String)hs), ftip);
		    mtabb.insertTab(hs, pwricon, (HVframe)hvft.get((String)hs), ftip, i);
		    
		} else {
		    statusArea.updateStatus(status);
		    rstore[rcount] = hs;				    
		    //  System.out.println(" Add to remove list:" + (String)rstore[rcount] );
		    rcount++;
		}
	    } 
	    
	    } catch (NumberFormatException ne) {
		statusArea.updateStatus("ERROR : Port Number format exception");
	    }
	}
	
	//2-Oct-01
	for (int i = 0; i < rcount; i++) {
	    // comment 27-Feb-2002 need some work!!!
	    //   frameList.removeHostName(rstore[i]);
	    //System.out.println(" Remove:" + (String)rstore[i] );
	    hvft.remove(rstore[i]);
	    statusArea.updateStatus("ERROR : Host:"+rstore[i]+" not initialized");
	    //	    statusArea.updateStatus("ERROR : Host:"+rstore[i]+" removed from HVframeList");
	    
	}
	
	
	statusArea.updateStatus("End initialization");
    }    

    
    // init map if it is presents in file: HVMapList.props 
    /*
     *
     */
    public void initMap(boolean updateAll) {
	mapList = new HVMapList();
       
	int numMaps = 0;
	int rcount = 0; 
	String rstore[] = new String[30];

	if(mapList.getStatus()) {
	    numMaps = mapList.getMapNum();
	    menu = menuBar.getMenu(3); // get menu "Map"
	    
	    // stop mapMonitor
	    if(!hvmt.isEmpty()) {
		if(mapMonitor!=null) {
		    mapMonitor.stopMonitor();
		    // waiting while monitor stops
		    while ( mapMonitor.isAlive()) {
			try {
			    Thread.sleep(100) ;
			    
			} catch (InterruptedException ie) { 
			} 
		    }		
		}
	    }


	    // first check if mapnames in hvmt coresponded to mapnames in mapList
	    for (Enumeration e = hvmt.keys() ; e.hasMoreElements() ;) {
		String hs =(String)e.nextElement() ;
		if (updateAll) { //update all maps
		    rstore[rcount] = hs;
		    rcount++;
		} else 
		    if( !mapList.regionMatches(hs) ) {		
		    rstore[rcount] = hs;
		    rcount++;
		}
		
	    }
	    //System.out.println("nmap:"+numMaps +"  rcount:"+rcount );
	    

	    for (int i = 0; i < rcount; i++) {
		// remove map with name from hvmt and menu
		// that is not founded in mapList
		
		mapTable = (HVMapTable)hvmt.get(rstore[i]); 
		mapTable.setVisible(false);
		mapTable = null;
		hvmt.remove(rstore[i]);
		int cnt = menu.getItemCount();
		for(int k=0;k<cnt;k++) {
		    String text = menu.getItem(k).getText();
		    //System.out.println(text);
		    if ( text.equalsIgnoreCase(rstore[i]) ) {
			menu.remove(k);		
			//  System.out.println(rstore[i]);
		    }
		}
		
		// ask to run fnalize methods (unsafe call???)
		//	System.runFinalization();	
	    }
	    
	    // second check if mapname in frameList is coresponded to mapname in hvmt
	    rcount = 0;	
	    for (Enumeration e = mapList.elements() ; e.hasMoreElements() ;) {
		String mns =(String)e.nextElement() ;
		
		if ( !hvmt.containsKey((String)mns) ) {
		    // add map with name 'mns' to hvmt and menu 
		    
		    //hvmap.add("hatsv16.*.*");
		    
		    // get index of map with name  in mns
		    int indx = mapList.getMapName().indexOf(mns);
		    // get associated filename for this map
		    String fileName= (String)mapList.getFileName().elementAt(indx);
		    String size = (String)mapList.getMapSize().elementAt(indx);
		    // trying to load map file
		    System.out.println("mapfile: " +fileName);		

		    hvmap = new Vector<String>();
		    loadMapFromFile(fileName, mns);
		    // check if map is loaded ok
		    if(isMapLoaded) {
			
			mapTable = new HVMapTable( hvmap , hvft, size);
			mapTable.setMapName(mns);
			hvmt.put((String)mns, mapTable );
			// System.out.println(" Put:" + (String)hs );
			// add menu
			
			String ftip = new String("Click to select HVmap:") + mns ;
			
			addMapMenu(menu,mapTable, mns, ftip);
		    } else {
			// map does not loaded
		    }

		}
		
		
	    }
	

	// start/restart monitor for map
	int delay = 5000; // delay of monitoring loop in 5sec 
	if(!hvmt.isEmpty()) {
	    if(mapMonitor!=null) {
		mapMonitor.stopMonitor();
		// waiting while monitor stops
		while ( mapMonitor.isAlive()) {
		    try {
			Thread.sleep(100) ;
			
		    } catch (InterruptedException ie) { 
		    } 
		}		
	    }
	    mapMonitor = new HVMapMonitor(hvft, hvmt, delay);
	    //restart monitor
	    mapMonitor.start();
	    
	}

	} else {
	    // no load HVMapList.propersties
	    if(mapMonitor!=null) {
		mapMonitor.stopMonitor();
		// waiting while monitor stops
		while ( mapMonitor.isAlive()) {
		    try {
			Thread.sleep(100) ;
			
		    } catch (InterruptedException ie) { 
		    } 
		}				
		
	    }	
	}
    }


    public void setMenuEnabled() {
	
	// enable items "Load Voltage set","Save Voltage Set","Save Settings"
	menuBar.getMenu(0).getItem(0).setEnabled(true);
	menuBar.getMenu(0).getItem(1).setEnabled(true);
	menuBar.getMenu(0).getItem(3).setEnabled(true);
	menuBar.getMenu(0).getItem(4).setEnabled(true);
	menuBar.getMenu(0).getItem(6).setEnabled(true);

    	// enable items "Add/Remove","Enable All Channels","Disable All Channels"
	menuBar.getMenu(1).getItem(0).setEnabled(true);
	menuBar.getMenu(1).getItem(1).setEnabled(true);
	//	menuBar.getMenu(1).getItem(2).setEnabled(true);

	// enable "View" menu
	menuBar.getMenu(2).setEnabled(true);

	// enable "Map" menu
	menuBar.getMenu(3).setEnabled(true);
	// enable "Tools" menu
	menuBar.getMenu(5).setEnabled(true);
	// enable 'Add/Remove Frame List' submenu in menu 'Tools'
	menuBar.getMenu(5).getItem(0).setEnabled(true);

	// enable  item "DataBase "  
	//	try {
	//    DBAdapter db = new DBAdapter(urldb, driverdb, user, password, tabledb); 
	//    menuBar.getMenu(4).setEnabled(true);
	//	} catch (NullPointerException ne) {
	//	menuBar.getMenu(4).setEnabled(false);	
	//    
	//}	


    }

    public void startServer() {
	//start hvserver

	HVServer hvserver = new HVServer(serverPort, hvft, hvmt, statusArea);	
	hvserver.start();
	if(hvserver.isAlive()) // 24-Apr-2014
	    statusArea.updateStatus("Start HV server -> port: "+serverPort); 
	else 
	    statusArea.updateStatus("ERROR : Can't start HV server on port: "+serverPort); 
    }

    /* wait untill all hvframes initiliztion thread are finished
     */
    public void waitEndInit() {
	for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {	    
	    //System.out.println( ((HVframe)e.nextElement()).name );
	    HVframe f = (HVframe)e.nextElement(); 
	    while ( f.isAlive()) {
		try {
		    Thread.sleep(200) ;
		    
		} catch (InterruptedException ie) { 
		} 
	    }
	}

	
    }



    public void addFileMenu(JMenu m) {

	String dataDir = System.getProperty(DataDirKey);
	final  JFileChooser fc  = new JFileChooser(dataDir);	

         //a group of JMenuItems
        menuItem = new JMenuItem("Load Settins",
                                 KeyEvent.VK_L);
        //menuItem.setMnemonic(KeyEvent.VK_L); //used constructor instead
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Load.This doesn't really do anything");
	menuItem.setActionCommand("load_V");
	//	menuItem.addActionListener(this);

	// disable this item while initHVframe
	menuItem.setEnabled(false);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		//fc.rescanCurrentDirectory();
		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("set");
		filter.setDescription("Text files with HV Settings");
		filter.addExtension("hv");
		filter.setDescription("Text files with HV Settings");
		fc.setFileFilter(filter);
		
                int returnVal = fc.showOpenDialog(HVmainMenu.this);
		//System.out.println("retval:"+returnVal);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
		   
		    final  File file = fc.getSelectedFile();

		    if(quitConfirmed(HVmainMenu.this,"Update","Update Settings from file:"+file.getName()+" ?","Update Confirmation")) {
		    final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
			    public Object construct() {
				try {
				    BufferedReader in
					= new BufferedReader(new FileReader(file));
				    load(in);
				    in.close();
				    
				} catch (FileNotFoundException ef) {
				    System.err.println("Load: " + ef);		    
				} catch (IOException ef) {
				    System.err.println("Load: " + ef);
				}
				return "load done";
			    }
			    public void finished() {
				
				//this is where a real application would open the file.
				statusArea.updateStatus("Settings updated from file: " + file + ".");
				
				System.out.println("Voltage set updated from file: " + file + "." + newline);
				//  showStatusDialog("Open file: "+ file.getName());
			    }
			};
		    worker.start();
		    fc.cancelSelection();

		    }
		} else {
			    System.out.println("Open command cancelled by user." + newline);
			}
		
            }
        });

	menuItem.setToolTipText("Load  Settings From File *.hv, *.set");
	m.add(menuItem);        

	//  Save item
        menuItem = new JMenuItem("Save Voltage Set",
                                 KeyEvent.VK_S);
	menuItem.setToolTipText("Save Demand Voltage Set to file *.hv, *.set");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Save.This doesn't really do anything");
	menuItem.setActionCommand("save_V");
	//	menuItem.addActionListener(this);

	// disable this item while initHVframe
	menuItem.setEnabled(false);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		//fc.rescanCurrentDirectory();
		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("set");
		filter.setDescription("Text files with HV Settings");
		filter.addExtension("hv");
		filter.setDescription("Text files with HV Settings");
		fc.setFileFilter(filter);
                int returnVal = fc.showSaveDialog(HVmainMenu.this);
		
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
		    FileWriter out = null;
		    if (saveConfirmed(HVmainMenu.this, file)) {
			try {
			    out = new FileWriter(file);
			    save(out);
			    out.close();
			} catch (FileNotFoundException ef) {
			    System.err.println("Save: " + ef);		    
			} catch (IOException ef) {
			    System.err.println("Save: " + ef);
		    }
			statusArea.updateStatus("Saving: " + file.getName());
			
			//this is where a real application would save the file.
			System.out.println("Saving: " + file.getName() + "." + newline);		    fc.cancelSelection();
		    }
                } else {
                    System.out.println("Save command cancelled by user." + newline);
                }
            }
	    });
	

	m.add(menuItem); 
       
	m.addSeparator(); 


	//  Save Selected Frame item
        menuItem = new JMenuItem("Save Selected HVFrame Settings",
                                 KeyEvent.VK_F);
	menuItem.setToolTipText("Save All Parameters for selected HVFrame to file *.hv, *.set");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Save ALL Selected");
	menuItem.setActionCommand("save_Sel");
	//	menuItem.addActionListener(this);

	// disable this item while initHVframe
	menuItem.setEnabled(false);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		fc.rescanCurrentDirectory();

		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("set");
		filter.setDescription("Text files with HV Settings");
		filter.addExtension("hv");
		filter.setDescription("Text files with HV Settings");
		fc.setFileFilter(filter);

                int returnVal = fc.showSaveDialog(HVmainMenu.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
		    FileWriter out = null;
		    if (saveConfirmed(HVmainMenu.this, file)) {
			try {
			    out = new FileWriter(file);
			    int sel = mtabb.getSelectedIndex();	
			    if(sel>=0) {
				hvf = (HVframe) mtabb.getComponentAt(sel); 
				hvf.save(out);
			    }

			out.close();
			} catch (FileNotFoundException ef) {
			    System.err.println("Save Selected: " + ef);		    
		    } catch (IOException ef) {
			System.err.println("Save Selected: " + ef);
		    }
			statusArea.updateStatus("Saving Settings for "+hvf.host+" : " + file.getName());
   
			//this is where a real application would save the file.
			System.out.println("Saving Settings "+hvf.host+" : " + file.getName() + "." + newline);
			fc.cancelSelection();
		    }
		} else {
                    System.out.println("Save command cancelled by user." + newline);
                }
            }
	    });
	

	m.add(menuItem); 

	//  Save ALL item
        menuItem = new JMenuItem("Save All Settings",
                                 KeyEvent.VK_A);
	menuItem.setToolTipText("Save All Parameters to file *.hv, *set");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "SaveALL");
	menuItem.setActionCommand("save_All");
	//	menuItem.addActionListener(this);

	// disable this item while initHVframe
	menuItem.setEnabled(false);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		fc.rescanCurrentDirectory();
                int returnVal = fc.showSaveDialog(HVmainMenu.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
		    FileWriter out = null;
		    if (saveConfirmed(HVmainMenu.this, file)) {
			try {
			    out = new FileWriter(file);
			saveAll(out);
			out.close();
			} catch (FileNotFoundException ef) {
			    System.err.println("SaveAll: " + ef);		    
		    } catch (IOException ef) {
			System.err.println("SaveAll: " + ef);
		    }
			statusArea.updateStatus("Saving: " + file.getName());
   
			//this is where a real application would save the file.
			System.out.println("Saving: " + file.getName() + "." + newline);
			fc.cancelSelection();
		    }
		} else {
                    System.out.println("SaveAll command cancelled by user." + newline);
                }
            }
	    });
	

	m.add(menuItem); 
       
	m.addSeparator(); 

        menuItem = new JMenuItem("Print",
                                 KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Print");
	menuItem.setActionCommand("print");

	// disable this item while initHVframe
	menuItem.setEnabled(false);

	menuItem.addActionListener(this);
	m.add(menuItem);        

	m.addSeparator(); 

        menuItem = new JMenuItem("Quit",
                                 KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Quit.This doesn't really do anything");
	menuItem.setActionCommand("quit");
	menuItem.addActionListener(this);

	m.add(menuItem);        
   	
    }

    public void addEditMenu(JMenu m) {
         //a group of JMenuItems

	// "Enable All Channels" menu item
        submenu = new JMenu("Enable Channels");
        submenu.setMnemonic(KeyEvent.VK_E);

        menuItem = new JMenuItem("In Selected Module",
                                 KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select.This doesn't really do anything");
	menuItem.setActionCommand("enable_module_chnls");
	menuItem.addActionListener(this);

	// disable this item while initHVframe
	submenu.setEnabled(false);
	submenu.add( menuItem);

        menuItem = new JMenuItem("In Selected Frame",
                                 KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select.This doesn't really do anything");
	menuItem.setActionCommand("enable_frame_chnls");
	menuItem.addActionListener(this);

	// disable this item while initHVframe
	submenu.setEnabled(false);
	submenu.add( menuItem);


        menuItem = new JMenuItem("All Channels",
                                 KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select.This doesn't really do anything");
	menuItem.setActionCommand("enable_all_chnls");
	menuItem.addActionListener(this);

	// disable this item while initHVframe
	submenu.setEnabled(false);
	submenu.add( menuItem);

	m.add(submenu);        


	// "Disable All Channels" menu item
	// next item with submenu
        submenu = new JMenu("Disable Channels");
	submenu.setMnemonic(KeyEvent.VK_D);

        menuItem = new JMenuItem("In Selected Module",
                                 KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select.This doesn't really do anything");
	menuItem.setActionCommand("disable_module_chnls");
	menuItem.addActionListener(this);

	// disable this item while initHVframe
	submenu.setEnabled(false);
	submenu.add( menuItem);
	// frame disable
        menuItem = new JMenuItem("In Selected Frame",
                                 KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select.This doesn't really do anything");
	menuItem.setActionCommand("disable_frame_chnls");
	menuItem.addActionListener(this);

	// disable this item while initHVframe
	submenu.setEnabled(false);
	submenu.add( menuItem);
	// module disable


        menuItem = new JMenuItem("All Channels",
                                 KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select.This doesn't really do anything");
	menuItem.setActionCommand("disable_all_chnls");
	menuItem.addActionListener(this);

	// disable this item while initHVframe
	submenu.setEnabled(false);
	submenu.add( menuItem);

	m.add(submenu);        

	m.addSeparator(); 


	// next item with submenu
        submenu = new JMenu("Options");
        submenu.setMnemonic(KeyEvent.VK_S);

 	cbmenuItem = new JCheckBoxMenuItem("Tab on Top/Left", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_T);
        cbmenuItem.addItemListener(this);
	submenu.add(cbmenuItem);        

		//	menuItem = new JMenuItem("Tab Placement Top/Left",
        //                         KeyEvent.VK_D);
	//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        //        KeyEvent.VK_D, ActionEvent.ALT_MASK));
	// 	menuItem.setActionCommand("set_tab");
	//	submenu.add( menuItem);
	//	menuItem.addActionListener(this);
	
	menuItem = new JMenuItem("Set Monitors Delay",
                                 KeyEvent.VK_D);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, ActionEvent.ALT_MASK));
 	menuItem.setActionCommand("set_delay");
	menuItem.addActionListener(this);
	submenu.add( menuItem);

	m.add(submenu);        

    }

    public void addViewMenu(JMenu m) {
         //a group of JMenuItems
        menuItem = new JMenuItem("SysInfo",
                                 KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "View_SysInfo.This doesn't really do anything");
	menuItem.setActionCommand("view_sys");
	menuItem.addActionListener(this);
	m.add(menuItem);        
	//	m.addSeparator(); 
	// ModuleInfo menuitem
        menuItem = new JMenuItem("ModuleInfo",
                                 KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "View_ModuleInfo.This doesn't really do anything");
	menuItem.setActionCommand("view_module");
	menuItem.addActionListener(this);
	m.add(menuItem);        

	//	m.addSeparator(); 

        menuItem = new JMenuItem("PowerUpStatus",
                                 KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "View_PowerUpStatus.This doesn't really do anything");
	menuItem.setActionCommand("view_pup");
	menuItem.addActionListener(this);
	m.add(menuItem);        

	// SysDefaults menuitem
        menuItem = new JMenuItem("SysDefaults",
                                 KeyEvent.VK_D);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "View_SysDefaults.This doesn't really do anything");
	menuItem.setActionCommand("view_sysdef");
	menuItem.addActionListener(this);
	m.add(menuItem);
        
	m.addSeparator(); 

	// ENET Info menuitem
        menuItem = new JMenuItem("ENET Info",
                                 KeyEvent.VK_E);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "View_ENET INfo.This doesn't really do anything");
	menuItem.setActionCommand("view_enet");
	menuItem.addActionListener(this);
	m.add(menuItem);        

    }

    public void addMapMenu(JMenu m, final HVMapTable hvmap, String name, String tip ) {
	
        //a group of JMenuItems
	
        menuItem = new JMenuItem(name,
                                 KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
						       KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
								 "View_Map.This doesn't really do anything");
	//	menuItem.setActionCommand("view_map");
	//	menuItem.addActionListener(this);
	menuItem.setToolTipText(tip);
	m.add(menuItem);        
//	menuItem.setEnabled(false);
	
	menuItem.addActionListener(new ActionListener() {
		//		final HVmapTable map = hvmap;
		public void actionPerformed(ActionEvent e) {
		    hvmap.pack();		    
		    int w = hvmap.getTableWidth();
		    int h = hvmap.getTableHeight();
		    System.out.println("W:"+w+"   H:"+h);
		    hvmap.setSize(w+30,h+45);
		    
		    hvmap.setVisible(true);
		}
	    });
	
    }


    public void loadMapFromFile(String mapfile, String mapname) {
	String mapDir = System.getProperty(MapDirKey);
	System.out.println("MapDir:"+mapDir);
	mapfile = mapDir + System.getProperty("file.separator") + mapfile;
	final JFileChooser fc = new JFileChooser(mapDir);	
	boolean trynext = false;
	File file = null;
	isMapLoaded = false;
	file = new File(mapfile);
	try {
	    BufferedReader in
		= new BufferedReader(new FileReader(file));
	    loadMap(in);
	    in.close();		     
	    if(mapTable!=null) {
		if(mapTable.isShowing()) {
		    // mapTable.closeWindow();
		    // viewMap();
		} 
	    }
	    statusArea.updateStatus("Map \'"+mapname+"\' loaded from file: " + file.getPath()+ ".");
	} catch (FileNotFoundException ef) {
	    // trynext = true;
	    statusArea.updateStatus("ERROR : Map file not found: " + file.getPath());
	    //  +"/"+file.getName() + ".");
	    System.err.println("Load Map: " + ef);		    
	    isMapLoaded = false;
	    
	} catch (IOException ef) {
	    // trynext = true;
	    System.err.println("Load Map: " + ef);
	    isMapLoaded = false;
	}
	if(trynext ) {
	    fc.rescanCurrentDirectory();
	    int returnVal = fc.showOpenDialog(HVmainMenu.this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		file = fc.getSelectedFile();
		try {
		    BufferedReader in
			= new BufferedReader(new FileReader(file));
		    loadMap(in);
		    in.close();			     
		    if(mapTable!=null) {
			if(mapTable.isShowing()) {
			    // mapTable.closeWindow();
			    // viewMap();
			} 
		    }
		} catch (FileNotFoundException ef) {
		    statusArea.updateStatus("ERROR : Map file not found: " +  file.getPath());
		    //+"/"+   file.getName() + ".");
		    System.err.println("Load Map: " + ef);		    
		} catch (IOException ef) {
		    System.err.println("Load Map: " + ef);
		    isMapLoaded = false;
		}
		statusArea.updateStatus("Map loaded from file: " +  file.getPath());
		//+"/"+ file.getName() + ".");
		fc.cancelSelection();
	    } else {
		//  System.out.println("Open command cancelled by user." + newline);
	    }
	}
    }



    public void addDBMenu(JMenu m) {
        //a group of JMenuItems
        menuItem = new JMenuItem("Store Settings",
                                 KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Store_Settings.This doesn't really do anything");
	menuItem.setActionCommand("store_db");
	menuItem.addActionListener(this);
	menuItem.setToolTipText("Store all current HVframes parameters to DataBase");
	m.add(menuItem);        
	menuItem.setEnabled(true);
        
        menuItem = new JMenuItem("Load Settings",
                                 KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Load_Settings.This doesn't really do anything");
	menuItem.setActionCommand("load_db");
	menuItem.addActionListener(this);
	menuItem.setToolTipText("Load HVframes parameters from DataBase");
	m.add(menuItem);        
	menuItem.setEnabled(true);

    }


    public void addAlarmMenu(JMenu m) {
 	cbmenuItem = new JCheckBoxMenuItem("Sound", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_S);
        cbmenuItem.addItemListener(this);
	m.add(cbmenuItem);        
	m.addSeparator(); 
 	cbmenuItem = new JCheckBoxMenuItem("HVOFF", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_F);
        cbmenuItem.addItemListener(this);
	m.add(cbmenuItem);        
 	cbmenuItem = new JCheckBoxMenuItem("No Connection", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_C);
        cbmenuItem.addItemListener(this);
	m.add(cbmenuItem);        
 	cbmenuItem = new JCheckBoxMenuItem("Panic OFF", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_P);
        cbmenuItem.addItemListener(this);
	m.add(cbmenuItem);        
 	cbmenuItem = new JCheckBoxMenuItem("HVframe Errors", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_E);
        cbmenuItem.addItemListener(this);
	m.add(cbmenuItem);        
 	cbmenuItem = new JCheckBoxMenuItem("Channel Trip", true);
	cbmenuItem.setMnemonic(KeyEvent.VK_T);
        cbmenuItem.addItemListener(this);
	cbmenuItem.setEnabled(false);
	m.add(cbmenuItem);        

	alarmMask = 0x3f;
    }

    public void addToolsMenu(JMenu m) {
        menuItem = new JMenuItem("Add/Remove Frame List",
                                 KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Add/Remove. This doesn't really do anything");
	menuItem.setActionCommand("add/remove");
	menuItem.addActionListener(this);

	menuItem.setEnabled(false);
	m.add(menuItem);        

	m.addSeparator(); 

        menuItem = new JMenuItem("Map Configurator",
                                 KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Popups Map Configurator Dialog.This doesn't really do anything");
	menuItem.setActionCommand("map_config");
	menuItem.addActionListener(this);
	menuItem.setToolTipText("Add/Remove/Edit Map");
	m.add(menuItem);        
	menuItem.setEnabled(true);

	m.addSeparator(); 
        menuItem = new JMenuItem("Inventory",
                                 KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Popups System Inventory Dialog.This doesn't really do anything");
	menuItem.setActionCommand("sys_inventory");
	menuItem.addActionListener(this);
	menuItem.setToolTipText("Get System Inventory");
	m.add(menuItem);        
	menuItem.setEnabled(true);
    }

    
    public void itemStateChanged(ItemEvent e) {
	JMenuItem source = (JMenuItem)(e.getSource());
	if (e.getStateChange() == ItemEvent.DESELECTED) {
	    String s = source.getText();

	    if(s.equalsIgnoreCase("Tab on Top/Left")) {
		mtabb.setTabPlacement(JTabbedPane.LEFT);
		
		//System.out.println("Sound OFF");
	    }
	    if(s.equalsIgnoreCase("Sound")) {
		alarmMask = alarmMask & 0x3e;
		//System.out.println("Sound OFF");
	    }

	    if(s.equalsIgnoreCase("HVOFF")) {
		alarmMask = alarmMask & 0x01f;
	    }

	    if(s.equalsIgnoreCase("Panic OFF")) {
		alarmMask = alarmMask & 0x037;
	    }

	    if(s.equalsIgnoreCase("No Connection")) {
		alarmMask = alarmMask & 0x2f;
	    }

	    if(s.equalsIgnoreCase("HVframe Errors")) {
		alarmMask = alarmMask & 0x3b;
	    }

	    if(s.equalsIgnoreCase("Channel Trip")) {
		alarmMask = alarmMask & 0x3d;
	    }

	}
	
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    String s = source.getText();
	    if(s.equalsIgnoreCase("Tab on Top/Left")) {
		mtabb.setTabPlacement(JTabbedPane.TOP);
		//System.out.println("Sound OFF");
	    }
	    if(s.equalsIgnoreCase("Sound")) {
		alarmMask = alarmMask | 0x1;
		System.out.println("Sound ON");
	    }
	    if(s.equalsIgnoreCase("HVOFF")) {
		alarmMask = alarmMask | 0x020;
	    }

	    if(s.equalsIgnoreCase("Panic OFF")) {
		alarmMask = alarmMask | 0x008;
	    }

	    if(s.equalsIgnoreCase("No Connection")) {
		alarmMask = alarmMask | 0x010;
	    }

	    if(s.equalsIgnoreCase("HVframe Errors")) {
		alarmMask = alarmMask | 0x04;
	    }

	    if(s.equalsIgnoreCase("Channel Trip")) {
		alarmMask = alarmMask | 0x02;
	    }
	}
	
	for (Enumeration<HVframe> f = hvft.elements() ; f.hasMoreElements() ;) {
	    ((HVframe)f.nextElement()).setAlarmMask(alarmMask) ;
	}    

	

    }
    
    public void addHelpMenu(String title) {
        JMenu m = (JMenu)menuBar.add(new JMenu(title));
        menuItem = new JMenuItem("Help",
                                 KeyEvent.VK_H);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Help?.This doesn't really do anything");
	menuItem.setActionCommand("show_help?");
	menuItem.addActionListener(this);
	menuItem.setToolTipText("Show Help");
	
        m.add(menuItem);
	m.addSeparator(); 
	
        menuItem = new JMenuItem("About",
                                 KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "About.This doesn't really do anything");
	menuItem.setActionCommand("show_about");
	menuItem.addActionListener(this);
	menuItem.setToolTipText("About HVS Program");
        m.add(menuItem);
   } 
    
    private void showHelp(String title, String path) {
	final JFrame helpframe = new JFrame(title);	
	HtmlDemo demo = new HtmlDemo(path);
	
	JButton closeButton = new JButton("Close");
	closeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    helpframe.dispose();
		}
	    });
	
	JPanel buttonPane = new JPanel();
	buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
	buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	//buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
	buttonPane.add(closeButton);
	
	//Put everything together, using the content pane's BorderLayout.
	Container contentPane = helpframe.getContentPane();
	contentPane.setLayout(new BorderLayout());
	contentPane.add(demo, BorderLayout.CENTER);
	contentPane.add(buttonPane, BorderLayout.SOUTH);
	
	helpframe.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    helpframe.dispose();
		}
	    });
	
	helpframe.pack();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	helpframe.setLocation(screenSize.width/2 - HtmlDemo.INITIAL_WIDTH/2,
			      screenSize.height/2 - HtmlDemo.INITIAL_HEIGHT/2);
	helpframe.setSize(HtmlDemo.INITIAL_WIDTH, HtmlDemo.INITIAL_HEIGHT);
	helpframe.show();	
	
    }





    
    
    public void closeWindow(JFrame frame) {
		    frame.dispose();
		    frame = null;
    }

    public void save(FileWriter out) {
	for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {
	    ((HVframe)e.nextElement()).save(out) ;
	}    
    }

    public void saveAll(FileWriter out) {
	for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {
	    ((HVframe)e.nextElement()).saveAll(out) ;
	}    
    }

    public void load(BufferedReader in) {
	String raw = new  String();
	StringTokenizer st ;
	boolean EOF = false;

	try {
	    while ((raw = in.readLine())!= null) {		
		raw = raw.trim();
		if(!raw.startsWith("#")) {
		    st = new StringTokenizer(raw);
		    if(st.hasMoreTokens()) {
			String frameid = st.nextToken();
			String command = raw.substring(frameid.length());
			//		frameid = frameid.substring(1);
			//System.out.println("Frame:"+frameid+":"+" Command:"+command+":");
			// int fid = Integer.parseInt(frameid);
			if(hvft.containsKey(frameid)) {
			   ((HVframe) hvft.get(frameid)).load(command);
			} else {
			    this.statusArea.updateStatus("ERROR : in load from file; hostname out of range");
			}
			
		    }
		}
	    }
	} catch (EOFException e) {
	    EOF = true;
	} catch (IOException e) {
	    System.err.println("load()" + e);
	}    
            
    }


    /**
     * loads clorimeter channels map from file to vector hvmap
     * @param in BufferedReader from map file
     * @ retrun none
     */
     public void loadMap(BufferedReader in) {
	String raw = new  String();
	StringTokenizer st ;
	boolean EOF = false;
	int cnt = 0;

        Vector<String> hostnames = new Vector<String>();

	try {
	    while ((raw = in.readLine())!= null) {		
		raw = raw.trim();
		if(raw!=null) {
		if(!raw.startsWith("#")) {
		    // process first string that should be list of host names
		    st = new StringTokenizer(raw);
		    if( cnt == 0) {
			while(st.hasMoreTokens()) {
			    hostnames.add((String) st.nextToken());
			}
		    } else {
			while(st.hasMoreTokens()) {
			    // extract of the hvchannels address: h#ss#cc# - five digits string,
			    //              or seven digits address for submodules :h#ss#sb#cc#
			    //  
 			    //     h# is one digit hostname index in list of hosnames(first string);
			    //     ss# is two digit slot number;
			    //     sb# is submodule name in form S0, S1, S2, ...
			    //     cc# is two digit hv channel number;
			    int i =0;
			    int m =0;
			    int k =0;
			    String nhvaddr = new String();
			    String hvaddr =  st.nextToken();
			    try {
				nhvaddr = "*";
				int lenw=hvaddr.length();
				if ( lenw > 4 ) { 
				    String chind   = hvaddr.substring(lenw-2,lenw);
				    String modind  = hvaddr.substring(lenw-4,lenw-2);
				    String hostind = hvaddr.substring(0,lenw-4);
				    // extract of the hostname, module and channel numbers;	
				    i = Integer.parseInt(hostind);
				    m = Integer.parseInt(modind);
				    chind.toUpperCase();
				    if(chind.startsWith("S")) {
//*** 03-Sep-05: we have sumbodule here in form S0,.., so add it ro module slot number
					modind=modind+chind;    
					chind =  hvaddr.substring(5,7);
				    }
				    k = Integer.parseInt(chind);
				
				// get hostname from vector
				
				    if ( i>0 ){
					String hname = (String) hostnames.get(i-1);
					nhvaddr = hname + "." +m +"." +k;
				    }
				}
			    } catch (ArrayIndexOutOfBoundsException ae) {
				System.err.println("loadMap()" + ae);
				 nhvaddr = "*";	   				
			    } catch (NumberFormatException ne ) {
                                System.err.println("loadMap()" + ne);
                                nhvaddr = "*";     
                            } catch (IndexOutOfBoundsException ie) {
				System.err.println("loadMap()" + ie);
				nhvaddr = "*";	 
			    }
			  
			    hvmap.add((String) nhvaddr);
			}
		    }
		    cnt++;
		}
	  
	    }}
	} catch (EOFException e) {
	    EOF = true;
	} catch (IOException e) {
	    System.err.println("loadMap()" + e);
	}    
	isMapLoaded = true;	
	// 	menuBar.getMenu(3).getItem(0).setEnabled(true);
    }
    
    public void saveMap(FileWriter out) {
	int i =0;
	String blank = new String(" ");
	String linefeed = new String("\n");
	int maxchanperline = 12;
	for (Enumeration e = hvmap.elements() ; e.hasMoreElements() ;) {

	    try {
		out.write( (String)e.nextElement()) ;
		out.write(blank);
		i++;
		if(i>maxchanperline) {
		    i=0;
		    out.write(linefeed);
		}
	    }     catch (IOException ex) {
		System.err.println("saveMap()" + ex);
	    }    
	}
    }
    



    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."
	    + "    Event source: " + source.getText();
	    //                  + " (an instance of " + getClassName(source) + ")";
	//     System.out.println(s);
	if (e.getActionCommand().equals("load_V")) {	
	}
	if (e.getActionCommand().equals("save_V")) {	
	}
	if (e.getActionCommand().equals("print")) {	
	    printTables();
	}
	if (e.getActionCommand().equals("map_config")) {	
	    //	    mapDialog.initialize(this, "HV Map List",
	    HVMapDialog.initialize(this, "HV Map List",
                              "HV Map Parameters");

	    String selectedName = HVMapDialog.showDialog(null,
						       "null");
	    // remove all mapTables from hvmt for updating

	    updateMap();	    
	}
	if (e.getActionCommand().equals("add/remove")) {	
	    HVListDialog.initialize(this, "HV Host List",
                              "Host list of HV frames");
	    String selectedName = HVListDialog.showDialog(null,
						    "null");
	    update(this);
	    
	}
	if (e.getActionCommand().equals("sys_inventory")) {	
	    //	    String chnlstatus = source.getText();
	    getInventory();
	}

	if (e.getActionCommand().equals("enable_all_chnls")) {	
	    //	    String chnlstatus = source.getText();
		EnableAllChannels("1");
	}
	if (e.getActionCommand().equals("enable_frame_chnls")) {	
		EnableFrameChannels("1");
	}
	if (e.getActionCommand().equals("enable_module_chnls")) {	
		EnableModuleChannels("1");
	}
	if (e.getActionCommand().equals("disable_all_chnls")) {	
		EnableAllChannels("0");
	}
	if (e.getActionCommand().equals("disable_frame_chnls")) {	
		EnableFrameChannels("0");
	}
	if (e.getActionCommand().equals("disable_module_chnls")) {	
		EnableModuleChannels("0");
	}

	if (e.getActionCommand().equals("set_alarm")) {	

	}

	if (e.getActionCommand().equals("set_delay")) {	
	    showDelayPanel(source.getX(), source.getY());
	}
	
	if (e.getActionCommand().equals("view_sys")) {	
	    viewSysInfo();
	}

	if (e.getActionCommand().equals("view_module")) {	
	    viewModuleInfo();
	}

	if (e.getActionCommand().equals("view_pup")) {	
	    viewPowerUp();
	}

	if (e.getActionCommand().equals("view_sysdef")) {	
	    viewSysDef();
	}

	if (e.getActionCommand().equals("view_enet")) {	
	    viewENet();
	}
	

	if (e.getActionCommand().equals("store_db")) {	
	    storeDB();
	}

	if (e.getActionCommand().equals("load_db")) {	
	    loadDB();
	}

	if (e.getActionCommand().equals("show_help?")) {	
	    showHelp("HVS Help",HelpFile);
	}
	if (e.getActionCommand().equals("show_about")) {	
	    showHelp("About HVS Program",AboutFile);
	}
	if (e.getActionCommand().equals("quit")) {	
	    quit(this);
	}
	
    }
    
    public void printTables() {
	PrinterJob printJob = PrinterJob.getPrinterJob();
	    /* Set up Book */
	PageFormat landscape = printJob.defaultPage();
	PageFormat portrait = printJob.defaultPage();
	landscape.setOrientation(PageFormat.LANDSCAPE);
	portrait.setOrientation(PageFormat.PORTRAIT);
	Book bk = new Book();
	int sel = mtabb.getSelectedIndex();
	if(sel>=0) {
	    hvf = (HVframe) mtabb.getComponentAt(sel); 
	    int nmodules=hvf.nunit;
	    int selbefore = hvf.tabb.getSelectedIndex();
	    for(int i=0;i<(nmodules);i++) {
	    //	hvf.tabb.setSelectedIndex(i);
		bk.append((Printable)hvf.hvm[i].table, landscape);
	     // it's work!// bk.append((Printable)this, landscape);
	    }
	    // add last page and number of all pages to print
	    //	    bk.append((Printable)hvf.hvm[(nmodules-1)].table, landscape, nmodules);
	    // restore selected module tab
	    hvf.tabb.setSelectedIndex(selbefore);
		
	}
	printJob.setPageable(bk);
	System.out.println("Num Pages:"+bk.getNumberOfPages());
	
	//    printJob.setPrintable(this);
	
	//Page dialog
	//PageFormat pf = printJob.pageDialog(printJob.defaultPage());
	
	//Print dialog
	if(printJob.printDialog()){
	    try { printJob.print(); } catch (Exception PrintException) { }
	}
    }
    
 
    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
	if (pi >= 1) {
	    return Printable.NO_SUCH_PAGE;
	}
	Graphics2D g2 = (Graphics2D) g;
     	//g2.setColor(Color.black);
     	int fontHeight=g2.getFontMetrics().getHeight();
     	int fontDesent=g2.getFontMetrics().getDescent();

     	//leave room for page number
     	double pageHeight = pf.getImageableHeight(); //-fontHeight;
     	double pageWidth = pf.getImageableWidth();
	double frameWidth = this.getWidth();
	double frameHeight = this.getHeight();
     	double wscale = 1; 
      	double hscale = 1; 
    	if (frameWidth >= pageWidth) {
		wscale =  pageWidth / frameWidth;
	}
    	if (frameHeight >= pageHeight) {
		hscale =  pageHeight / frameHeight;
	}

	g2.translate(pf.getImageableX(), pf.getImageableY());	
	//	g.translate(100, 200);
	//Font  f = new Font("Monospaced",Font.PLAIN,12);
	//g2.setFont (f);
	if(hscale <= wscale) {
	    g2.scale(hscale,hscale);
	} else {
	    g2.scale(wscale,wscale);
	}
	paint (g2);
	return Printable.PAGE_EXISTS;
    } 
    
    /** Makes connection to the DataBase and store all HVframes parameters to the table
     *
     * @param none
     * @return none
     */
    public void storeDB() {
	DBAdapter db = new DBAdapter(urldb, driverdb, user, password, tabledb);   
	JTextField textField = new JTextField(20);
	textField.setFont(new Font("Helvetica", Font.PLAIN, 16));
	Object[] inpField = {"input comments", textField};
	
	try {
	    final JOptionPane optionPane = new JOptionPane(inpField,
				 JOptionPane.QUESTION_MESSAGE,
				 JOptionPane.OK_CANCEL_OPTION);
	    
	    final JDialog dialog = optionPane.createDialog(this,"Input Comments"); 
	
	    String comment = null;
	    dialog.pack();
	    dialog.setVisible(true);
	    int value = ((Integer)optionPane.getValue()).intValue();
	    if (value == JOptionPane.OK_OPTION) {
		comment = textField.getText();
		
	    }
	    System.out.println("Comment:"+comment);
	    String insertString = db.prepareInsertString(hvft,comment);
	    
	    if(db.executeUpdate(insertString)) {
		statusArea.updateStatus("HV parameters are stored into DataBase");
	    }
	    
		db.close();
	    } catch (SQLException ex) {
		System.err.println(ex);
	    } catch (NullPointerException ne) {
		System.err.println(ne);
	    } 		
    }


    /** Makes connection to the DataBase and load HVframes parameters (setteble) from table 
     *  to  connected HVframes.
     * @param none
     * @return none
     */
    public void loadDB() {
	ListDB.initialize(null, "List Chooser",
                              "Dates from DB");
	DBAdapter db = new DBAdapter(urldb, driverdb, user, password, tabledb);   
	String query = "SELECT id,timeentry,comments FROM "+tabledb;
	try {
	    Vector v =new Vector();
	    v = db.executeQuery(query,0);
	    int ns = v.size();
	    //create list of string from db
	    for(int i=0;i<ns;i++) {
		ListDB.addElement((String)v.get(i));
	    }
	
	    v.clear();
	    String sel =  ListDB.showDialog(this,"");
	    String id = null;
	    if( sel != null) {
		StringTokenizer toks = new StringTokenizer(sel);
		if (toks.hasMoreTokens()) {
		id = toks.nextToken();
		}
		if(id !=null) {
		    query = db.prepareQuery(id);
	    	    
		    // make query to dataBase and update hvframes
		    v = db.executeQuery(query,1);
		if (quitConfirmed(this,"Update","Update all Settings from DataBse?","Update Confirmation")) {
		    updateFrames(v, id);
		}
		}
	    }
	    
	    db.close();
	} catch (SQLException ex) {
	    System.err.println(ex);
	} catch (NullPointerException ne) {
	    System.err.println(ne);
	} 	
    }
    

    /** Updates HVframes parameters loaded from database
     * @param v - Vector with Strings including HVframe hostname and command line
     * @return none
    */
    public void updateFrames(Vector v, String id) {
	StringTokenizer st;
	for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {
	    HVframe f = ((HVframe)e.nextElement());
	    String host = f.name ;	    	
	    
	    int vs=v.size();
	    Vector<String> cmd = new Vector<String>();
	    
	    for(int i=0;i<vs;i++) {
		String s = (String)v.get(i); 
		st = new StringTokenizer(s);
		String frameid = st.nextToken();
		String command = s.substring(frameid.length());
		System.out.println("Host:"+frameid+"  comand:"+command);
		if(host.equalsIgnoreCase(frameid)) {
		   cmd.add(command.trim());
		}
	    }
	    if( !cmd.isEmpty()) {
		// load to frame using SwingWoker
		updateF(f, cmd, id);
	    } 
	}
	
    }
    
    /**
     * Updates  parameters of connected HVframes. This metod runs as thread using SwingWoker class.
     * @param f - HVframe to update 
     * @param v - Vector with Strings including command line for sending to HVframe
     * @param id - String with number of record id from DataBase
     * @return none
     */
    public void updateF(final HVframe f, final Vector<String> v, final String id) {
	final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		public Object construct() {
		    int l = v.size();
		    for(int i=0; i<l;i++) {
			f.load((String)v.get(i));
		    }
		    return (new Object()); //return value not used by this program
		}
		
		//Runs on the event-dispatching thread.
		public void finished() {
		    statusArea.updateStatus("HV settings with ID="+id+" are loaded from DataBase to the "+f.name);
  		}
	    };
	worker.start();  //required for SwingWorker 3
    }

    /**
     *  Enables or disables All Channels in System.
     * @param stat - String if equals "0" then disable, if equals "1" then enable
     * @return none
     */
    public void EnableAllChannels( final String stat) {

	final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		public Object construct() {

		    for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {
			((HVframe)e.nextElement()).EnableAllChannels(stat) ;
		    }    

		    
		    return (new Object()); //return value not used by this program
		}
		
		//Runs on the event-dispatching thread.
		public void finished() {
		    if(stat.equalsIgnoreCase("1"))
			statusArea.updateStatus("INFO : All Channels have been Enabled.");
		    else statusArea.updateStatus("INFO :All Channels have been Disabled.");
		    
  		}
	    };
	worker.start();  //required for SwingWorker 3	
	
    }


    /**
     *  Enables or disables Channels in Selected Frame.
     * @param stat - String if equals "0" then disable, if equals "1" then enable
     * @return none
     */
    public void EnableFrameChannels(final String stat) {
	final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		public Object construct() {
		    int sel = mtabb.getSelectedIndex();
		    if(sel>=0) {
			hvf = (HVframe) mtabb.getComponentAt(sel);
			hvf.EnableAllChannels(stat) ;
		    }    
		    return (new Object()); //return value not used by this program
		}
	    
		//Runs on the event-dispatching thread.
		public void finished() {
		    if(stat.equalsIgnoreCase("1"))
			statusArea.updateStatus("INFO : Frame Channels in "+hvf.host+"  have been Enabled.");
		    else statusArea.updateStatus("INFO : Frame Channels in "+hvf.host+" have been Disabled.");
		
		}
	    };
	worker.start();  //required for SwingWorker 3
    }

    /**
     * Enables or disables Channels in Selected Module.
     * @param stat - String if equals "0" then disable, if equals "1" then enable
     * @return none
     */
    public void EnableModuleChannels(final String stat) {

	final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		HVmodule hvm = null;
		public Object construct() {
		    int sel = mtabb.getSelectedIndex();
		    if(sel>=0) {
			hvf = (HVframe) mtabb.getComponentAt(sel);
			int msel = hvf.tabb.getSelectedIndex();
			//			hvm=(HVmodule) hvf.tabb.getComponentAt(msel);
			hvm=hvf.hvm[msel];
			if(msel>=0) hvm.EnableAllChannels(stat) ;
		    }    
		    return (new Object()); //return value not used by this program
		}
		
		//Runs on the event-dispatching thread.
		public void finished() {
		    if(stat.equalsIgnoreCase("1"))
			statusArea.updateStatus("INFO : Module Channels in "+hvf.host+" in Slot " +hvm.slot +"  have been Enabled.");
		    else statusArea.updateStatus("INFO : Module Channels in " +hvf.host+" in Slot "+hvm.slot +" have been Disabled.");
		    
		}
	    };
	worker.start();  //required for SwingWorker 3
    }

    public void viewENet() {
	int sel = mtabb.getSelectedIndex();
	if(sel>=0) {
	    hvf = (HVframe) mtabb.getComponentAt(sel);
	    if(!hvf.enet.isEmpty()) {
		statusArea.updateStatus("    ***");
		for(int k = 0; k<hvf.enet.size(); k++) {
		    statusArea.updateStatus((String)hvf.enet.get(k)); 
		}
		statusArea.updateStatus(" *** ENET Info for " + hvf.host + " ***");
	    }
	} else {
	    statusArea.updateStatus("No selected item");
	}
    }

    public void viewSysDef() {
	int sel = mtabb.getSelectedIndex();
	if(sel>=0) {
	    hvf = (HVframe) mtabb.getComponentAt(sel);
	    if(hvf.sysdef != null) {
		statusArea.updateStatus("    ***");
		statusArea.updateStatus(hvf.sysdef); 
		statusArea.updateStatus(" *** SysDefaults for " + hvf.host + " ***");
	    }
	} else {
	    statusArea.updateStatus("No selected item");
	}
    }

    public void viewPowerUp() {
	int sel = mtabb.getSelectedIndex();
	if(sel>=0) {
	    hvf = (HVframe) mtabb.getComponentAt(sel);
	    if(hvf.pupstatus != null) {
		statusArea.updateStatus("    ***");
		statusArea.updateStatus(hvf.pupstatus); 
		statusArea.updateStatus(" *** Power Up Status for " + hvf.host + " ***");
	    }
	} else {
	    statusArea.updateStatus("No selected item");
	}
    }
     
    public void viewSysInfo() {
	int sel = mtabb.getSelectedIndex();
	if(sel>=0) {
	    hvf = (HVframe) mtabb.getComponentAt(sel);
	    if(!hvf.sysinfo.isEmpty()) {
		statusArea.updateStatus("    ***");
		for(int k = 0; k<hvf.sysinfo.size(); k++) {
		    statusArea.updateStatus((String)hvf.sysinfo.get(k)); 
		}
		statusArea.updateStatus(" *** SysInfo for " + hvf.host + " ***");
	    }
	} else {
	    statusArea.updateStatus("No selected item");
	}
    }
    


    public void viewModuleInfo() {
	int sel = mtabb.getSelectedIndex();
	if(sel>=0) {
	    hvf = (HVframe) mtabb.getComponentAt(sel);
	    int selm = hvf.tabb.getSelectedIndex();
	    if(selm>=0) {
		//hvf[sel].hvm[selm].getModel();
		statusArea.updateStatus(" ******");
		statusArea.updateStatus("Number of Properties:  "
					+hvf.hvm[selm].getNumParam()); 
		statusArea.updateStatus("SubChannels:  "
					+hvf.hvm[selm].getSubChns()); 
		statusArea.updateStatus("Channels:  "
					+hvf.hvm[selm].getNChns()); 
		statusArea.updateStatus("Serial Number:  "
					+hvf.hvm[selm].getSerNum()); 
		statusArea.updateStatus("Model:  "
					+hvf.hvm[selm].getModel()); 
		statusArea.updateStatus(" *** ModuleInfo ***");
	    } else {
		statusArea.updateStatus("No selected item");
	    }
	} else {
	    statusArea.updateStatus("No selected item");
	}
    }
    

    
    // get inventory for all frames in system
    public void getInventory() {

         HVInventory inv = new HVInventory();
 	 inv.get(hvft);
       //Make sure we have nice window decorations.
	 //JFrame.setDefaultLookAndFeelDecorated(true);

	 // inv.showInventory();
	 //Create and set up the window.
	 if(true) {
	 
	     
	 //        JFrame frame = new JFrame("HV System Inventory");
	     JFrame frame = HVInventory.frame; 
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = inv;
        newContentPane.setOpaque(true); //content panes must be opaque

        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	 }
    }
    
    //This method must be envoked from the event-dispatching thread.
    public void quit(JFrame frame) {
        if (quitConfirmed(frame,"Quit","Quit from program?","Quit Confirmation")) {
	for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {
	    HVframe f = ((HVframe)e.nextElement());
	    f.close();
	}	    
	    
            System.out.println("Quitting.");
	    rt.exit(0);
	    //            System.exit(0);
        }
        System.out.println("Quit operation not confirmed; staying alive.");
    }
    
    private boolean quitConfirmed(JFrame frame,String bOK, String message, String title) {
        String s1 = bOK;
        String s2 = "Cancel";
        Object[] options = {s1, s2};
        int n = JOptionPane.showOptionDialog(frame,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                s1);
        if (n == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    

    public void updateMap() {
	System.out.println(" update maps");
	initMap(true);
    }

    public void update(JFrame frame) {
	if(updateConfirmed(frame)) {
	    progstate = INIT;
	    final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		    public Object construct() {
			init();
			// disable  item "View Map "  
			menuBar.getMenu(3).getItem(0).setEnabled(false);
			//if(mapframe!=null) {
			//    if(mapframe.isShowing()) {
			//	mapframe.closeWindow();
			//    }
			//}
			waitEndInit();
			initMap(false);

			return null; //return value not used by this program
		    }
		    //Runs on the event-dispatching thread.
		    public void finished() {
			// enable  item "View Map "  
			menuBar.getMenu(3).getItem(0).setEnabled(true);
			progstate = RUN;
		    }
		};
	    worker.start();  //required for SwingWorker 3

	}
    }

   private boolean updateConfirmed(JFrame frame) {
        String s1 = "Yes";
        String s2 = "Cancel";
        Object[] options = {s1, s2};
        int n = JOptionPane.showOptionDialog(frame,
                "Update connections?",
                "Quit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                s1);
        if (n == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean saveConfirmed(JFrame frame, File file) {
	if(file.exists()) {
	    String s1 = "Yes";
	    String s2 = "Cancel";
	    String fname= file.getName();
	    Object[] options = {s1, s2};
	    int n = JOptionPane.showOptionDialog(frame,
						 "Overwrite file: "+fname+"?",
						 "File Exists!",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.QUESTION_MESSAGE,
						 null,
						 options,
						 s2);
	    if (n == JOptionPane.YES_OPTION) {
		return true;
	    } else {
		return false;
	    }
	} else  return true;
    }


    public void showDelayPanel(int x, int y) {
        final JFrame frame = new JFrame("Monitor Delays");
	String title = "Select Monitor Delay";
	JPanel opane = new JPanel();
	opane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(title),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
        final JComboBox delayChooser = new JComboBox(); 
	String[] strDelay = {"1 sec delay", "3 sec delay", "5 sec delay", "9 sec delay"};
       for (int i = 0; i < 4; i++) { //Populate it.
            delayChooser.addItem(strDelay[i]);
        }
       switch(monitordelay) {
       case 1:delayChooser.setSelectedIndex(0);
	   break;
       case 3:delayChooser.setSelectedIndex(1);
 	   break;
       case 5:delayChooser.setSelectedIndex(2);
 	   break;
       case 10:delayChooser.setSelectedIndex(3);
 	   break;
       default:delayChooser.setSelectedIndex(4);
       }
       delayChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		int i = delayChooser.getSelectedIndex();
		String sdelay = (String)delayChooser.getItemAt(i);                
		sdelay = sdelay.substring(0,1);
		sdelay=sdelay.trim();
		int del = Integer.parseInt(sdelay);
		updateMonitorsDelay(del);
		frame.setVisible(false);
	    }
        });
	
	opane.add(delayChooser);
	opane.setPreferredSize(new Dimension(200, 80));
	frame.getContentPane().add(opane);
	//frame.setLocationRelativeTo(this);
	frame.setLocation(x,y);
	frame.pack();
	frame.setVisible(true);
    }
    
   public void updateMonitorsDelay(int delay) {
       monitordelay = delay;
	for (Enumeration<HVframe> e = hvft.elements() ; e.hasMoreElements() ;) {
	    HVframe f =(HVframe)e.nextElement() ;	    
	    f.hvmon.setMonitorDelay(1000*delay);	    
	}
    }

    //MUST be called from the event dispatching thread.
    public void showStatusDialog(String text) {
        JOptionPane.showMessageDialog(null, text);
    }

    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }


}









