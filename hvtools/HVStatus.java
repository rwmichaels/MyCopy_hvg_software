//Modd&Add: 2-Oct-01: added new method saveToLog to store status messages to
//          logfile. Change updateStatus method: date and time added to messages
//          16-Oct-05: added blue color for messages that begins with "INFO " word

package hvtools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.util.*;
import java.text.*;
import java.io.*;


/**
 * HVStatus displays list of strings with current status of HV system
 * @version 1.1
 * Last update: 17-May-01
 */
public class HVStatus extends JList implements HVListener {
    DefaultListModel listModel;
    int maxrows = 25;
    String sfile = new String();
    FileWriter out = null;
    SimpleDateFormat formatter = new SimpleDateFormat ("hh:mm:ss a MMM dd, yyyy");


    /**
     * Constructucts a newly allocated HVstatus object
     * @param initString String to show after initilization
     *
     */	
    public HVStatus (String initString) {
       super() ;
       setFont(new Font("Serif", Font.PLAIN, 14));
       setForeground(Color.black);
       setSelectionBackground(Color.white);
       MyCellRenderer renderer = new MyCellRenderer();
       setCellRenderer(renderer);
       listModel = new DefaultListModel();
       //       listModel.addElement((String)initString);
       setModel(listModel);
       //setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    } 

 
    /**
     * Constructucts a newly allocated HVstatus object
     * @param initString String to show after initilization
     * @param maxrows int maximum number of string in list to store
     */	
    public HVStatus (String initString, int maxrows, String sfile) {
       super() ;
       setFont(new Font("Serif", Font.PLAIN, 14));
       setForeground(Color.black);
       setSelectionBackground(Color.white);
       MyCellRenderer renderer = new MyCellRenderer();
       setCellRenderer(renderer);
       listModel = new DefaultListModel();
       //       listModel.addElement((String)initString);
       setModel(listModel);

       this.maxrows = maxrows;
       this.sfile = sfile;
       updateStatus(initString+"\n");

       //setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 
    }

   /**
    * Udates (appends) current list with new string and place it on 
    * the top of the list, store message to log file
    * @param message String string to append
    */	
    public synchronized void updateStatus(String message) {

	String dateString = formatter.format(new Date());
	String smessage = dateString +" > " +message;

	if(listModel.getSize()<maxrows) {
	    listModel.add(0,(String)smessage);
	} else {
	    listModel.remove(listModel.getSize()-1);
	    listModel.add(0,(String)smessage);
	}
	saveToLog(message);
      	setSelectedIndex(0);
    }
    
   /**
    * Appends to current message date and time and store it in log file
    * @param message String string to store
    */	
    public void saveToLog (String message) {
	String dateString = formatter.format(new Date());

	try {
	    out = new FileWriter(sfile, true);
	    // save message
	    out.write(dateString +" > " +message +"\n");
	    out.close();
	} catch (FileNotFoundException ef) {
	    System.err.println("HVStatus:saveToLog " + ef);		    
	} catch (IOException ef) {
	    System.err.println("HVStatus:saveToLog " + ef);
	} 
	
    }


    class MyCellRenderer extends JLabel implements ListCellRenderer {
	public MyCellRenderer() {
	    //setOpaque(true);
	    setFont(new Font("Serif", Font.PLAIN, 14));
	}
	public Component getListCellRendererComponent(
						      JList list,
						      Object value,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus)
	{
	    setText(value.toString());
	    setBackground(Color.white);
	    setForeground(Color.black);
	    StringTokenizer st = new StringTokenizer(value.toString());
	    String s = new String();
	    while (st.hasMoreTokens()) {
		s = st.nextToken();
		if(s.equalsIgnoreCase("ERROR")||s.equalsIgnoreCase("ALARM")) {
		    setForeground(Color.red);
		    //    setBackground(Color.red);
		}
		if(s.equalsIgnoreCase("INFO")) {
		    setForeground(Color.blue);
		
		} 
	    }
	 
	    return this;
	}
    }
}
 








