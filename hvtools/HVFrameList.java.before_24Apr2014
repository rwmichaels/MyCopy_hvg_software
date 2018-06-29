package hvtools;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;

/**
 * Implements methods to manipulate with list of hvframe hostnames 
 * @version 1.1
 * Last update: 27-Feb-2002 add methods getToken() and regionMatches()
 */
public class HVFrameList extends Parameters {
    private int defaultNum = 1;
    public String defaultPort = "1090";
    public String defaultProtocol = "tcp";
     public String defaultUser = "[telnetuser]";
     public String defaultPassword = "[telnetpassword]";
   
    public String defaultName = "hostname"+":portnumber" +":"+
	defaultProtocol ; //+ ":"+ defaultUser+":"+defaultPassword;
    private boolean loadstatus = false;
    private int nFrames = 0;
    private Vector hostnameList = new Vector();
    private String numFrames = "num.frames";
    private String nameFrames = "name.frames";

    /**
     * Constructor init and load parameters from file HVframes.conf
     */
    public HVFrameList() {
	super("HVframes.conf", "HVFrame Hosts List");	
      loadstatus = getParameters();
    }
    
    /**
     * Returns status of loaded parameters.
     * @return boolean if true - parameters loaded, else false
     */   
    public boolean getStatus() {
	return loadstatus;
    }  
    
    /**
     * Setup parameters values to default.
     * @param defaults Properties
     */  
    protected void setDefaults(Properties defaults) {
        defaults.put(numFrames, new Integer(defaultNum).toString() );
	defaults.put(nameFrames, defaultName);
    }
	
    /**
     * Gets properties values from file
     */	
    protected void updateSettingsFromProperties() {
        try {
            String tmp;
	    String names;
	    // get number of frames
            tmp = properties.getProperty(numFrames);
            nFrames = Integer.parseInt(tmp);
	    // get hostnames of HVframes
	    names = properties.getProperty(nameFrames);
	    hostnameList = parseList(names);
	    
        } catch (NumberFormatException e) {
            // we don't care if the property was of the wrong format,
            // they've all got default values. So catch the exception
            // and keep going.
        }
    }

    /**
     * Store properties value in file
     */	
    protected void updatePropertiesFromSettings() {
	String names = "";
	String ports = "";
	
        properties.put(numFrames,
                       new Integer(nFrames).toString());
	nFrames = hostnameList.size();
	for(int i=0; i<nFrames; i++) {
	    names = names+(String)hostnameList.get(i) +" "; 
	}
        properties.put(nameFrames,
                       names);
    }
    
    /**
     * Sets number of mainframe hosts to the property nFrames
     * @param hostnum int number of hvframes
     */
    public void setHostNum(int hostnum) {
	this.nFrames = hostnum;
    }

   /**
     * Returnss number of mainframe hosts from property vriable nFrames
     * @return int number of hvframes
     */
     public  int getHostNum() {
	return nFrames;
    }
 
     
    /**
     * Sets list of host names of hvframes to the property hostnameList
     * @param hostnameList Vector  hostnames list of hvframes
     */
   public  void setHostName(Vector hostnameList) {
	this.hostnameList = hostnameList;
	nFrames = hostnameList.size();
	saveParameters();
    }
    
    

    /**
     * Returns list of host names of hvframes from property hostnameList
     * @return Vector  hostnames list of hvframes
     */
    public  Vector getHostName() {
	return hostnameList;
    }
    
    /**
     * Adds hostname to the hostname list property hostnameList and
     * increments number of hostname nFrames by 1.
     * @param host String hostname of added hvframe
     */ 	
     public void addHostName(String host) {
	hostnameList.add((String)host);
	nFrames++;
    }

    /**
     * Inserts the specified hostname as a component in hostnameList
     * at the specified index.
     * @param ind int index where to insert the new component.
     * @param host String the hostname to insert.
     */
     public void insertHostName(int ind, String host) {
	hostnameList.add(ind, (String)host);
	nFrames++;
    }
	
    /**
     * Tests if the specified host is a component in hostnameList.
     * @param host String hostname of tested
     * @return true if and only if the specified object is the same 
     *         as a component in hostnameList, false otherwise.
     */
     public boolean contains(String host) {
	return hostnameList.contains((String)host);
    }


    /**
     * Tests if the specified host is a component in hostnameList.
     * @param host String hostname of tested
     * @return true if and only if the specified object is match region 
     *         of a component in hostnameList, false otherwise.
     */
     public boolean regionMatches(String host) {

	for (Enumeration e = hostnameList.elements() ; e.hasMoreElements() ;) {
	    String hs =(String)e.nextElement() ;
	    if(hs.regionMatches(0,host,0,host.length())) return true;
	}
	return false;
     }


    /**
     * Overrides method Vector.elements()
     * @return 	Enumeration list of vector components
     */
    public Enumeration elements() {
	return hostnameList.elements();
    }

    /**
     * Removes the first occurrence of the specified element in 
     * hostnameList. If the hostnameList does not contain the element, 
     * it is unchanged.
     * @param host String hostname to remove.
     */
    public  void removeHostName(String host) {
	int indx;
	if(!hostnameList.isEmpty()) {
	    indx = hostnameList.indexOf((String)host);
	    hostnameList.remove(indx); 
	    nFrames--;	    
	}
    }

    /**
     * Removes the element at the specified position in hostnameList.
     * Decrement number of hostname in propery nFrame by 1
     * @param ind int the index of the hostname to removed.
     */
     public void removeByindex(int indx) {
	if(!hostnameList.isEmpty()) {
	    hostnameList.remove(indx);
	    nFrames--;
	    
	}
    }

    /**
     * Parses the string argument as a list of hostnames
     * @param theStringList String with list of hostnames 
     *   delimited by blank character
     * @return Vector with hostnames
     */
    protected static Vector parseList(String theStringList) {
        Vector v = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            v.addElement(name);
        }
        return v;
    }

    public static String getToken(int ind, String s) {
	String name = null;
	int cnt = 0;
	StringTokenizer tokenizer = new StringTokenizer(s, ":");
	if(ind <= tokenizer.countTokens()) {
	    while (tokenizer.hasMoreTokens()) {
		name = tokenizer.nextToken();
		cnt++;
		if(ind == cnt) break;	   
	    }
	} 
	return name;
    }



    /**
     * main method used only for testing 
     */
    public static void main(String arg[]) {
	Vector host = new Vector();
	host.addElement((String)"host1:1090");
	host.addElement((String)"host2:1091");
	host.addElement((String)"host3:1092");

	
	HVFrameList hvList = new HVFrameList();
	System.out.println("nhost:"+hvList.getHostNum());
	System.out.println("name:"+hvList.getHostName());

	//hvList.setHostNum(3);
	//hvList.setHostName(host);
	
	for(int i=0;i<hvList.getHostNum();i++) {
	    System.out.println((String)hvList.getHostName().get(i));
	}
    }
}

