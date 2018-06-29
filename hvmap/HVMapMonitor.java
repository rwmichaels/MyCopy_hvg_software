// Last update: 03-Sept-05
package hvmap;

import java.util.*;
import java.lang.*;
import hvtools.*;
import hvframe.*;

/* HVmapMonitor - impelements monitor task to wach hvchannels data updates and
 * hvmainframe status (hv0n/hvOff) changing. Then updates correspnded data in HVmapTable object.
 * One monitor is used for all maps 
 * @version 2.0
 * Last update: 03-Sept-05 : changed type of returned by getModuleSlot() to String for
 *                           support submodule addressing in form: S11S0,S11S1,.. 
 */
public class HVMapMonitor extends Thread { 
    final int delay;
    private HVMapTable map;
    private boolean stopm = false;
    private Vector GSnumbers = new Vector();
    private Vector LSnumbers = new Vector();
    private Hashtable hvft = new Hashtable();
    private Hashtable hvmt = new Hashtable();
   
    public HVMapMonitor(Hashtable hvft, Hashtable hvmt, int delay ) {	
	this.delay = delay;
	this.hvft = hvft; 
	this.hvmt = hvmt; 
  
	// get GS and LS nunmbers from hvframes
        for (Enumeration e = hvft.elements() ; e.hasMoreElements() ;) {
            HVframe f =(HVframe)e.nextElement() ;
	    GSnumbers.addElement((String)f.getGSnumber());
	    LSnumbers.addElement((String)f.getLSnumber());
	}
	

    }

    // update channels for all maps
    public void updateMapChannels(String addr) {
        for (Enumeration e = hvmt.elements() ; e.hasMoreElements() ;) {
            map =(HVMapTable)e.nextElement() ;
	    // System.out.println(map.getMapName()+":HVmapMonitor:update:"+addr);
	    map.updateChannel(addr);

	}
	
    }

    public void stopMonitor() {
	stopm = true;
    }
    
    public void run() {
        while (!stopm) {
	    int indf = 0;
	    for (Enumeration e = hvft.elements() ; e.hasMoreElements() ;) {
		HVframe f =(HVframe)e.nextElement() ;
		String newGS = f.getGSnumber();
		String oldGS =(String) GSnumbers.get(indf);
		if(!newGS.equalsIgnoreCase(oldGS)) {
		    // check what number is different
		    StringTokenizer gsn = new StringTokenizer(newGS);
		    StringTokenizer gso = new StringTokenizer(oldGS);
		    int igs = 0;
		    while ((gsn.hasMoreTokens())&(gso.hasMoreTokens())) {
			String sval = gsn.nextToken();
			if(!sval.equalsIgnoreCase(gso.nextToken())) {
			    if(igs==0) {
				// check measured LSnumber
				update(0, f, indf);
			    } 
			    if(igs==1) {
				// check settable LSnumber
				//	System.out.println(map.getMapName()+":HVmapMonitor:update:"+f.name+":index:"+indf);
				update(1, f, indf);
			    } 
			    
			    if(igs==2) {
				// check HVstatus
				updateHVstatus(f);
				// System.out.println(map.getMapName()+":HVmapMonitor:updateHVstatus:"+f.name+":index:"+indf);
			
				if(f.HVONstatus) {
				
				} else { 
				
				}
				
			    }
			}
			igs++;
		    }
		    
		    // update GS summary numbers
		    GSnumbers.set(indf, newGS);
		}
		indf++;
	    }
	    	    
	    try {
		//        sleep(100+(int)(Math.random() * 3000));
		sleep(delay);
		// if (threadSuspended) {
		//  synchronized(this) {
		//      while (threadSuspended)
		//          wait();
		//  }
	    } catch (InterruptedException ei) { }
	}
    }
    

    private void updateHVstatus(HVframe f) {
	for(int id = 0; id<f.nunit;id++ ) {
	    int nc = f.hvm[id].nchn;
//**** 3-Sept-2005, returns String instead of integer
	    //	    int slot = f.getModuleSlot(id); 
	    String slot = f.getModuleSlot(id); 
	    String pdelimiter = new String(".");
	    String sl = "*";
	    if(slot!=null) sl = slot;
//****
	    for (int i=0 ; i<nc;i++) {
		String addr = f.host +pdelimiter +sl +pdelimiter +i;
		//System.out.println("HVmapMonitor:updatechannels:"+addr);
		
//		map.updateChannel(addr);
		updateMapChannels(addr);
		//	f.hvm[id].table.myModel.updateColumn(iprop+1);
	    }		

	}
    }

    private void update(int ishft, HVframe f, int find) {
        //find what LS summary numbers are different
        String lsold =  (String) LSnumbers.get(find);	
        String lsnew =  f.getLSnumber();
        StringTokenizer lso = new StringTokenizer(lsold);
        StringTokenizer lsn = new StringTokenizer(lsnew);
        int ils = 0;  // index of LS summary number
        while ((lsn.hasMoreTokens())&(lso.hasMoreTokens())) {
            String lsval = lsn.nextToken();
            if(!lsval.equalsIgnoreCase(lso.nextToken())) {
		// calculate for what number of logical unit LS is differnt
                int id = Math.round(ils/2); // index of logical unit
	        String pdelimiter = new String(".");
		int nc = f.hvm[id].nchn;
//**** 3-Sept-2005, returns String instead of integer
		String slot = f.getModuleSlot(id); 
		String sl = "*";
		if(slot!=null) sl = slot; 
//****
		for (int i=0 ; i<nc;i++) {
		    String addr = f.host +pdelimiter +sl +pdelimiter +i;
//		    map.updateChannel(addr);
		updateMapChannels(addr);
		    //	f.hvm[id].table.myModel.updateColumn(iprop+1);
		}		
	    }

            ils++;
        }
        //update LS summary number 
        LSnumbers.set(find, lsnew);
    }
    
        
    
}




















