/**
 * HVmonitor - implement methods to updating channels and table data 
 *             if its have changed in mainframe. 
 *             Summary numbers changing is monitoring with time period ~3sec
 *
 * @version 1.1
 * Last update: 17-May-01
 *              04-Oct-01, remove stuck if hvclient stops 
 *
 *              16-Oct-05, add status update with INFO message when monitor stops or resumes 
 *              26-Oct-05, add parameter 'name' type of String to suspendMonitor() and resumeMonitro() 
 *              10-Feb-14, add cheks for empty response for RC command in update() function. Print error message for empty response.
 */             
              

package hvframe;

import java.util.*;
import hvtools.*;

public class HVmonitor extends Thread implements Cmd {
    // monitoring some properties of hvmodules by sliced time
    private int delay;
    private HVMessage mess = null;
    private HVframe f = null;
    private HVCommand moncom = null;
    public String command = null;
    public String response = null;
    public String error = null;
    public boolean stopm = false;
    public String ID =null;
    public String errorCode = null;
    private boolean threadSuspended = false;
    private int nunit = 0;
    private static long lcount = 0;

    public HVListener notifys;

    public HVmonitor(String ID, HVMessage mess, int delay, HVframe f, HVListener notifys) {
 	super(ID);	
	this.delay = delay;
	this.f = f; 
	this.nunit = f.nunit;
	this.mess = mess;
	this.ID = ID;
	this.moncom = new HVCommand(this.mess, notifys);
	this.command = GS;
	this.notifys = notifys;
    }
    
    public void stopMonitor() {
        resumeMonitor("Monitor");
	stopm = true;		
	//	while (this.isAlive()) 
	//	    try { 
	//		Thread.sleep(100) ;
	//	    } catch (InterruptedException ie) { }
	//	stopm=false;
	notifys.updateStatus("INFO : "+ ID+": Monitor stopped...");
    }

    public void setMonitorDelay(int delay) {
	 suspendMonitor("Monitor");
	 this.delay = delay;
	 resumeMonitor("Monitor");
    }

 
    public synchronized void suspendMonitor(String name) {
	threadSuspended = true;	
	notifys.updateStatus("INFO : "+ ID+": Monitor suspended by:"+name);
    }

    public synchronized void resumeMonitor(String name) {
	if(threadSuspended) {
	threadSuspended = false;
	notify();
	notifys.updateStatus("INFO : "+ ID+": Monitor resumed by:"+name);
	}

    }

    public void run() {
	System.out.println("Monitor:"+ID+" starting");
	
	stopm=false;
	while (!stopm) {
	    command = "GS";

	    f.checkClientStatus();
	    //	    System.out.println("Monitor:"+ID+" after checkClientStatus()");
	    //System.out.println(".........monitor");
	    if(moncom.exec(command, ID)) {
	    
	    if(!moncom.isEmpty()&&!stopm) {
		response =  moncom.getResponse();
	    }
	    // 06-Sep-05
	    //	    while(!moncom.isEmpty()&&!stopm) {
	    //	response =  moncom.getResponse();
	    //}

	    	
	    //	    System.out.println("Monitor: after"+ID+" if(!moncom)");
	    if((!stopm)&&(mess.isConnected())) {
		response = response.trim();
		//compare GSnumbers old and new
		String oldGS = new String(f.getGSnumber());
		//System.out.println("OLD:GS:"+oldGS);
		//System.out.println("NEW:GS:"+response);
		if(!response.equalsIgnoreCase(oldGS)) {
		    // check what number is different
		    StringTokenizer gsn = new StringTokenizer(response);
		    StringTokenizer gso = new StringTokenizer(oldGS);
		    int igs = 0;
		    while ((gsn.hasMoreTokens())&(gso.hasMoreTokens())) {
			String sval = gsn.nextToken();
			if(!sval.equalsIgnoreCase(gso.nextToken())) {
			    if(igs==0) {
				// check measured LSnumber
				update(0);
			    } 
			    if(igs==1) {
				// check settable LSnumber
				update(1);
			    } 
			    
			    if(igs==2) {
				// check CONFIG 
				command = CONFIG;
				// ask hvframe about configuration status
				if(moncom.exec(command,ID)) {
				// get response
				    String config = new String();
				while(!moncom.isEmpty()) {
				    config = moncom.getResponse().trim();
				}
				
				f.parseConfigWord(config);
				f.updateLabelStatus();
				}
				
				// check HVstatus
				//*command = HVSTATUS;
				// ask hvframe about current status
				//*moncom.exec(command,ID);
				// get response
				String st = new String("HV?");
				//*while(!moncom.isEmpty()) {
				//*    st = moncom.getResponse().trim();
				//*}
				// notifys.updateStatus(f.name+":"+st);
				//*if(st.equalsIgnoreCase("HVON")) {
				//*    f.setHVONstatus(true);
				//	f.HVONstatus = true;
				  
			  	//f.ftabb.setIconAt(f.ftabb.getSelectedIndex(), f.ipwron);
				//*} else { 
				//*    f.setHVONstatus(false);
				//f.HVONstatus = false;
				    //   f.offButton.setSelected(true);
			  	//f.ftabb.setIconAt(f.ftabb.getSelectedIndex(), f.ipwroff);
				//*}
				
			    }
			}
			igs++;
			if(!f.checkClientStatus()) break;
		    }
		    
		    // update GS summary numbers
		    f.setGSnumber(response);
		}
		
		//	    System.out.println("COUNT:"+ lcount++);
		
		try {
		    //        sleep(100+(int)(Math.random() * 3000));
		    System.out.println("Monitor:"+ID+" in sleep()");
		    f.setRemoteColor(f.remloc);
		    sleep(delay);
		    if (threadSuspended) {
			f.setRemoteColor(f.remc);
			synchronized(this) {

			    while (threadSuspended)
				wait();
			    //			    f.checkClientStatus();
			    sleep(delay); // delay after resume monitor
			}		
			
			
		    }
		    f.setRemoteColor(f.remhic);
		} catch (InterruptedException e) { }
	    }

	    } else { // 06-Sep-05 stop Monitor	      
		//if(!mess.isConnected()) stopMonitor();
	    
	    // wait until connection is resumes
		while(!mess.isConnected()) {
		    try {
			//        sleep(100+(int)(Math.random() * 3000));
			f.checkClientStatus();
			sleep(delay);
			if (threadSuspended) {
			    synchronized(this) {
				while (threadSuspended)
				    wait();
				
			    }
			}
		    } catch (InterruptedException e) { }
		    
		}
		//check hvstatus after reopen connection
		command = CONFIG;
		// ask hvframe about configuration status
		if(moncom.exec(command,ID)) {
		    // get response
		    String config = new String();
		    while(!moncom.isEmpty()) {
			config = moncom.getResponse().trim();
		    }
		    
		    f.parseConfigWord(config);
		    f.updateLabelStatus();
		    
		}
	    }//if(0)

	} 
	
    }


    private void update(int ishft) {
	System.out.println("Shift:"+ishft);
	command = "LS";
	String lsresponse = null;
	if(moncom.exec(command, ID)) {
	// get response
	    lsresponse =  moncom.getResponse();
	    lsresponse = lsresponse.trim();
	    // chek if more responses present in FIFO (for LS command only)
	    while(!moncom.isEmpty()) {
		lsresponse = lsresponse +" "+
		    moncom.getResponse().trim();
	    } 
	} else { 
	    return;
	}				
	
	//find what LS summary number are different
	StringTokenizer lsn = new StringTokenizer(lsresponse);
	StringTokenizer lso = new StringTokenizer(f.getLSnumber());
	int ils = 0;  // index of LS summary number
	String psresponse = null;
	String vresponse = null;
	int id = 0;
	
	//	System.out.println("old:LS:"+lsresponse);
	//System.out.println("new:LS:"+f.getLSnumber());

	while ((lsn.hasMoreTokens())&(lso.hasMoreTokens())) {
	    String lsval = lsn.nextToken();
	    if(!lsval.equalsIgnoreCase(lso.nextToken())) {
		// calculate for what number of logical unit LS is differnt
		id = Math.round(ils/2); // index of logical unit;
		// check what property need to udate
		command = "PS "+"L"+id;
		if(moncom.exec(command, ID)) {
		    while(!moncom.isEmpty()) {
			psresponse = moncom.getResponse().trim();
		    } 
		} else {
		    return;
		}
		//find what properties sum.numbers are different
		StringTokenizer psn = new StringTokenizer(psresponse);
		StringTokenizer pso = new StringTokenizer(f.getPSnumber(id));
		int iprop = 0; // properties summary number index 
		while ((psn.hasMoreTokens())&(pso.hasMoreTokens())) {
		    String psval = psn.nextToken();
		    if(!psval.equalsIgnoreCase(pso.nextToken())) {
			// get name of property that has changed
			//System.out.println(" GetParameterName id,iprop"+id+" "+iprop); 
			String prop = f.hvm[id].getParameterName(iprop);
			// get new value of property from frame and update table data
			command = "RC "+"L"+id+" "+prop;
			if(moncom.exec(command, ID)) {
			    while(!moncom.isEmpty()) {
				vresponse = moncom.getResponse().trim();
			    }
			} else { 
			    return;
			}
			// *** 10-Feb-2014
			if(vresponse!="") {
			    // update channels data
			    f.hvm[id].channelsUpdate(prop,vresponse);
			    // System.out.println("LU:"+id+" Prop:"+prop+" Resp:"+vresponse);			
			    if(iprop >= 0) {
				f.hvm[id].table.myModel.updateColumn(iprop+1);
			    }
			} else { // do not update
			    System.out.println("Error : LU:"+id+"  Prop:"+prop+"  Response:"+ "<"+vresponse+">");		
			}
			if(prop.equalsIgnoreCase("ST")) {
			    if(f.hvm[id].isChannelRamping()) {
				f.startRampAnimation();
			    } else {
				f.stopRampAnimation();
			    }
			    if(f.hvm[id].isChannelsTrip()) {
				f.stopRampAnimation();
				f.setModuleAlarm(id);
				System.out.println("Set Alarm in module:L" + id);
			    } else {
			        f.clearModuleAlarm(id);
			    }
			}
		    }
		    iprop++;
		}
		// update PS summary numbers
		f.setPSnumber(psresponse,id);
	       
	    }
	    
	    ils++;
	}// while
	
	//update LS summary number 
	f.setLSnumber(lsresponse);
    }

    
}




