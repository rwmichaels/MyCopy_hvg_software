/* 
 * Update 05-Jun-2014: replaced 'server=f.host' in constructor with 'server=f.name' due to
 *                     changing 'f.host' from just hostname to 'hostname:port' early.
 */
package hvframe;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import hvtools.*;

/* HVclient - this class runs as thread, makes connection to server
 *  (high voltage  mainframe) by TCP/IP protocol
 * and transfer messages to this host and receives responses from it
 * @version 1.1 
 * Last update: 17-May-01
 *              14-Nov-01 add  methods isConnected() and setConnected() from hvmessage 
 *              05-Feb-02 change initialization parameters
 */
public class HVclient extends Thread  {
    Socket hvSocket = null;
    PrintWriter hvout = null;
    BufferedReader hvin = null;
    String command = null;
    private HVMessage hvmess;
    boolean frozen = false;
    public boolean flag = false;
    private String server = null;
    private int port = 0;
    private String status = new String("OK");
    private boolean errStatus = false;
    boolean fgetconnection = false;
    HVListener notifys ;
    private long timeout = 30000; //timeout for Socket.read() in mSec
    HVframe f;

    // constructor 
    public HVclient(HVframe f) {
	super(f.host);
    }
    
  // constructor 
    public HVclient(HVframe f,
		    HVListener notifys) {
	super(f.host);
	this.f = f;
	this.hvmess = f.mess ;	
	/***	server = f.host;  05-Jun-2014 ***/
	server = f.name; /***/
	port = f.port;
	this.notifys = notifys;
	hvmess.setConnected(false);
	hvmess.setTimeOut(timeout);
	// init connection to server
	try {
	    hvSocket = new Socket(server, port);
	    hvout = new PrintWriter(hvSocket.getOutputStream());
            hvin = new BufferedReader(new InputStreamReader(hvSocket.getInputStream()));
	    hvmess.setConnected(true);
	    // hvSocket.setSoTimeout((int) timeout);
       } catch (UnknownHostException e) {
	    status = "Don't know about host: " + server+":" +port;
            System.err.println(status);
	    errStatus = true;
	    notifys.updateStatus(status);
	    // System.exit(1);
        } catch (IOException e) {
	    status ="Couldn't get I/O for the connection to:" + server;
            System.err.println(status);
	    errStatus = true;
	    notifys.updateStatus(status);
	    // System.exit(1);
        }


	//System.out.println("Sock:"+hvSocket+"\n  out:"+hvout+"\n  in:"+hvin);
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
    
    private void  reopen() {
	fgetconnection = false;
	errStatus = true;
	int cnt = 0;
	// close connection to server and reopen
	try {
	    close() ;
	} catch (IOException e) {
            System.err.println("Couldn't close the connection to:" + server);
            System.exit(1);
        }
	while ((!fgetconnection)&&(frozen)) {
	    //try to open new connection
	    try {
		hvSocket = new Socket(server, port);
		hvout = new PrintWriter(hvSocket.getOutputStream());
		hvin = new BufferedReader(new InputStreamReader(hvSocket.getInputStream()));
		fgetconnection = true;
		errStatus = false;
		hvmess.setConnected(true);
		//boolean b = f.checkClientStatus();
		//hvSocket.setSoTimeout((int) timeout);			    
	    } catch (UnknownHostException e) {
		System.err.println("Don't know about host:" + server);
		//		System.exit(1);
	    } catch (IOException e) {
		System.err.println("Couldn't get I/O for the connection to:" + server);
		cnt ++;
		if(cnt>9) {
		    System.err.println("Couldn't get I/O for the connection to:"+server +" after "+cnt+" attemps");
		    notifys.updateStatus("Couldn't get I/O for the connection to:"+server +" after "+cnt+" attemp");	
		    cnt =0;
		    //frozen = false;
		    //System.exit(1);
		}
	    }
	    try{
		sleep(10000);
	    } catch (InterruptedException eint) {
		System.err.println("\nInterrupt Exception "+eint); 
	    }
	    
	}
    }
    
    
    public void run() {
	String fromUser=null;
	String toUser=null;
	String s ="";
	int   c = 0;
	boolean nextmess = false;
	long cnt = 0;
	boolean fexit =false;
	frozen = true;
	//	    while (fromUser != null) {
	do {
	    
	    if(!hvmess.isConnected()) { //try to reopen connection
		reopen(); // stay a while get connection... or thread is stoped		
		notifys.updateStatus("HVclient: Reopen connection to: "+server);	   
		System.out.println("HVclient: Reopen connection to: "+server);
	    }

	    try { 
		fromUser=hvmess.get();	    
		
		//    notifys.updateStatus("Client: " + fromUser);	
		fromUser=fromUser.concat("\0");	    
		System.out.println("Client to "+ server + ":" + fromUser);

		// send message from user to server
		hvout.print(fromUser); // 'println' method adds endline character 0x0A
		//hvout.flush();   // after 'print' method the flush() method is used
		if(hvout.checkError()) System.err.println("ERROR in hvclient.hvout.print()"); 
		// get ressponse(s) from server	    
		
		//	    System.out.println("Waiting response");
		StringBuffer sb = new StringBuffer();
	    do {
		// loop on multiple response
		do {
		    cnt = 0;
		    fexit =false;
		    //loop on response string

		    try {
			while(!hvin.ready()) {			    
			    //System.out.println("("+cnt+")hvin - not ready...");
			    //System.out.println("SBUF:<"+sb+">");
			    try{
				sleep(5);
			    } catch (InterruptedException eint) {
				System.err.println("\n Interrupt Exception "+eint); 
			    }
			    cnt++;
			    if(cnt>2000) {
				cnt = 0;	
				errStatus = true;
				//frozen = false;
				notifys.updateStatus("ALARM : HVclient: NO response from server: "+server);
				hvmess.setConnected(false);
				boolean b = f.checkClientStatus();
				//hvmess.put(" 5000 ERROR: No connection"); //unlock responce to monitor
				
				c = 0;
			    }  
			    if(!frozen || !hvmess.isConnected()) break;
			} 
		      
			if(frozen&&hvmess.isConnected()) c = hvin.read();
		    } //catch (SocketTimeoutException es) { } 
		    catch (IOException e) {
			System.err.println("Couldn't get I/O in.read()");
			errStatus = true;
			//frozen = false;
			hvmess.setConnected(false);
			boolean b = f.checkClientStatus();
		    } 
		    
		
		    //System.out.print(" :" + String.valueOf(c) );
		    s = String.valueOf((char )c);	      
		    sb = sb.append(s);
		} while ((c!=0)&&frozen);
		
		if (frozen&&hvmess.isConnected()) {
		    toUser=sb.toString();
		    sb = sb.delete(0,sb.length());
		
		    nextmess = false;
		
		    if(toUser.startsWith("C")) nextmess = true;

		    if(!hvmess.put(toUser)) nextmess = false; 
		}
	    

	    } while (nextmess&&frozen&&hvmess.isConnected());

	    } catch (InterruptedException ex) {
		
	    }
	    
	    catch (NullPointerException en) {
		System.out.println("Client:NullPointerException");
	    }
	    
	} while (frozen);
    } // end run() method 
    
 
 // stop thread and close connection
    public void stopClient() {
 	frozen = false;
 	hvout.close();
	try {
	    hvin.close();
	    hvSocket.close();
	    hvmess.setConnected(false);
	    boolean b = f.checkClientStatus();
					
	} catch (IOException e) {
	    System.err.println("Couldn't close I/O for the connection:" +hvSocket);
	    //System.exit(1);
	}
  }

    public void close() throws IOException {
	hvout.close();
	try {
	    hvin.close();
	    hvSocket.close();
	    hvmess.setConnected(false);
	    boolean b = f.checkClientStatus();
} catch (IOException e) {
	    System.err.println("Couldn't close I/O for the connection:" +hvSocket);
	    //System.exit(1);
	}
	
    } // end close() method
    
}
