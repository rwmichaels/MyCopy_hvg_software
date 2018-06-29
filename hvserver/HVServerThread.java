package hvserver;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
 
import hvtools.*;
/**
 * HVServerThread - implements thread to receive external command messages and 
 * send responses. Command message and response are strings of ASCII chars 
 * null terminated with ('\0').
 * @version 3.1
 * Last update: 04-Apr-14
 * Mod&Add: 04-Aug-05 changing parameter (instead hvmap Vector, we using hvmt Hashtable with stored HVMapTables)  
 * Mod&Add: 04-Apr-2014 set limit for receiving buffer 'sb'  max of 256 chars (MaxInBuff) with null terminator.
 * Mod&Add: 17-Apr-2014 add 'finaly' block to close connection and cleanup all buffers. 
 */
public class HVServerThread extends Thread {
    private Socket socket = null;
    private HVServerProtocol hvsp;
    private HVListener notify;
    private  PrintWriter out=null;
    private BufferedReader in=null;
    private StringBuffer sb=null;
    int MaxInBuff=256; //04-Apr-2014
    
    public HVServerThread(Socket socket, Hashtable hvft, Hashtable hvmt, HVListener notify) {
	super("HVServerThread");
	this.socket = socket;	
	this.notify = notify;
	hvsp = new HVServerProtocol(hvft, hvmt, notify);
    }

    public void run() {
	try {

	    out = new PrintWriter(socket.getOutputStream(), true);
	    in = new BufferedReader(
				     new InputStreamReader(socket.getInputStream()));	    

	    String inputLine, outputLine, HostName, HostIP;
	    sb = new StringBuffer();
	    boolean nextoutput = false;
	    int c = 0;
	    

	    HostName = socket.getInetAddress().getHostName();
	    HostIP = socket.getInetAddress().getHostAddress();
	    String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()); 
	    if(notify!=null)
		notify.updateStatus("INFO : Get connection from: " +HostName +" (" +HostIP +"): " +timeStamp);
	    
	    if(in!=null)
	    do {
		c = in.read(); 
		//System.out.print(": " + String.valueOf(c) );			      
		//System.out.print(": " + String.valueOf(c)+"(" + String.valueOf(sb.length())+")" );			   
		
		sb = sb.append( String.valueOf((char )c));
		//	    } while ((c!=0) && (c!=0xA) && (sb.length()<MaxInBuff)); //** 04-Apr-2014
	    } while ((c>0) && (c!=0xA) && (sb.length()<MaxInBuff)); //** 08-Apr-2014
	    
	    //	    System.out.println("\n<out of loop>");
	    
	    if(sb.length()<MaxInBuff) { //** 17-Apr-2014 
		inputLine=sb.toString();
		sb = sb.delete(0,sb.length());	    
		//inputLine = in.readLine(); // read incoming string of message
		while(hvsp.next())
		    {
			outputLine = hvsp.processInput(inputLine);
			if((outputLine!=null) && (out!=null)) {
			    out.print(outputLine); 
			    //out.println(outputLine); 
			    if(out.checkError()) {
				System.err.println("ERROR: HVServerThread: in out.print()");	    	    
			    }
			}
		    }
		
	    } else { //** 17-Apr-2014
		System.err.println("\nDate: "+timeStamp);
		System.err.println("ERROR: HVServerThread: input request is more then "+MaxInBuff +" symbols");
		out.print("ERROR: input request exceeds of symbols limit \0");		
		if(out.checkError()) {
		    System.err.println("ERROR: HVServerThread: in out.print()");	    	    
		}
	    }
	    timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
	    
	    System.err.println("INFO : Connection closed: " +HostName +" (" +HostIP +"): " +timeStamp);
	    if(notify!=null)
		notify.updateStatus("INFO : Connection closed: "+HostName +" (" +HostIP +"): "+timeStamp);
	    inputLine=null;
	    outputLine=null;
	    timeStamp=null;

	} catch (IOException e) {
	    e.printStackTrace();
	    //	    socket=null; //** 04-Apr-2014
	    
	} finally { //** 17-Apr-2014
	    	    
            if(in != null) {
                try {
		    in.close();
                } catch (IOException e) {
		    e.printStackTrace();
                }
	    }
	    if(out != null) {
		out.close();
  	    }
            if(socket != null) {
                try {
		    socket.close();
		    //		     System.out.println("\n<socket closed>");
                } catch (IOException e) {
		    e.printStackTrace();
                }
	    }
	    
	    sb=null;
	    in=null; //** 04-Apr-2014
	    out=null; //** 04-Apr-2014
	    socket=null; //** 04-Apr-2014
	}
    }
}


