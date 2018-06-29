
package hvserver;

import java.net.*;
import java.io.*;
import java.util.*;

import hvtools.*;
/**
 * HVServerThread - implements thread to receive external command messages and 
 * send responses. Command message and response are strings of ASCII chars 
 * null terminated with ('\0').
 * @version 2.0
 * Last update: 04-Aug-05
 * Mod&Add: 04-Aug-05 changing parameter (instead hvmap Vector, we using hvmt Hashtable with stored HVMapTables)  
 */
public class HVServerThread extends Thread {
    private Socket socket = null;
    private HVServerProtocol hvsp;
    private HVListener notify;
    public HVServerThread(Socket socket, Hashtable hvft, Hashtable hvmt, HVListener notify) {
	super("HVServerThread");
	this.socket = socket;	
	this.notify = notify;
	hvsp = new HVServerProtocol(hvft, hvmt, notify);
    }

    public void run() {
	try {
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
						   new InputStreamReader(
	       				 socket.getInputStream()));

	    String inputLine, outputLine;
	    StringBuffer sb = new StringBuffer();
	    boolean nextoutput = false;
	    int c = 0;
	    notify.updateStatus("INFO :Get connection from: "+socket.getInetAddress().getHostName()+" ("+
				socket.getInetAddress().getHostAddress()+")");

	    do {
	    	c = in.read(); 
		//System.out.print(": " + String.valueOf(c) );		
		sb = sb.append( String.valueOf((char )c));
	    } while ((c!=0) && (c!=0xA));
	   
	    inputLine=sb.toString();

	    sb = sb.delete(0,sb.length());	    

	    //inputLine = in.readLine(); // read incoming string of message

	    while(hvsp.next())
	    {
		outputLine = hvsp.processInput(inputLine);
		out.print(outputLine); 
		//out.println(outputLine); 
		if(out.checkError()) {
		    System.err.println("ERROR: HVServer: in out. print");	    	    
		}
	    }

	    out.close();
	    in.close();
	    socket.close();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}



