
package hvframe;


/* HVMessage - implements methods to synchronize stream of messages and replies 
 * between sender and HVclient
 * 
 * @version 1.1
 * Last update: 17-May-01
 *             14-Nov-01 add methods isConnected()  and setConnected() that used by 
 *                          hvclient and hvmonitor   
 */
public class HVMessage {
    private String in;
    private String out;
    private boolean availableout = false;
    private boolean availablein = false;
    private long  timeout = 30000; // wait 30sec to break
    private String messageIDin = null;
    private String messageIDout = null;
    private String messageIDg = null;
    private String messageIDp = null;
    private boolean connected = false; //14-Nov-01

    /** This method used by hvmonitor to detect hvclient  connection
     */ 
    public synchronized boolean isConnected() {
	return connected;
    }

    public synchronized void setTimeOut(long t) {
	timeout = t;
    }

    public synchronized long getTimeOut() {
	return timeout;
    }



    /** This method used by hvclient to set status of connection
     */ 
    public synchronized void setConnected(boolean con) {
	connected = con;
    }

    //user wont to get message from client
    public synchronized String uget(String messID) throws InterruptedException {
	if(connected) {
        while ((availableout == false)) {
	    long waitTime = timeout;
	    long start = System.currentTimeMillis();
            try {	
                wait(waitTime);

		waitTime = timeout - (System.currentTimeMillis() - start);
		if (waitTime <= 0) {
		    clear();
		    System.out.println("uget:TimeOut:"+messID);
		    notifyAll();
		    // if(!connected) return null;
		    throw (new InterruptedException());
		}
	    } catch (InterruptedException e) { 
		System.err.println( "ERROR: timeout in thread.uget()");
		notifyAll();
		throw e;
	    }			
	}
	
        while (!messID.equals(messageIDp)) {
            try {	
                wait(timeout);
            } catch (InterruptedException e) { 
		System.err.println( "ERROR_2: timeout in thread.uget()");
		//System.exit(2);
	    }			
	}
	
	    availableout = false;
	    //System.out.println("\n ugavin:"+availablein +"\n ugavout:"+availableout);
	    notifyAll();
	    return out;
	} else {
	    return null;
	}
	
    }

    // client wont to put message for user
    public synchronized boolean put(String value) throws InterruptedException {
	if(connected) {
	    while ((availableout == true)) {
	    long waitTime = timeout;
	    long start = System.currentTimeMillis();
            try {
                wait(waitTime);
		waitTime = timeout - (System.currentTimeMillis() - start);
		if (waitTime <= 0) {
		    clear();
		    System.out.println("put:TimeOut:"+value);
		    notifyAll();
		    //if(!connected) return false;
		    throw (new InterruptedException());
		}
            } catch (InterruptedException e) {
		System.err.println( "ERROR: timeout in thread.put()");
		notifyAll();
		throw e;
	    }
        }
	
	messageIDp = messageIDg;
        out = value;
        availableout = true;
        //System.out.println("\n pavin:"+availablein +"\n pavout:"+availableout);
        notifyAll();
	return true;
	} else {
	    return false;
	}
    }

    //client wont to get message from user
    public synchronized String get() throws InterruptedException {
	if(connected) {
        while ((availablein == false)) {
	    long waitTime = timeout;
	    long start = System.currentTimeMillis();
            try {
                wait(waitTime);
		waitTime = timeout - (System.currentTimeMillis() - start);
		if (waitTime <= 0) { 
		    clear();
		    System.out.println("get:TimeOut:");
		    notifyAll();
		    //if(!connected) return null;
		    throw (new InterruptedException());
		}
            } catch (InterruptedException e) {
		System.out.println("timeout in get()"); 
		notifyAll();
		throw e;
	    }
        }
	 
	messageIDg = messageIDin;
	availablein = false;
	//System.out.println("\n gavin:"+availablein +"\n gavout:"+availableout);
	notifyAll();
	return in;
	} else {
	    return null;
	}
    }
    
    //user wont to put message for client
    public synchronized boolean uput(String value, String messID) throws InterruptedException {
	if(connected) {
        while ((availablein == true)) {
	    long waitTime = timeout;
	    long start = System.currentTimeMillis();
            try {
                wait(waitTime);
		waitTime = timeout - (System.currentTimeMillis() - start);
		if (waitTime <= 0) {
		    clear();
		    System.out.println("uput:TimeOut:"+ messID);
		    notifyAll();
		    //    if(!connected) return false;
		    throw (new InterruptedException());
		}
            } catch (InterruptedException e) {
		System.out.println("timeout in uput()"); 
		notifyAll();
		throw e;
	    }
        }

	messageIDin = messID;
        in = value;
        availablein = true;
        //System.out.println("\n upavin:"+availablein +"\n upavout:"+availableout);
        notifyAll();
	return true;
	} else {
	    return false;
	}
    }

    
    public void clear() {

    messageIDin = null;
    messageIDout = null;
    messageIDg = null;
    messageIDp = null;
    availableout = false;
    availablein = false;
	
    }

}





