/*  HVServer - implement server to listening and proccesing messages
 *  and requests  from external hosts 
 *   
 *  @version 2.0
 *  Last update: 04-Aug-05
 *  Mod&Add: 04-Aug-05 changing parameter (instead hvmap Vector, we using hvmt Hashtable with stored HVMapTables)  
 */

package hvserver;

import java.net.*;
import java.io.*;
import java.util.*;

import hvtools.*;

public class HVServer extends Thread {
    int serverPort = 5555;
    boolean listening = true;
    ServerSocket serverSocket = null;
    private Hashtable hvft;
    private Hashtable hvmt;
    public HVListener notify;

    public HVServer(int port, Hashtable hvft, Hashtable hvmt) {       
	super("HVServer");
	serverPort = port; 
	this.hvft = hvft;
	this.hvmt = hvmt;

    }
     public HVServer(int port, Hashtable hvft, Hashtable hvmt, HVListener notify) {       
	super("HVServer");
	serverPort = port; 
	this.hvft = hvft;
	this.hvmt = hvmt;
	this.notify = notify;
    }
    
    public void run() {	
	try {
	    serverSocket = new ServerSocket(serverPort);
	    while (listening)
		new HVServerThread(serverSocket.accept(), hvft, hvmt, notify).start();
	    serverSocket.close();
	} catch (IOException e) {
	    System.err.println("Could not listen on port: "+serverPort);
	    //	System.exit(-1);
	}
	
    }
}


