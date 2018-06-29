
package hvframe;

import java.util.*;
import hvtools.*;
import javax.swing.SwingUtilities;

/**
 * HVComand - implements methods to form a queue of messages 
 * between sender and HVclient to communicate with Hvframe.
 * 
 * @version 1.3
 * Last update: 29-Oct-2005
 * Add&Mod:
 * 29-Oct-2005 add cath of  NumberFormatException and  IndexOutOfBoundsException
 */

public class HVCommand  {
    // put command string to the HVMessage obj and get response(s)
    //     from it. Response(s) are stored into the FIFO(first in - first out) obj.
    //
    public HVMessage mess = null;
    //    public Stack responseBag = new Stack();
    //public Stack errorBag = new Stack();
    public FiFo responseBag = new FiFo();
    public FiFo rawresponseBag = new FiFo();
    public FiFo errorBag = new FiFo();
    public HVListener notifys;
    public String ID = null;

   //constructor
    public HVCommand(HVMessage mess, HVListener notifys) {
	//super(false);
    	this.mess = mess;
	this.notifys = notifys;
	this.ID = ID;
    }

    // methods
    public  synchronized boolean exec(String command, String ID) {
	//Runnable doExec = new Runnable() {
	//	public void run() {

	String response = null;
        boolean next = false;
	String errorCode = null;
	boolean response1th = true;
	int indb = 0;
    
	if (!errorBag.empty()) { 
	    errorBag.clear(); 
	}
	if (!responseBag.empty()) {
	    responseBag.clear();
	}
	if (!rawresponseBag.empty()) {
	    rawresponseBag.clear();
	}

	try {	
	    //System.out.println(" Command:"+command);
	    if(mess.isConnected()) {
		
		if( mess.uput(command,ID)) {
		    response1th = true;
		    indb = 7+command.length();  
		    do {
			response = mess.uget(ID);
			
			next = false;
			if(response.startsWith("C")) {
			    next = true;	
			}
			
			//	System.out.println("next:"+next+" response:"+response);

			errorCode = response.substring(1,6);
			errorCode = errorCode.trim();
			errorBag.push((String)errorCode);
			if (Integer.parseInt(errorCode) >= 20) {
			    notifys.updateStatus(ID+":ERROR - #"+ errorCode+" Command :'"
						 +command+"'" + " Response :" + response.trim());
			}
			rawresponseBag.push((String)response);
			response = response.substring(indb);
			response = response.trim();
			responseBag.push((String)response);
			//System.out.println("Response:"+ID + ":"+response);
			//System.out.println("status:"+ID+":" +errorCode);
			response1th = false;
			indb = 7;
		    } while (next);
		    //	}
		    //    };
		    
		    return true;
		    //SwingUtilities.invokeLater(doExec);
		} else {
		    clearResponse();
		    clearRawResponse();
		    return false;
		}
		    
	    } else {
		
		clearResponse();
		clearRawResponse();
		return false;
	    }
      
	} catch (InterruptedException ie) {
	    outMess("ERROR :HVComand:InterruptedException");
	    clearResponse();
	    clearRawResponse();
	    return false;
	} catch (NullPointerException ne) {
	    outMess("ERROR :HVComand:NullPointerException");
	    clearResponse();
	    clearRawResponse();
	    return false;      

	} catch (NumberFormatException nfe) {
	    outMess("ERROR :HVComand:NumberFormatException");
	    clearResponse();
	    clearRawResponse();
	    return false;
	
	} catch (IndexOutOfBoundsException ine) {
	    outMess("ERROR :HVComand:IndexOutOfBoundsException Exception");
	    clearResponse();
	    clearRawResponse();
	    return false;
	}
 
	
    }

    public String getErrorCode() {
	if (!errorBag.empty()) {
	    return (String)errorBag.pop();
	} else {
	    return null;
	}
    }
    
    public void clearError() {
	if (!errorBag.empty()) {
	    errorBag.clear();
	}
    }

    public synchronized String getResponse() {
	if (!responseBag.empty()) {
	    return (String)responseBag.pop();
	} else {
	    return null;
	}
    }

   
    public void clearResponse() {
	if (!responseBag.empty()) {
	    responseBag.clear();
	}
    }

     public String getRawResponse() {
	if (!rawresponseBag.empty()) {
	    return (String)rawresponseBag.pop();
	} else {
	    return null;
	}
    }

    public void clearRawResponse() {
	if (!rawresponseBag.empty()) {
	    rawresponseBag.clear();
	}
    }

    public boolean isRawEmpty() {
	return rawresponseBag.empty();
    }
    
    public synchronized boolean isEmpty() {
	return responseBag.empty();
    }
    
    public void outMess(String mess) {
	    notifys.updateStatus(mess);
	    System.out.println(mess);
    }


    public class FiFo extends Vector {

	public FiFo() {
	    super();
	}
	
	public void push(Object ob) {
	    this.addElement(ob);
	}

	public Object pop() {
	    return this.remove(0) ;
	}

	public boolean empty() {
	    return this.isEmpty() ;
	}
    }
}







