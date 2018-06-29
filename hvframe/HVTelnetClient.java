
package hvframe;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;


import hvtools.*;
import socket.*;

/* HVTelnetClient - this class runs as thread, makes connection to server
 *  (high voltage  mainframe) by telnet protocol using DIGI portserver
 * and transfer messages to this host and receives responses from it
 * @version 1.0 
 * Last updates: 06-June-2005: login protocol to hvframe 
 *               20-June-2005: add variable 'name'='server:port' and put it in status messages
 *               29-Oct-2005: add TimedOutExeption in run() 
 *                            and call to isConnected() to check connection after InterruptedExeption  
 */

public class HVTelnetClient extends HVclient  {
 
    String command = null;
    private HVMessage hvmess;
    /** Server name */
    private String server = null;
    /** Server port */
    private int port = 0;
    /** Full server name in form: 'server:port' */
    private String name = null;
    boolean frozen = false;

    private long timeout = 20000; //timeout for Socket.read() in mSec
    HVframe f;
    private boolean errStatus = false;
    private String status = new String("OK");
    /** Login name */
    private String login;
    /** Password */
    private String password;
    /** Login prompt  */
    private String loginPrompt = ":" ;
    /** Password prompt  */
    private String passwordPrompt = ":";
    /** Prompt  */
    private String prompt = new String(">");
    private TelnetWrapper telnet;
    private int  sleepToReopen = 30000; // sleep time for next try

    // constructor 
    public HVTelnetClient(HVframe f, HVListener notifys) {
	super(f);
	this.f = f;
	this.hvmess = f.mess ;	
	server = f.name;
	port = f.port;
	name=server+":"+port;
	login = f.login;
	password = f.password;
	this.notifys = notifys;
	hvmess.setConnected(false);
	hvmess.setTimeOut(timeout);
	
        //System.out.println(server+":"+port+":"+loginPrompt+":"+login+":"+passwordPrompt+":"+password+":"+prompt);
    	// init connection to server
	try {
	    telnet = new TelnetWrapper(server, port);
	    //telnet.debug = true;
	    //System.out.println("new Telnet");
	    // setting the correct prompt ahead of time is very important 
	    // if you want to use login and sendLine
	    //telnet.setPrompt(prompt);

	    telnet.setLoginPrompt(loginPrompt);
            telnet.setPasswordPrompt(passwordPrompt);
    
	    if(login!=null) {
		telnet.send("\n\r");    
		telnet.login(login, password);
		//System.out.println("telnet login");
	    }

	    telnet.setPrompt(prompt);
	    
	    telnet.send("\r");    
    	    telnet.wait(prompt, 10000);
	    System.out.println("wait prompt");
	    
	    Thread.sleep(100);
	    String ls = telnet.sendLine("1450\r");
	    System.out.println("sendLine1:"+ls);


	    //telnet.send("\r");
	    Thread.sleep(100);
	    
	    ls = telnet.sendLine("1450\r"); // 06 June 2005
	    //  System.out.println("sendLine2:"+ls);		
	    Thread.sleep(100);

	    telnet.send("\r");    
    	    telnet.wait("1450"+prompt, 10000);
	    System.out.println("wait prompt1450");
	   
		telnet.send("HI\r");		    
		telnet.wait(prompt, 10000);

		hvmess.setConnected(true);
		fgetconnection = true;
		errStatus = false;
	

	} catch  (TimedOutException te) {
      	    status = "Login Time Out: " + name;
            System.err.println(status);
	    errStatus = true;
	    notifys.updateStatus(status);
	    hvmess.setConnected(false);
	    // System.exit(1);
        } catch (IOException e) {
	    status ="HVTelnetClient:Couldn't get I/O for the connection to:" + name;
            System.err.println(status);
	    errStatus = true;
	    hvmess.setConnected(false);
	    notifys.updateStatus(status);
	    // System.exit(-1);

	} catch (InterruptedException eint) {
	    System.err.println("\nreopen():Interrupted Exception "+eint); 
	}	    
	
    }

    /* Checks telnet connection to frame
     * returns false if it is no connection or true if connected     *
     */
    private boolean isConnected() {
	try {
	    telnet.send("HI\r");		    
	    telnet.wait(prompt, 2000);
	    return true;
	} catch  (TimedOutException te) {
      	    status = "Login Time Out: " + name;
            System.err.println(status);
	    errStatus = true;
	    notifys.updateStatus(status);
	    hvmess.setConnected(false);
	    return false;
	    // System.exit(1);
        } catch (IOException e) {
	    status ="HVTelnetClient:Couldn't get I/O for the connection to:" + name;
            System.err.println(status);
	    errStatus = true;
	    hvmess.setConnected(false);
	    notifys.updateStatus(status);
	    // System.exit(-1);
	    return false;

	}	    
	
    }

    private void  reopen() {
        fgetconnection = false;
        errStatus = true;
        int cnt = 0;
	//System.out.println("in reopen():before while()");
	while ((!fgetconnection)&&(frozen)) {
            
            try {

		// close connection to server and reopen      
		telnet.disconnect();
		
		//try to open new connection
   		telnet = new TelnetWrapper(server, port);		
		telnet.setLoginPrompt(loginPrompt);
		telnet.setPasswordPrompt(passwordPrompt);
		
		System.out.println("in reopen():in try");
		if(login!=null) {
		    telnet.send("\n\r");    
		    telnet.login(login, password);
		    System.out.println("in reopen():telnet login");
		}
		
		telnet.setPrompt(prompt);
		telnet.send("\r");    
		
		telnet.wait(prompt, 10000);
		System.out.println("in reopen():wait prompt");
		// telnet.send("1450"+"\r");    
		//telnet.wait(prompt, 10000);
		Thread.sleep(100);

		String ls = telnet.sendLine("1450\r");
		System.out.println("sendLine1:"+ls);
		//telnet.send("\r");

		Thread.sleep(100);
		
		ls = telnet.sendLine("1450\r"); // 06 June 2005
		//System.out.println("sendLine2:"+ls);

		Thread.sleep(100);
		telnet.send("\r");    
		telnet.wait("1450"+prompt, 10000);
		System.out.println("wait prompt1450");
		
		telnet.send("HI\r");	 
		telnet.wait(prompt, 10000);
		
		hvmess.setConnected(true);
		
		fgetconnection = true;
                errStatus = false;
		return;

	    } catch  (TimedOutException te) {
		status = "in reopen(): Time Out: " + name;
		System.err.println(status);
		errStatus = true;
		notifys.updateStatus(status);	
		System.err.println("I'm:"+Thread.currentThread().getName());

		// System.exit(1);
	    } catch (IOException e) {
		status ="HVTelnetClient:reopen():Couldn't get I/O for the connection to:" + name;
		System.err.println(status);
		errStatus = true;
		notifys.updateStatus(status);		
		System.err.println("I'm:"+Thread.currentThread().getName());
		// System.exit(1);
	    }	    catch (Exception ee) {
	    }


            try{
                sleep(sleepToReopen); // waiting 10 sec
            } catch (InterruptedException eint) {
                System.err.println("\nreopen():Interrupted Exception "+eint); 
            }	    
	    
	}
	 
    }

    public  String getStatus() {
	//	notifyAll();
	return status ;
    }

    public boolean getErrStatus() {
	//notifyAll();
	return errStatus ;
    }

    public void setFrozen(boolean setfr) {
	frozen = setfr;
	//	notifyAll();
   }
    
    public void run() {
	String fromUser=null;
	String toUser=null;
	String blank = new String(" ");
	String cont = new String("C");
	int   c = 0;
	boolean nextmess = false;
	frozen = true;
	String threadName=Thread.currentThread().getName();

	do {
            if(!hvmess.isConnected()) { 
		//try to reopen connection
		//		if(f.hvmon.isAlive()) f.hvmon.stopMonitor();
		//		notifys.updateStatus("INFO :HVTelnetClient:Check Monitor -> "+threadName+" isAlive()="+f.hvmon.isAlive() );
		f.hvmon.suspendMonitor(threadName);
                reopen(); //stay a while get connection..or thread is stopped

		f.hvmon.resumeMonitor(threadName);
		//		f.hvmon.start();
		//                notifys.updateStatus("HVTelnetClient: Start Monitor for : "+name);      
		notifys.updateStatus("INFO :HVTelnetClient:Check Monitor -> "+threadName+" isAlive()="+f.hvmon.isAlive() );
                notifys.updateStatus("HVTelnetClient: Reopen connection to: "+name);      
                System.out.println("HVTelnetClient: Reopen connection to:"+name);
            }
	    try { 
		fromUser=hvmess.get();	    
		fromUser=fromUser.concat("\r");	    
		System.out.println("Client to "+ name + ":" + fromUser);
		
		// send message from user to the server
		String ls = telnet.sendLine(fromUser, timeout);
		// get ressponse(s) from server	    
		//loop on response string
		
		StringTokenizer st = new StringTokenizer(ls,"\r");
		int n = st.countTokens();
		Vector v = new Vector();
		while (st.hasMoreTokens()) {
		    String ps = st.nextToken();
		    ps = ps.trim();
		    v.add(ps);
		    //System.out.println("("+ps+")");
		}
			
		// convert messages to HVCommand format
		// get error number from prompt(last token)
		String prmt = (String) v.lastElement();
		StringTokenizer pt = new StringTokenizer(prmt,"\\");
		String serr = new String();
		if (pt.hasMoreTokens()) serr = pt.nextToken();
		int nrepl = v.size()-1; 
		String begin = blank;
		if (nrepl>1) begin = cont;
		
		String bs = begin;
		// prepare error code for inserting to replay
		for(int k=0; k < (5-serr.length()) ;k++) {
		    bs = bs+blank;
		}
		bs = bs + serr + blank;
		for(int i=0; i < nrepl;i++) {
		    toUser = (String) v.get(i);
		    toUser = bs.concat(toUser);
		    if(i==(nrepl-1)) toUser=" "+toUser.substring(1,toUser.length());
		    
		    toUser = toUser.concat("\0");	    
		    //System.out.println(toUser);
		    if(!hvmess.put(toUser)) {
			//System.out.println("break");
			break;
		    }
  		}

	    } catch (IOException ioe) {
		System.err.println("HVTelnetClient: IOException");
		notifys.updateStatus("ALARM : HVTelnetClient: NO response from: "+name);
		fgetconnection = false;
		hvmess.setConnected(false);
		boolean b = f.checkClientStatus();
	    } catch (InterruptedException ex) { // propagate
		//******** 9-Oct-2005 UNsafe  call!!!
		//Thread.currentThread().interrupt();

		System.out.println("HVTelnetClient:InterruptedException");		
		//Thread.currentThread().dumpStack();
		System.err.println("I'm:"+threadName);
		//		errStatus = false;
		//		hvmess.setConnected(false);
		
		boolean chkMon = f.hvmon.isAlive();
		notifys.updateStatus("INFO :HVTelnetClient:Check Monitor -> "+threadName+" isAlive()="+chkMon );
		if(chkMon) f.hvmon.resumeMonitor(threadName);
		else {
		    f.startMonitor(f.monitorDelay);
		    notifys.updateStatus("INFO :HVTelnetClient:Starting Monitor -> "+threadName+" isAlive()="+f.hvmon.isAlive() );
		    if(f.hvmon.isAlive()) {
			f.hvmon.resumeMonitor(threadName);
			System.out.println("HVTelnetClient:Restarting Monitor -> "+threadName);		
			notifys.updateStatus("INFO :HVTelnetClient:Restarting Monitor -> "+threadName);
		    } else {
			
			notifys.updateStatus("ERROR :HVTelnetClient:Couldn't start Monitor -> "+threadName+" isAlive()="+f.hvmon.isAlive());
		    }
		}
		fgetconnection=isConnected();
		boolean b = f.checkClientStatus();
		//Thread.currentThread().yield();
		
	    } catch (NumberFormatException ne) {
		System.out.println("HVTelnetClient:NumberFormatException");
	    }	    catch (Exception ee) {
	    }

	    

	} while (frozen);
    } // end run() method 
    
 
    // stop thread and close connection
    public void stopClient() {
 	frozen = false;
	try {
	    telnet.send("quit"+"\r");
	    telnet.send("^]"+"\r");
	    telnet.send("quit"+"\r");
	    telnet.disconnect();
					
	} catch (IOException e) {
	    System.err.println("Couldn't close I/O for the connection:" +name);
	    //System.exit(-1);
	}
	    // clean up
	    hvmess.setConnected(false);
	    boolean b = f.checkClientStatus();

    }

    public void close() throws IOException {
 	frozen = false;
	if(fgetconnection) { 
	try {
	    this.stop();
	    telnet.send("quit"+"\r");
	    telnet.send("\n\r");
	    telnet.send("^]"+"\r");
	    telnet.send("quit"+"\r");
	    telnet.disconnect();
	} catch (NullPointerException ne) {
	} catch (IOException e) {
	    System.err.println("Couldn't close I/O for the connection:" +name);
	    //System.exit(1);
	}

	// clean up
	boolean b = f.checkClientStatus();
	hvmess.setConnected(false);

	}
    } // end close() method

    protected void finalize() throws Throwable {
        close();
    }
    
}







