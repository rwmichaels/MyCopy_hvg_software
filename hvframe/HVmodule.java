/* 
 * class HVmodule - initialization of channels of HVmodule. 
 *                  Store data about channels properties, attributes and status.
 *                  Also has a HVmoduleTable(JTable) to show and edit channels data
 * @version 1.2                 
 * Last update: 08-Feb-2014
 */

// 13-Dec-2011 command RC is used instead of channels 'init()' method due to DMP command bug issue for emulated HV crates
// 08-Feb-2014  a) initialization all parameters for all channels to ZERO (due to some time empty string responses 
//                for RC command from emulated hvframe). 
//              b) replace 'val' type from Hashtable to ConcurrentHashMap<> in HVmodule class. Need java version 1.5 or more
//              c) add NullPointerException  for HVchannel::setValue();
// 10-Feb-2014  Add PROP and RC commands repeat few times if got empty response in HVmodule() class constructor.
//              To compile: javac -Xlint:unchecked hvframe/HVmodule.java	    


package hvframe;
import java.util.*;
import java.util.concurrent.*;
import java.awt.print.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Dimension;

import hvtools.*;

public class HVmodule implements Cmd, Printable {
    public int id = 0; // HVmodule id (logical number in HVframe)
    public String lu = null; // logic name of HVmodule like: L#
    public String slot = null; // slot number of module in frame like: S#
    public String moduleID = null; // store data about HVmodule 
                                   //(return by ID command)
    public String name = null;     // Full name of HVmodule
                                   //(including hostname of HVframe)
    public int nchn = 0;           // number of channels in HVmodule
    public String status = null;   
    public String params = null;   // string of parameters(properties)
                                   // of HVmodule
    public int numparam = 0;       // number of parameters in HVmodule
    public HVMessage mess = null;  // HVmessage object to communicate 
                                   // with 'client'
    public HVCommand hvcom = null; // HVCommand object to send(receiv) 
                                   //command(response) to(from) 'client'; 
    public Vector par = new Vector(); // properties list    
    public UpAttribute[] pattr = null; // array of attributes 
    public String bl = " ";
    public HVmoduleTable table = null;// table for representation channels data
    public HVListener notifys; // interface to update Status Window
    
    public HVchannel[] ch = null;// channels objects to store channels data

    private boolean channelTrip = false; // set to true if channel has any trip
    private Vector chTrip = new Vector();
    

    // default constructor
    public HVmodule(HVMessage mess,String servername, int id, String slot, String moduleID, HVListener notifys) {
	this.id = id;
	this.lu = "L"+id;
	this.slot = slot;
	this.name = servername+":Module "+lu;	    
	this.mess = mess ;
	this.hvcom = new HVCommand(mess, notifys);
	this.notifys = notifys;

	this.moduleID = moduleID;

       	String command = ID +bl +lu;
	
	//	hvcom.exec(command,name);
	// get response
	//	while(!hvcom.isEmpty()) {
	//    moduleID =  hvcom.getResponse();
	//	}
	//System.out.println("response:module:"+name+ " ID:"+moduleID);

	numparam = Integer.parseInt(getNumParam());
	
	pattr = new UpAttribute[numparam]; 
	command = PROP +bl +lu;
	if(hvcom.exec(command,name)) {
	// get response
	while(!hvcom.isEmpty()) {
	    params =  hvcom.getResponse();
	    //     System.out.println(" resp:"+params);	
	}
	

	// *** 10-Feb-2014
	if(params!="") 
	    setParamName(params);
	else {// try few times
	    System.out.println("... Try more times : "+name+ " : "+ command);
	    if(hvcom.exec(command,name)) {
		// get response
		while(!hvcom.isEmpty()) {
		    params =  hvcom.getResponse();
		}
	    	if(params!="") 
		    setParamName(params);
		else {// try one more time
		    System.out.println("... Try more times : "+name+ " : "+ command);
		    if(hvcom.exec(command,name)) {
			// get response
			while(!hvcom.isEmpty()) {
			    params =  hvcom.getResponse();
			}
			if(params!="") 
			    setParamName(params);
			else {// print error
			    System.out.println("Error : Got empty response after 3 attempts : "+name+ " : "+ command);			    
			}
		    }
		}
	    }
	} // *** 10-Feb-2014

	nchn = Integer.parseInt(getNChns());
	//	System.out.println("CHN:"+nchn);
	ch = new HVchannel[nchn];	
	String parameter  = null;
	String values = null;

	for (int i=0; i<nchn;i++) {
	    ch[i] = new HVchannel(mess, lu, String.valueOf(i));
	    // ch[i].init(); // *** 13-Dec-2011

	    // *** 08-Feb-2014 
	    // *** initialization all parameters for all channels to ZERO (due to some time empty string responses 
	    // *** for RC command from emulated hvframe).	    
	    for (int ip=0; ip<numparam; ip++) {
		parameter = getParameterName(ip);
		ch[i].setValue(parameter, new String("0.0"));
	    }
	    
	} 
	//*** 13-Dec-2011 command RC used instead of channels 'init()' due to DMP command bug issue for emulated HV crates
	// ask values of all channels for every parameters
	for (int i=0; i<numparam; i++) {
	    parameter = getParameterName(i);

	    command = RC+bl+lu+bl+parameter;
	    if(hvcom.exec(command,name)) {
	    // get response
		while(!hvcom.isEmpty()) {
		    values = new String( hvcom.getResponse());
		}

		if(getnword(values)==nchn)
		    //		if(values!="")
		    channelsUpdate(parameter,values);
		else { 
		    // got empty response? skip it? Try more time!
		    // *** 10-Feb-2014
		    System.out.println("Error : Got empty response for : "+name+ " : "+ command);	    
		    int itry=0; // try repeat command 4 times
		    int ntry=4;
		    while(getnword(values)!=nchn) {
		    //		    while(values=="") {
			System.out.println("...Try more times for : "+name+ " : "+ command+" : "+(ntry-itry));
			if(hvcom.exec(command,name)) {
			    // get response
			    while(!hvcom.isEmpty()) {
				values = new String( hvcom.getResponse());
			    }
			    itry++;
			}
			if(itry> (ntry-1)) break;
		    };
		    if(getnword(values)==nchn)
			//		    if(values!="") 
			channelsUpdate(parameter,values);
		    else {
			System.out.println("Error : Got empty response after 4 attempts : "+name+ " : "+ command);
		    }
		    
		}
	    }
	}
	
        table = new HVmoduleTable(this);
	} 
    }

    // constructor to optimize initializtion of module ( atributes are taked from previose the same module)
    public HVmodule(HVMessage mess,String servername, int id, String slot, String moduleID, Vector par, UpAttribute pattr[],  HVListener notifys) {
	this.id = id;
	this.lu = "L"+id;
	this.slot = slot;
	this.name = servername+":Module "+lu;	    
	this.mess = mess ;
	this.hvcom = new HVCommand(mess, notifys);
	this.notifys = notifys;

	this.moduleID = moduleID;

       	String command = ID +bl +lu;
	
	numparam = Integer.parseInt(getNumParam());
	
	this.par = par;
	this.pattr = pattr;

	nchn = Integer.parseInt(getNChns());
	ch = new HVchannel[nchn];	
	String parameter  = null;
	String values = null;

	for (int i=0; i<nchn;i++) {
	    ch[i] = new HVchannel(mess, lu, String.valueOf(i));
	    //	    ch[i].init();

	    // *** 08-Feb-2014 
	    // *** initialization all parameters for all channels to ZERO (due to some time empty string responses 
	    // *** for RC command from emulated hvframe).	    
	    for (int ip=0; ip<numparam; ip++) {
		parameter = getParameterName(ip);
		ch[i].setValue(parameter, new String("0"));
	    }
	} 
	
	// ask values of all channels for every parameters
	for (int i=0; i<numparam; i++) {
	    parameter = getParameterName(i);

	    command = RC+bl+lu+bl+parameter;
	    if(hvcom.exec(command,name)) {
	    // get response
		while(!hvcom.isEmpty()) {
		    values = new String( hvcom.getResponse());
		}
		//	
		if(getnword(values)==nchn)
		    //if(values!="")
		    channelsUpdate(parameter,values);
		else { // got empty response? skip it!
		    // got empty response? skip it? Try more time!
		    // *** 10-Feb-2014
		    System.out.println("Error : Got empty response for : "+name+ " : "+ command);	    
		    int itry=0; // try repeat command 4 times
		    int ntry=4;
		    while(getnword(values)!=nchn) {
			System.out.println("...Try more times for : "+name+ " : "+ command+" : "+(ntry-itry));
			if(hvcom.exec(command,name)) {
			    // get response
			    while(!hvcom.isEmpty()) {
				values = new String( hvcom.getResponse());
			    }
			    itry++;
			}
			if(itry> (ntry-1)) break;
		    };
		    if(getnword(values)==nchn)
			//		    if(values!="") 
			channelsUpdate(parameter,values);
		    else {
			System.out.println("Error : Got empty response after 4 attempts : "+name+ " : "+ command);
		    }
		    		    		    
		} // else
	    }
	}
        
	table = new HVmoduleTable(this);
    } 

    // send command to frame, waiting for response and returns response
    //
    public String doCmd(String command, String name) {
	return "0";
    }
    
    // return token(word) with index n in input String 
    public String getword(String sinp,int n) {
	StringTokenizer s = new StringTokenizer(sinp.trim());
	String dummy = null;
	int i = 0;
	while (s.hasMoreTokens()) {
	    dummy = s.nextToken();
	    if (n==i) return dummy ;
	    i++;
	}
	return null;
    }

    // return number tokens(word) in input String
    public int getnword(String sinp) {
	StringTokenizer s = new StringTokenizer(sinp.trim());
	return 	s.countTokens(); 
    }
  	
    public String getNChns() {
	return getword(moduleID,4);
	}

    public String getNumParam() {
	return getword(moduleID,3);
	}
    
    public String getSubChns() {
	return getword(moduleID,2);
	}
    // 06-Sep-2005
   public String getSubChnNum() {
	return getword(moduleID,1);
	}

    public String getModel() {
	return getword(moduleID,0);
	}
	
    public String getSerNum() {
	return getword(moduleID,5);
	}

    public void EnableAllChannels(String stat) {
	String parname = new String("CE");
	if(hasParameter(parname)) {
	    String com="LD "+lu+bl+parname;
	    for(int i=0 ; i<nchn; i++) {	
		com = com + bl + stat;
	    }
	    
	    if(hvcom.exec(com,name)) {
	    // get response
	    while(!hvcom.isEmpty()) {
		String dummy =  hvcom.getResponse();
	    }
	    }
	}
    }

    public  Vector getParameters() {
	return par;
    }

     public  UpAttribute[] getAttributes() {
	return pattr;
    }

    public boolean hasParameter(String parname) {
	return par.contains((String)parname);
    }

    public  String getParameterName(int ind) {
	return (String)par.get(ind);
    }

    public int getParameterIndex(String par) {
	return par.indexOf((String)par);
    }

    public void channelsUpdate(String par, String val) {
	StringTokenizer st = new StringTokenizer(val);
	int i = 0;
	while (st.hasMoreTokens()) {
	    String sval = st.nextToken();
	    if(i<nchn) {
		ch[i].setValue(par,sval);
	    }
	    i++;
	}
	if(i!=nchn) { // error in  'val'
	    System.out.println("Error : "+name+" : Got wrong values in channelsUpdate() : param_name="+par+ " , values="+ val);	    
	}
    }
    
    /** returns true if any channel is in ramping conditions
     */ 
    public boolean isChannelRamping() {
	boolean ret = false;
	
	String sval = null;
	for(int i=0;i<nchn;i++) {
	    sval = ch[i].getValue("ST");
	    try {
		long status = Long.parseLong(sval,16);
		if(status>15) {
		    //set Alarm channel trip
		    if(!chTrip.contains(new Integer(i))) {
			chTrip.add(new Integer(i));
			String stat = "ALARM :"+name+"."+i+":"+statusToString(status);
			notifys.updateStatus(stat);
		    }
		    channelTrip = true;
		} else {
		    if(chTrip.contains(new Integer(i))) chTrip.remove(new Integer(i));
		    if(chTrip.isEmpty()) channelTrip = false;
		}
		    
		if(((status>>2)&1)==1) {
		    return true;
		    // ramping to lower absolute value or zero   
		}
		if(((status>>1)&1)==1) {
		    // ramping to higher absolute value   
		    return true;
		}
		
	    } catch (NumberFormatException en) {}				
	    
	}	
	return ret;
    }
    
    public boolean isChannelsTrip() {
	return channelTrip;
   }

    public Vector getChannelsTrip() {
	return chTrip;
    }



    public String statusToString(long status ) {
	String s = new String("Status:");
	if(((status>>2)&1)==1) {
	    s = s + "Ramping to lower absolute value or zero:";
	    // ramping to lower absolute value or zero   
	}
	if(((status>>1)&1)==1) {
	    // ramping to higher absolute value   
	    s = s + "Ramping to higher absolute value:";
	}
	// convert trip conditions
	for(int i =0 ; i<12; i++) {
	    if(((status>>(i+4))&1)==1) {
		// Trip condition   
		s = s + "Trip Condition " + i+":(This state is cleared by cycling Enable/Disable status)";
	    }
	}

	return s;
    }

    // call from init() methods    
    public void setParamName(String propnames) {
	StringTokenizer s = new StringTokenizer(propnames.trim());
	String command = ATTR + bl +lu +bl;
	String response = new String();
	String str = new String();
	int i =0;
	
	    while (s.hasMoreTokens()) {
	    //store parameters names in vector
	    str = s.nextToken();
	    //	    System.out.println(str );
	    par.add(i, (String)str );
	    // add attributes for this parameter and set name of parameter
	    pattr[i] = new UpAttribute(str) ;

	    // ask hvmodule about attributes of given parameter
	    if(hvcom.exec(command + str,name)) {

	    // get response from hvmodule
	    while(!hvcom.isEmpty()) {
		response =  hvcom.getResponse();
	    }
	    // store attributes in hashtable
	    pattr[i].setAttr(response);
	    //System.out.println(str );
	    i++;
	    }
	    }
	//numparam = par.size();
    }	
    

     public int print(Graphics g, PageFormat pageFormat, 
        int pageIndex) throws PrinterException {
     	Graphics2D  g2 = (Graphics2D) g;
     	g2.setColor(Color.black);
     	int fontHeight=g2.getFontMetrics().getHeight();
     	int fontDesent=g2.getFontMetrics().getDescent();

     	//leave room for page number
     	double pageHeight = pageFormat.getImageableHeight()-fontHeight;
     	double pageWidth = pageFormat.getImageableWidth();
     	double tableWidth = (double) table.hvtable.getColumnModel().getTotalColumnWidth();
     	double scale = 1; 
     	if (tableWidth >= pageWidth) {
		scale =  pageWidth / tableWidth;
	}

	System.out.println("in printing: scale: "+scale);
     	double headerHeightOnPage=
                      table.hvtable.getTableHeader().getHeight()*scale;
     	double tableWidthOnPage=tableWidth*scale;

     	double oneRowHeight=(table.hvtable.getRowHeight()+
                      table.hvtable.getRowMargin())*scale;
     	int numRowsOnAPage=
                      (int)((pageHeight-headerHeightOnPage)/oneRowHeight);
     	double pageHeightForTable=oneRowHeight*numRowsOnAPage;
     	int totalNumPages= (int)Math.ceil((
                      (double)table.hvtable.getRowCount())/numRowsOnAPage);
     	if(pageIndex>=totalNumPages) {
                      return NO_SUCH_PAGE; 
     	}

     	g2.translate(pageFormat.getImageableX(), 
                       pageFormat.getImageableY());
     	g2.drawString("Page: "+(pageIndex+1),(int)pageWidth/2-35,
                      (int)(pageHeight+fontHeight-fontDesent));//bottom center

	if (false) {
     	g2.translate(0f,headerHeightOnPage);
     	g2.translate(0f,-pageIndex*pageHeightForTable);

     	//If this piece of the table is smaller than the size available,
     	//clip to the appropriate bounds.
     	if (pageIndex + 1 == totalNumPages) {
                     int lastRowPrinted = numRowsOnAPage * pageIndex;
                     int numRowsLeft = table.hvtable.getRowCount() - lastRowPrinted;
                     g2.setClip(0, (int)(pageHeightForTable * pageIndex),
                       (int) Math.ceil(tableWidthOnPage),
                       (int) Math.ceil(oneRowHeight * numRowsLeft));
     	}
     	//else clip to the entire area available.
     	else{    
                     g2.setClip(0, (int)(pageHeightForTable*pageIndex), 
                     (int) Math.ceil(tableWidthOnPage),
                     (int) Math.ceil(pageHeightForTable));        
     	}
	}
     	g2.scale(scale,scale);
     	table.paint(g2);

	
	//g2.scale(1/scale,1/scale);
     	//g2.translate(0f,pageIndex*pageHeightForTable);
     	//g2.translate(0f, -headerHeightOnPage);
     	//g2.setClip(0, 0,(int) Math.ceil(tableWidthOnPage), 
        //                       (int)Math.ceil(headerHeightOnPage));
	//     	g2.scale(scale,scale);
     	//    table.hvtable.getTableHeader().paint(g2);//paint header at top

     	return Printable.PAGE_EXISTS;
   }



    // inner class
    public class HVchannel implements Cmd{
	public String number = null; 
	public String id = null;
	public String name = null;
	public String status = null;
	//***	public Hashtable val = null;
	ConcurrentHashMap<String, String> val = null;
	public HVMessage mess = null;
	
	// constuctor
	public HVchannel(HVMessage mess,String lu, String id) {
	    this.number = id;
	    this.id = lu +"." +id;
	    this.name = "CH" +id;
	    this.mess = mess;
	    //***	    this.val = new Hashtable();
	    this.val = new ConcurrentHashMap<String, String>(8, 0.9f, 1);	
	}
    
	public void init() {
	    String command = DMP +bl +id;
	    if(hvcom.exec(command,name)) {
	    String initpar = null;
	    // get response
	    while(!hvcom.isEmpty()) {
		initpar =  hvcom.getResponse();
		for (int k=0;k<par.size();k++) {		
		    // setup init value of channels parameters
	
		    setValue((String)par.get(k),getword(initpar,k));
		}
	    }
	    }
	}

	// method to store channel parameters like pair: 
	//        ParameterName=ParameterValue      
	public void setValue(String name, String value) {
	    try {
		val.put((String)name, (String)value);
	    }  catch (NullPointerException en) {
		System.out.println("Error : NullPointerException  for channel : "+id+" : in SetValue(name=" +name+" , value="+value+")");	       
	    }
	}

	// get channel parameter value
	public String getValue(String name) {
	    return (String)val.get((String)name);
	}

	public void Enable(String stat) {
	}
    }
}















