
package hvserver;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import hvframe.*;
import hvmap.*;
import hvtools.*;


/**
 *  This class implements two type of protocol to process incoming command 
 *  messages. The first type of protocol(RAWPROTOCOL) is original hvmainframe command messages string
 *  with additional number of hvmainframe in frame list, that sending directly to hvmainframe.
 *  The second type is protocol(MAPPROTOCOL) to control hvchannels parameters by addressing channels 
 *  in detector map coordinates x,y. 
 *  @version 2.0 
 *  Last update:23-Aug-05
 *  Mod&Add: 23-Aug-05 add correction for axis index, if it starts not from '1'
 *  Mod&Add: 04-Aug-05 changing parameter (instead hvmap Vector, we using hvmt Hashtable with stored HVMapTables),
 *           in 'convertToIndex() change conversion formula for different direction of axis(X,Y)
 *           add new method 'getMapName()' to extract 'mapname' from command message;
 *           change command message in maprotocol to: <mapname> <command> <parameter> <address_list>
 *           28 Feb 2005 add searching address of hvframe only in form 'hostname:port'
 *           18 May 2001
 *           22 Nov 2001 in rawprotocol serch for hvframe by index using hostnames vector
 */
public class HVServerProtocol {    
    private static final int WAITING = 0;
    private static final int NEXTMESSAGE = 1;
    private static final int RAWPROTOCOL = 0;
    private static final int MAPPROTOCOL = 1;
    private static final int CONFIGPROTOCOL = 2;
    private static final int UNKNOWNPROTOCOL = -1;
    private int protocol = UNKNOWNPROTOCOL ;
    private boolean nextmessage = true;
    private Vector msgtoreply = new Vector(); 
    // commands list
    private final String cmdup = "UP";
    private final String cmddown = "DOWN";
    private final String cmdget = "GET";
    private final String cmdset = "SET";
    private final String cmdload = "LOAD";
    private final String cmdsave = "SAVE";
    private final String[] commands = {cmdup,cmddown, cmdget, cmdset, cmdload};

    // commands list for config protocol
    private final String[] cnfcommands = {cmdget, cmdset, cmdload, cmdsave};

    // properties list for config protocol
    private final String pmaplist = "MAPLIST";
    private final String pframelist = "FRAMELIST";
    private final String[] cnfproperties = {pmaplist,pframelist};

    // properties name list
    private final String pmc = "MC"; // measured current
    private final String pmv = "MV"; // measured voltage
    private final String pdv = "DV"; // demand voltage
    private final String pce = "CE"; // channel enable
    private final String pst = "ST"; // channel status
    private final String[] properties = {pmc, pmv, pdv, pce, pst};

    private final String ErrMsg1 = "ERROR: wrong message format:";
    private final String ErrMsg2 = "ERROR: wrong command format:";
    private final String ErrMsg3 = "ERROR: wrong property name:";
    private final String ErrMsg4 = "ERROR: wrong channel format:";
    private final String ErrMsg5 = "ERROR: channel number out of range:";
     /**
     *  maximum length of replay string
     */
    private final int MAXMSGLENGTH = 100;

    private String ErrorMsg = new String();
    private boolean ErrStatus = false;
    private Hashtable hvft; // stores hvframes objects
    private Hashtable hvmt; // stores hvmap objects
    private int nframes = 0;
    private int state = WAITING;
    private Vector hvmap;
    private int XMAX = 0;
    private int YMAX = 0;
    private int startX = 0;
    private int startY = 0;
    final char c0 = '\0';
    private boolean Xdir = true;
    private boolean Ydir = true;
    private HVframe hvframe = null;
    private HVMapTable mapTable = null;
    int hostID = 0;
    private String mapname = null;
    private String command = null;
    private String addrlist = null;
    private String property = null;
    private int cmd = 0;
    private String myname = "HVS Server";
    public HVListener notify;

    /**
     * @param hvft  Hashtable with stored hvframe objects in system
     * @param hvmap Vector with hvchannel address in format 'hostname:port.module_slot_number.channel_number',
     *              index of hvchannel address in hvmap is coresponded to x,y coordinates of detector map by expression:
     *              index = XMAX*YMAX - XMAX*(y-1) - x -1
     * @see HVmapTable
     */    
    public HVServerProtocol(Hashtable hvft, Hashtable hvmt) {
    this.hvft = hvft;
    this.nframes =hvft.size();
    this.hvmt = hvmt;
  }

    /**
     * @param hvft  Hashtable with stored hvframe objects in system
     * @param hvmap Vector with hvchannel address in format 'hostname:port.module_slot_number.channel_number',
     *              index of hvchannel address in hvmap is coresponded to x,y coordinates of detector map by expression:
     *              index = XMAX*YMAX - XMAX*(y-1) - x -1
     * @param notify HVListener to send status messages
     * @see HVmapTable
     */    
    public HVServerProtocol(Hashtable hvft, Hashtable hvmt, HVListener notify) {
    this.hvft = hvft;
    this.nframes =hvft.size();
    this.hvmt = hvmt;
    this.notify = notify;
  }

    /**
     * Process input string comman message and return reply message
     * @param theInput String input command message
     * @return String with replay message
     */ 
    public String processInput(String theInput) {
	String theOutput = null;	
	int ind = 0;
	
	theInput=theInput.trim();
	StringTokenizer ss = new StringTokenizer(theInput," ");
	
	System.out.println("Request:"+theInput);
	if(notify!=null)
	    notify.updateStatus("INFO : Request:"+theInput);

	if (state == WAITING) {
	    if(ss.hasMoreTokens()) {
		nextmessage = false;
		try {
		    // look if config protocol first
		    command = getConfigCommand(theInput);
		    if(command!=null) {
			property = getConfigProperty(theInput);
			if(property!=null) { 
			    theOutput = processConfigMsg();
			} else {
			    //error unknown property
			    nextmessage = false;
			    state = WAITING;
			    theOutput = "      " + ErrMsg1 + " no parameters" + c0;
			    return theOutput;
			}
		    } else {
		    // trying to define type of message again
		    mapname = getMapName(theInput);

		    if (mapname!=null) {
			
		    command = getCommand(theInput);
		    if (command!=null) {

			property = getProperty(theInput);
			if(property!=null) {
			    property = property.toUpperCase();
			    addrlist = theInput.substring((mapname.length()+command.length() + property.length()+2));
			    addrlist = addrlist.trim();
			    if (addrlist!=null) {
				switch(cmd) {
				case 1: 
				    theOutput = processMsg(addrlist);
				    break;
				case 2: 
				    theOutput = processMsg(addrlist);
				    break;
				case 3: 
				    theOutput = processMsg(addrlist);
				    break;
				case 4: 
				    theOutput = processMsg(addrlist);
				    break;
				case 5: 
				    nextmessage = false;
				    state = WAITING;
				    theOutput = "      " + "ERROR: Not supported yet" + c0;
				    break;
				default:
				}		
			    } else {
				// no address list
				nextmessage = false;
				state = WAITING;
				theOutput = "      " + ErrMsg2 + " no address" + c0;
				return theOutput;
			    }
			} else { 
			    //error unknown property
			    nextmessage = false;
			    state = WAITING;
			    theOutput = "      " + ErrMsg1 + " no parameters" + c0;
			    return theOutput;
			}
			} else { 
			    //error unknown command
			    nextmessage = false;
			    state = WAITING;
			    theOutput = "      " + ErrMsg1 + " unknown command" + c0;
			    return theOutput;		     
			}
		    } else {
			command = ss.nextToken();			
			//command send directly to hvframe
			
			//hostID = Integer.parseInt(command);//NumberFormatException
			ind = theInput.indexOf(" ");
			if(ind>-1) {
			
			String sendmessage = theInput.substring(ind);
			sendmessage =  sendmessage.trim();
			String name = "HVServer:"+command;
			
			//if(hostID<nframes) {
			    //extract name of HVframe by hostname from hashtable
			    protocol = RAWPROTOCOL;
			    int i=0;
			    //String host = (String) hostnames.get(hostID);
			    String host = command;
			    String hs = null;
			    String hostname = null;
			    int port = 0;
			    for (Enumeration e = hvft.keys() ; e.hasMoreElements() ;) {
				hs =(String)e.nextElement() ;
				StringTokenizer toks = new StringTokenizer(hs, ": ");
				while (toks.hasMoreTokens()) {
				    hostname = toks.nextToken();
				    if(toks.hasMoreTokens()) // ** 04-Apr-2014
					port =  Integer.parseInt(toks.nextToken());
				    else {
					port=0;//???
				    };
				}
				// add on Feb28, 2005 to allow only talk to hvframe with address in form 'hostname:port'
				hostname=hs;
				if (hostname.equalsIgnoreCase(host)) break;
				hs = null;
			    }
			    

				//for (Enumeration e = hvft.elements() ; e.hasMoreElements() ;) {
				//hvframe = (HVframe)e.nextElement();
				//i++;
				//if(hostID==i) break; 
			    //}
			
			    if(hs!=null) {
				System.out.println("to Host:"+hs+" message:"+sendmessage);
				hvframe = (HVframe) hvft.get(hs);
				
				if(hvframe.hvcom.exec(sendmessage,name)) {
			    
				// get response
				if(!hvframe.hvcom.isRawEmpty()) {
				    theOutput = hvframe.hvcom.getRawResponse();
				} 
				if(hvframe.hvcom.isRawEmpty()) {
				    nextmessage = false;
				    state = WAITING;
				} else {
				    nextmessage = true;
				    state = NEXTMESSAGE;
				}
				} else {
				    nextmessage = false;
				    state = WAITING;
				    theOutput = "ERROR:Host:"+hostname + " no response" + c0;
				}
			    } else {
				nextmessage = false;
				state = WAITING;
				theOutput = "ERROR:Hostname of mainframe not found("+ command +")" + c0;
			    }
			    //} else { 
			    // nextmessage = false;
			    //state = WAITING;
			    //theOutput = "ERROR:Frame index out of bound("+nframes+")" + c0;
			    //}
			} else {
			    nextmessage = false;
			    state = WAITING;
			    theOutput = "ERROR:Hostname of mainframe not found("+ command +")" + c0;
			}
		    } 
		    
		    } // **** 13 Oct 2005   if{...} else {
		} catch (NumberFormatException e ) {
		    nextmessage = false;
		    state = WAITING;
		    theOutput = "      " + ErrMsg1 + c0;
		}
		
		} else {	    
		// no command message
		nextmessage = false;
		state = WAITING;
		theOutput = "      " + ErrMsg1 + " no parametrs" + c0;
		return theOutput;
		}
	} else if(state == NEXTMESSAGE) {
	    if(protocol == RAWPROTOCOL) {
		theOutput =hvframe.hvcom.getRawResponse();
		if(hvframe.hvcom.isRawEmpty()) {
		    nextmessage = false;
		    state = WAITING;
		    protocol = UNKNOWNPROTOCOL;
		} else {
		    nextmessage = true;
		    state = NEXTMESSAGE;
		}
	    }
	    if(protocol == MAPPROTOCOL) {
		if(!msgtoreply.isEmpty()) {
		    
		    theOutput = "      " + (String)msgtoreply.remove(0);
		    if(msgtoreply.isEmpty()) {
			nextmessage = false;
			state = WAITING;
			protocol = UNKNOWNPROTOCOL;
		    } else {
			theOutput = "C"+ theOutput.substring(1); 
			nextmessage = true;
			state = NEXTMESSAGE;
		    }
		    theOutput = theOutput +c0;
		}
	    }
	    if(protocol == CONFIGPROTOCOL) {
		if(!msgtoreply.isEmpty()) {
		    
		    theOutput = "      " + (String)msgtoreply.remove(0);
		    if(msgtoreply.isEmpty()) {
			nextmessage = false;
			state = WAITING;
			protocol = UNKNOWNPROTOCOL;
		    } else {
			theOutput = "C"+ theOutput.substring(1); 
			nextmessage = true;
			state = NEXTMESSAGE;
		    }
		    theOutput = theOutput +c0;
		}
	    }


	}
	
	//theOutput = theInput;
	//	nextmessage = false;
	//state = WAITING;
	System.out.println("Response to client:"+theOutput);
	return theOutput;
    }
    
    
    public boolean next() {
	return nextmessage;
    }

    
    private String getConfigCommand(String msg ) {
	String retmsg = null;
	StringTokenizer cc = new StringTokenizer(msg," ");
	if(cc.hasMoreTokens()) {
	    String command = cc.nextToken();
	    for(int i=0; i<4; i++) {
		if(command.equalsIgnoreCase(cnfcommands[i])) {
		    cmd = i+1;
		    retmsg = command;
		    protocol = CONFIGPROTOCOL;
		    break;
		}
	    }
	}
	//	System.out.println("Got:"+retmsg+": Cmd:"+cmd);
	return retmsg;		
    };


    /**
     * Extract and returns mapname from comand message. Setup 'hvmap' vector, XMAX and YMAX for corresponded 'map'.
     *        If no mapname on hvmt then returns null.
     * @param msg String input command message
     * @return String mapname extracted from command mesage or null if no namemap in 'hvmt'
     */
    private String getMapName(String msg) {
	String retmsg = null;
	StringTokenizer ss = new StringTokenizer(msg," ");
	if(ss.hasMoreTokens()) {
	    String first = ss.nextToken();
	    for (Enumeration e = hvmt.keys() ; e.hasMoreElements() ;) {
		String  name =(String)e.nextElement() ;
		if(first.equalsIgnoreCase(name)) {
		    retmsg = name;
		    getMapParameters(name);
		}
	    }

	}
	return retmsg;
    }


    private void getMapParameters(String mapname) {

	mapTable = (HVMapTable)hvmt.get(mapname);
	hvmap = mapTable.hvmap; // get hvmap vector with addresses of hvchannels
	XMAX = mapTable.getMapXmax();
	YMAX = mapTable.getMapYmax();
	Xdir =  mapTable.getMapXdir();
	Ydir =  mapTable.getMapYdir();
	startX = mapTable.getMapXstart();
	startY = mapTable.getMapYstart();	

    }



    /**
     * Extract and returns command from comand message. If command unknown, returns null.
     * @param msg String input command message
     * @return String command extracted from command mesage or null if no command
     */
    private String getCommand(String msg) {
	String retmsg = null;
	StringTokenizer ss = new StringTokenizer(msg," ");
	if(ss.hasMoreTokens())  command = ss.nextToken();// skip mapname
	if(ss.hasMoreTokens()) {
	    String command = ss.nextToken();
	    for(int i=0; i<5; i++) {
		if(command.equalsIgnoreCase(commands[i])) {
		    cmd = i+1;
		    retmsg = command;
		    break;
		}
	    }
	}
	return retmsg;
    }

   /**
     * Extract and returns property from command message. If propery unknown, returns null.
     * @param msg String input command message
     * @return String property extracted from command mesage or null if unknown propery
     */
    private String getConfigProperty(String msg) {
	String retmsg = null;
	String ps = new String();
	// skip command
	
	int lasti = msg.lastIndexOf(" ");	
	ps=msg.substring(lasti+1).trim();
	for(int i=0; i<2; i++) {
	    if(ps.equalsIgnoreCase(cnfproperties[i])) {
		retmsg = ps;
		break;
		}
	}
	
	//	System.out.println("Prop:"+retmsg+" :"+ps);
	return retmsg;
    }

   /**
     * Extract and returns property from command message. If propery unknown, returns null.
     * @param msg String input command message
     * @return String property extracted from command mesage or null if unknown propery
     */
    private String getProperty(String msg) {
	String retmsg = null;
	String property = new String();
	StringTokenizer ss = new StringTokenizer(msg," ");
	// skip command
	if(ss.hasMoreTokens())  property = ss.nextToken();// skip mapname
	if(ss.hasMoreTokens())  property = ss.nextToken();// skip command
	if(ss.hasMoreTokens()) {  
	    property = ss.nextToken();
	    for(int i=0; i<5; i++) {
		if(property.equalsIgnoreCase(properties[i])) {
		    retmsg = property;
		    break;
		}
	      }
	  }
	return retmsg;
    }
    

    /**
     * Process next part of input command message after command extraction for map protocol.
     * Extracts channels address and convert
     *
     */
    private String processConfigMsg() {
	String returnmsg = null;
	String firstch = " ";
	ErrStatus=true;
	
	switch (cmd) {
	case 1: // get command
	    {
		if(property.equalsIgnoreCase(pmaplist)) {
		// get maplist
		    ErrorMsg = "No map loaded";
		
		    for (Enumeration e = hvmt.keys() ; e.hasMoreElements() ;) {
			String  name =(String)e.nextElement() ;
			getMapParameters(name);
			String mm = name + " " +XMAX +" " +YMAX +" " +startX +" " +startY;
			
			msgtoreply.addElement(mm);				
			ErrStatus = false;			
			//			System.out.println(mm);
		    }

	    } else {
		// get framelist
		String hs = new String();
		for (Enumeration e = hvft.keys() ; e.hasMoreElements() ;) {
		    hs =(String)e.nextElement() ;
		    msgtoreply.addElement(hs);				
		    ErrStatus = false;			
		    //System.out.println(hs);
		}

	    }
	    
		break;}
	case 2: // set command
	    break;
	    
	case 3: // load command
	    break;
	    
	case 4: // save command
	    break;
	}
	//		process
	

	//	System.out.println("ErrStatus:"+ErrStatus+" :"+msgtoreply.isEmpty());
	if(!ErrStatus) {
	    if(msgtoreply.size()>1) {
		firstch = "C";
		nextmessage = true;
		state = NEXTMESSAGE;
	    } else if(msgtoreply.size() ==1) {
		nextmessage = false;
		state = WAITING;
	    }
	    
	    if(!msgtoreply.isEmpty()) {
		
		returnmsg = (String)msgtoreply.remove(0);
		returnmsg = firstch + "     " + returnmsg;
		//		System.out.println("returnMsg:"+returnmsg);
	    } 
	    
	} else {
	    ErrStatus = false;
	    returnmsg = "      " + ErrorMsg;
	    nextmessage = false;
	    state = WAITING;
	}
    
	return(returnmsg+c0);
    
    }


    /**
     * Process next part of input command message after command extraction for map protocol.
     * Extracts channels address and convert
     *
     */
    private String processMsg(String msg ) {
	//extract channel indexes from message
	Vector chind = new Vector();
	String hvchan = null;
	String returnmsg = null;
	String firstch = " ";

	if(hvmap.size()>0) {
	    protocol = MAPPROTOCOL;
	    msg = msg.trim();
	    String setvalue = new String();
	    if(msg!=null) {
		int lasti = msg.lastIndexOf(" ");
		setvalue = msg.substring(lasti+1);
		if((lasti>0)&&(cmd!=3)) {
		    // System.out.println("Setvalue="+setvalue+" lasti:"+lasti);
		    msg = msg.substring(0,lasti);	    
		    //System.out.println("Addrlist="+msg);
		} else {
		    if(cmd!=3) {
			returnmsg ="      " +  ErrMsg2;
			nextmessage = false;
			state = WAITING;
			return(returnmsg+c0);
		    }
		}
	    } else {
		returnmsg ="      " +  ErrMsg2;
		nextmessage = false;
		state = WAITING;
		return(returnmsg+c0);
	    }
	    chind = getChannelsIndex(msg);	
	    if(!ErrStatus) {
		//	    returnmsg = chind.toString()+"***"+msg;
		// process command over all channels
		processCmd(cmd,chind,setvalue);
		if(!ErrStatus) {
		    if(msgtoreply.size()>1) {
			firstch = "C";
			nextmessage = true;
			state = NEXTMESSAGE;
		    } else if(msgtoreply.size() ==1) {
			nextmessage = false;
			state = WAITING;
		    }
		    if(!msgtoreply.isEmpty()) {
			returnmsg = (String)msgtoreply.remove(0);
			returnmsg = firstch + "     " + returnmsg;
		    }
		} else {
		    ErrStatus = false;
		    returnmsg = "      " + ErrorMsg;
		    nextmessage = false;
		    state = WAITING;
		}
	    } else {
		ErrStatus = false;
		returnmsg ="      " +  ErrorMsg;
		nextmessage = false;
		state = WAITING;
	    }
	} else {
	    ErrStatus = false;
	    returnmsg ="      " +  "ERROR:Map not loaded";
	    nextmessage = false;
	    state = WAITING;
	}
	return(returnmsg+c0);
    }
	
    /**
     *   Extract numbers of channels from command message.
     *   Format of channels numbers(address) is follows: 
     *   (x1 and y1 are integer numbers from 1 to 22 for x and from 1 to 32 for y)
     *    x1,y2 - one channel with coordinates x=x1 and y=y1
     *    x1:xn,y1 - channels with coordinates x=x1,x2,..,xn and y=y1
     *    x1,y1:yn - channels with coordinates x=x1 and y=y1,y2,..,yn 
     *    x1:xn,y1:ym -channels with coordinates x=x1,x2,..,xn and y=y1,y2,..,ym
     *   @param msg String command message string
     *  @return Vector with hvchannels addresses 
     */
    private Vector getChannelsIndex(String msg) {
	Vector vind = new Vector();
	msg= msg.trim();
	StringTokenizer st = new StringTokenizer(msg);
	while(st.hasMoreTokens()) {
	    String sx1 = null;
	    String sx2 = null;
	    String sy1 = null;
	    String sy2 = null;
	    String sxy=st.nextToken();
	    int ind = sxy.indexOf(",");
	    if( ind > 0) {
		String xx = sxy.substring(0,ind);
		String yy = sxy.substring(ind+1);
		int xind = xx.indexOf(":");
		int yind = yy.indexOf(":");
		//extract pair of begin and end index of x coordinate
		if(xind>0) { 
		    sx1 = xx.substring(0,xind);
		    sx2 = xx.substring(xind+1);
		} else 
		    if(xind == 0) {
			sx1 = "0";
			sx2 = xx.substring(xind+1);
		    } else {
			sx1 = xx;
			sx2 = null;
		    }
		//extract pair of begin and end index of y coordinate
		if(yind>0) { 
		    sy1 = yy.substring(0,yind);
		    sy2 = yy.substring(yind+1);
		} else 
		    if(yind == 0) {
			sy1 = "0";
			sy2 = yy.substring(yind);
		    } else {
			sy1 = yy;
			sy2 = null;
		    }		
	    } else {
		// error in channel list format
	    }
	    System.out.println("SX1="+sx1+":SX2="+sx2+"  SY1="+sy1+":SY2="+sy2);
	    //convert pair of channel coordinates to one flat indexes range (0...XMAX*YMAX)
	    //and add channel numbers to the vector
	    for (Enumeration e = convertToIndex(sx1,sx2,sy1,sy2).elements() ; e.hasMoreElements() ;) {
		vind.addElement(e.nextElement()) ;
	    }
	}
		
      	return vind;
    }

	private Vector convertToIndex(String sx1, String sx2, String sy1, String sy2) {
	    int x1 = 0;
	    int x2 = 0;
	    int y1 = 0;
	    int y2 = 0;
	    Vector vi = new Vector();
	    try {
		x1 = Integer.parseInt(sx1);
		if( sx2!=null ) x2 = Integer.parseInt(sx2);
		y1 = Integer.parseInt(sy1);
		if( sy2!=null) y2 = Integer.parseInt(sy2);
	    } catch (NumberFormatException e ) {
	    	ErrStatus = true;
	    	ErrorMsg = ErrMsg4;
		System.err.println("Error:channel number format:");
	    }
	    //*** 23-Aug-2005 
	    // correction for index shift if it starts not with '1'
	    x1=x1-startX+1;
	    x2=x2-startX+1;
	    y1=y1-startY+1;
	    y2=y2-startY+1;

	    if( (x1<=XMAX)&&(x2<=XMAX)&&(y1<=YMAX)&&(y2<=YMAX)&&(x1>0)&&(y1>0) ) {
		// check direction of X and Y axis and set parameters
		// For conversion to index we  used next formula:
		// index = a*x + b*y - c
		// where coefficients defined as follows:
		// Axis direction: X to left, Y to up:  
		//                 a=-1; b=-XMAX; c=XMAX*(YMAX+1)   
		// Axis direction: X to right, Y to up:  
		//                 a=1;  b=-XMAX; c=XMAX*YMAX-1   
		// Axis direction: X to left, Y to down:  
		//                 a=-1; b=XMAX;  c=0   
		// Axis direction: X to right, Y to down:  
		//                 a=1;  b=XMAX; c=-XMAX-1   
		
		// Axis direction: X to right, Y to up:  
		int a = 1;
		int b = XMAX;
		int c = 0;  
		
		if(Ydir) { // Y incremental to up 
		    b=-XMAX;
		    if(Xdir) { // X incremental to right 
			a=1;
			c=XMAX*YMAX-1;
		    } else {
			a=-1;
			c=XMAX*(YMAX+1);
		    }
		} else {
		    b=XMAX;
		    if(Xdir) { // X incremental to right 
			a=1;
			c=-XMAX-1;
		    } else {
			a=-1;
			c=0;
		    }
		}
		do {	     
		    int x=x1;
		    do {
			
			Integer index = new Integer( a*x + b*y1 + c);
			//		    Integer index = new Integer( XMAX*YMAX - x - XMAX*(y1-1));
		    vi.addElement(index);
		    x++;
		    } while(x<=x2);
		    y1++;
		} while(y1<=y2);
	    } else { 
	    	ErrStatus = true;
	    	ErrorMsg = ErrMsg5;
		System.err.println("Error:channel number out of range:");
	    }
	    
	    return vi;
	}
	
	
    public void processCmd(int cmd, Vector chind, String value) {
	String hvchan = null;
	String retmsg = new String();;
	String chstat = null;
	Hashtable htab = new Hashtable();

	switch (cmd) {
	case 1: // up command
	    retmsg = processUpDown(chind, value, 1);
	    if(retmsg.length()>0) msgtoreply.addElement(retmsg);
	    break;
	case 2: // down command
	    retmsg = processUpDown(chind, value,-1);	    
	    if(retmsg.length()>0) msgtoreply.addElement(retmsg);
	    break;
	case 3: //get command
	    if(hvmap.size()>0) {
		for(Enumeration e = chind.elements(); e.hasMoreElements();) {	
		    hvchan =(String) hvmap.elementAt( ((Integer)e.nextElement()).intValue() );
		    chstat = (String)getChannelStatus(hvchan, property);
		    retmsg = retmsg + chstat + " ";
		    if(retmsg.length()>MAXMSGLENGTH) {
			msgtoreply.addElement(retmsg);
			retmsg = new String();
		    }
		    //System.out.println(hvchan);
		}
		if(retmsg.length()>0) msgtoreply.addElement(retmsg);
	    } 
	    break;
	case 4: // set command


	    htab = sortByHostname(chind);
		
	  for(Enumeration e = htab.keys(); e.hasMoreElements();) {	
	    
	    String host = (String)e.nextElement();
	    String chaddr = (String)htab.get(host);
	    chaddr = chaddr.trim();
	    StringTokenizer sc = new StringTokenizer(chaddr);
	   
	    HVframe f= (HVframe)hvft.get(host);
	    f.hvmon.suspendMonitor(myname);
	    while(sc.hasMoreTokens()) {
	      
	      String msgtosend = "LD "+"S"+sc.nextToken()+" "+property+" "+value;
	      if(f.hvcom.exec(msgtosend, f.name)) {
	      // get response
	      while(!f.hvcom.isRawEmpty()) {
		chstat =  f.hvcom.getRawResponse();
		//		chstat = chstat.substring((7+msgtosend.length()-value.length()), (chstat.length()-1));
	      }
	      String ecode = f.hvcom.getErrorCode();
	      retmsg = command +" " +property +" " +addrlist;
	      if (Integer.parseInt(ecode) >= 20) {
                retmsg = "ERROR - #"+ ecode+" Command :'"
                                     +retmsg+"'" + " Response :" + chstat.trim();
	      }

	      } else {
		  retmsg = "ERROR - Command :"+command +": no response from host:"+host;
		  return;
	      }
	    }
	    f.hvmon.resumeMonitor(myname);
	    
	  }
	
	  if(retmsg.length()>0) msgtoreply.addElement(retmsg);
	  break;

	default: break;
	    
	}
	
	return ;
  }
    

    /** 
     * Process command "UP" and "DOWN" . Calculate new value for demand voltage for requested channels and
     * send its to mainframe.
     * @param chind Vector list of channel addresses
     * @param value String value for changing current settings
     * @param up int parameter, if up=1 process 'UP' command, if up=-1, process "DOWN command
     * @return String error messag from mainframe or null if no errror
     */
    public String processUpDown(Vector chind, String value, int up) {
	String retmsg = new String();
	boolean percents = false;
	Hashtable htab = new Hashtable();
	if(value.endsWith("%")) {
	    percents = true;
	    value = value.substring(0, (value.length()-1));
	}
	double deltaval = Double.parseDouble(value);
	htab = sortByHostname(chind);
	for(Enumeration e = htab.keys(); e.hasMoreElements();) {	
	    String chstat = new String();
	    String host = (String)e.nextElement();
	    String chaddr = (String)htab.get(host);
	    chaddr = chaddr.trim();
	    StringTokenizer sc = new StringTokenizer(chaddr);
	    
	    //System.out.println("host:"+host+">"+chaddr);
	    HVframe f= (HVframe)hvft.get(host);
	    f.hvmon.suspendMonitor(myname);
	    while(sc.hasMoreTokens()) {
		String slotchan = sc.nextToken();
		String hvchanaddr = host+"."+slotchan;
		
		chstat = (String)getChannelStatus(hvchanaddr, property);
		double chval = Double.parseDouble(chstat);
		double delta = deltaval;
		if(percents) {
		    
		    delta = delta*chval/100.;
		}
		
		if(chval<0) {
		    chval = Math.rint(chval - up*Math.abs(delta));
		}
		if(chval>0) {
		    chval = Math.rint(chval + up*Math.abs(delta));
		}
		
		String msgtosend = "LD " +"S" +slotchan +" " +property +" " +chval;
		if(f.hvcom.exec(msgtosend, f.name)) {
		// get response
		while(!f.hvcom.isRawEmpty()) {
		    chstat =  f.hvcom.getRawResponse();
		    //		chstat = chstat.substring((7+msgtosend.length()-value.length()), (chstat.length()-1));
		}
		String ecode = f.hvcom.getErrorCode();
		retmsg = command +" " +property +" " +addrlist;
		if (Integer.parseInt(ecode) >= 20) {
		    retmsg = "ERROR - #"+ ecode+" Command :'"
			+retmsg+"'" + " Response :" + chstat.trim();
		}
		
		} else {
		    retmsg = "ERROR - Command :"+command +": no response from host:"+host;
		    return retmsg;
		}
	    }
	    f.hvmon.resumeMonitor(myname);
	    
	}
	return retmsg;
    }
    

    /**
     * Sorts hvchannel addresses by hostname, creates hashtable to store channels addresses for every host
     * @param chind Vector with hvchannel addresses
     * @return hashtable
     */
    private Hashtable sortByHostname( Vector chind) {

	String star = new String("*");
	String hostname = star;
	String module = star;
	String channel = star;
	String lasthost = new String();
	Hashtable htab = new Hashtable();
	
	// sorting channels by hostname
	for(Enumeration e = chind.elements(); e.hasMoreElements();) {	
	    
	    String hvchan =(String) hvmap.elementAt( ((Integer)e.nextElement()).intValue() );
	    
	    StringTokenizer st =  new StringTokenizer(hvchan,".");
		
	    if(st.hasMoreTokens())  hostname = st.nextToken(); 
	    if(st.hasMoreTokens())  module = st.nextToken(); 
	    if(st.hasMoreTokens())  channel = st.nextToken(); 
	    if(!hostname.equalsIgnoreCase(star)) {
		if(hvft.containsKey(hostname)) { 
		    String chaddr = module+"."+channel+" ";
		    
		    if (htab.containsKey(hostname)) {
			String adds = ((String)htab.get(hostname)) + chaddr;
			htab.put(hostname, adds);
		    }
		    if(!htab.containsKey(hostname)) {
			htab.put(hostname, new String(chaddr));
		    }
		}
		
	    }  
	}
	return htab;
    }

    /**
     * Returns channels property value. 
     * @param chnaddr String address of channel. String in format: H#.S#S#.C#C#
     *                  H# - hostname:portnumber of hvframe
     *                  S#S# - two digit slot number (0-15)
     *                  C#C# - two digit channel number (0-11)
     * @param propname String string with property name
     */
    public Object getChannelStatus(String chnaddr, String propname) {
	
	String star = new String("*");
	String hostname = star;
	String module = star;
	String channel = star;
	int imodule = 0;
	int smodule = 0;
	int ichannel = 0;
	String chnstat = new String("***"); // present not connected channels

	StringTokenizer st =  new StringTokenizer(chnaddr,".");
	if(st.hasMoreTokens())  hostname = st.nextToken(); 
	if(st.hasMoreTokens())  module = st.nextToken(); 
	if(st.hasMoreTokens())  channel = st.nextToken(); 
   	if(!hostname.equalsIgnoreCase(star)) {
	    if(hvft.containsKey(hostname)) { 
		//do next step
		if(!module.equalsIgnoreCase(star)) {
//**** 3-Sept-2005, changed input parameter to String instead of integer
		    //		    smodule = Integer.parseInt(module) ;
		    imodule = ((HVframe)hvft.get(hostname)).getModuleIndex(module);
		    if(imodule>=0) {
			if(!channel.equalsIgnoreCase(star)) {
			    ichannel = Integer.parseInt(channel) ;
			    boolean isHVON = ((HVframe)hvft.get(hostname)).HVONstatus;
			    chnstat = ((HVframe)hvft.get(hostname)).hvm[imodule].ch[ichannel].getValue(propname);
			    //		System.out.println(propname+":"+hostname+":"+imodule+":"+ichannel+":"+chnstat);
			    if( propname.equalsIgnoreCase("CE")) {
				if(chnstat.equalsIgnoreCase("1")&& isHVON) chnstat = "1";
				if(chnstat.equalsIgnoreCase("1")&& (!isHVON)) chnstat = "0";
			    }
			}
		    }
		}
	    }
	}
	
	return chnstat;
    }
    



}


    

















