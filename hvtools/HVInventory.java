package hvtools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import java.io.*;
import java.util.*;


import hvframe.*;

public class HVInventory extends JPanel implements ActionListener {

    protected JTextPane textPane;
    protected JTextArea textArea;
    private final static String newline = "\n";
    private final static String bl = " ";

    protected static final String buttonString = "JButton";
    public static final JFrame frame = new JFrame("HV Inventory");
    private String DataDirKey = "data.dir";


    public HVInventory() {
        //super(new GridLayout(2,0));
	setLayout(new BorderLayout());


        textPane = createTextPane();
        textArea = new JTextArea();

        textArea.setEditable(false);

	JScrollPane paneScrollPane = new JScrollPane(textPane);
	paneScrollPane.setVerticalScrollBarPolicy(
			   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(250, 155));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));
	JScrollPane scrollPane = new JScrollPane(textArea,
						 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


        scrollPane.setPreferredSize(new Dimension(400, 500));
        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
	        c.fill = GridBagConstraints.HORIZONTAL;
        //add(textField, c);

        //c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        add(scrollPane,BorderLayout.CENTER);	
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	JButton saveButton = new JButton("Save");
	String dataDir = System.getProperty(DataDirKey);
	final  JFileChooser fc  = new JFileChooser(dataDir);	
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		//fc.rescanCurrentDirectory();
		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("txt");
		//		filter.addExtension("html");
		filter.setDescription("ASCII text files");
		fc.setFileFilter(filter);
		int returnVal = fc.showSaveDialog(HVInventory.this);
		
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
		    FileWriter out = null;
		    //		    if(file.exists()) 
			if (saveConfirmed(frame, file)) {
			    try {
				out = new FileWriter(file);
				save(out);
				out.close();
			    } catch (FileNotFoundException ef) {
				System.err.println("Save Inventory: " + ef);		    
			    } catch (IOException ef) {
				System.err.println("Save Inventory: " + ef);
			    }
			
		    
			    //this is where a real application would save the file.
			    System.out.println("Saving Inventory: " + file.getName() + "." + newline);		 
			    fc.cancelSelection();
			}
                } else {
		    // System.out.println("Save command cancelled by user." + newline);
                }
            }
	    });
	JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		//frame.setVisible(false);
		     closeFrame();
            }
        });

	buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
	buttonPanel.add(saveButton);
	buttonPanel.add(Box.createHorizontalGlue());
	buttonPanel.add(closeButton);
	add(buttonPanel,BorderLayout.SOUTH);

    }

    private boolean saveConfirmed(JFrame frame, File file) {
	if(file.exists()) {
	    String s1 = "Yes";
	    String s2 = "Cancel";
	    String fname= file.getName();
	    Object[] options = {s1, s2};
	    int n = JOptionPane.showOptionDialog(frame,
						 "Overwrite file: "+fname+"?",
						 "File Exists!",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.QUESTION_MESSAGE,
						 null,
						 options,
						 s2);
	    if (n == JOptionPane.YES_OPTION) {
		return true;
	    } else {
		return false;
	    }
	} else  return true;
    }

    public static String getDate(String DATE_FORMAT) {
	/* on some JDK, the default TimeZone is wrong
	** we must set the TimeZone manually!!!
	**   Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
	*/
	Calendar cal = Calendar.getInstance(TimeZone.getDefault());
	
	//	String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	java.text.SimpleDateFormat sdf = 
	    new java.text.SimpleDateFormat(DATE_FORMAT);
	    /*
	    ** on some JDK, the default TimeZone is wrong
	    ** we must set the TimeZone manually!!!
	    **     sdf.setTimeZone(TimeZone.getTimeZone("EST"));
	    */
	sdf.setTimeZone(TimeZone.getDefault());          
	
	return sdf.format(cal.getTime());
    }

    public void get(Hashtable hvft) {
	String DATE_FORMAT = "hh:mm:ss a MMM dd, yyyy";
	//statusArea.updateStatus("Date:"+getDate(DATE_FORMAT));
	textArea.setColumns(40);
	textArea.append("Date:"+getDate(DATE_FORMAT) + newline);
	textArea.append("Number of HVFrames: "+hvft.size() + newline);
	String offset="  ";
	
	for (Enumeration e = hvft.elements() ; e.hasMoreElements() ;) {
	    HVframe f = ((HVframe)e.nextElement());
	    // get sysinfo from frame
	    String name = f.host;
	    textArea.append(newline+"#--------------------------------------"+newline);
	    textArea.append("Host Name: "+name + newline);
	    if(!f.sysinfo.isEmpty()) {
		for(int k = 0; k<f.sysinfo.size(); k++) {
		    textArea.append(offset+(String)f.sysinfo.get(k) + newline);
		    //		    statusArea.updateStatus((String)f.sysinfo.get(k)); 
		}
	    }
	    
	    String head = "  Slot#     Model     Serial#";

	    textArea.append(head + newline);
	    
	    String slot;  
	    String model = new String();
	    String serial = new String();
	    for (int j=0;j<f.nunit;j++) {
	     	slot = f.getModuleSlot(j);
		int sl=slot.length();
		String bls="";
		for (int ib=0;ib<(10-sl);ib++) bls=bls+bl;
		model = f.hvm[j].getModel(); 
		serial = f.hvm[j].getSerNum(); 
		String text = offset+slot +bls +model+bl+bl+bl+bl+bl+serial;
		textArea.append(text+newline);
		//		textArea.append(text + newline);
	//	statusArea.updateStatus("Slot:"+slot+"   Model:"+model+"   Serial#:"+serial); 
	    }
	    
	} //for 0

    }

    private void save(FileWriter out) {
	try {
	    out.write(textArea.getText());
	} catch (IOException ef) {
	    System.err.println("HVInventory.save(): " + ef);
	}

    }

    private void closeFrame() {
       frame.setVisible(false);
       frame.dispose();
    }

    private JTextPane createTextPane() {
        String[] initString =
                { "This is an editable JTextPane, ",            //regular
                  "another ",                                   //italic
                  "styled ",                                    //bold
                  "text ",                                      //small
                  "component, ",                                //large
                  "which supports embedded components..." + newline,//regular
                  " " + newline,                                //button
                  "...and embedded icons..." + newline,         //regular
                  " ",                                          //icon
                  newline + "JTextPane is a subclass of JEditorPane that " +
                    "uses a StyledEditorKit and StyledDocument, and provides " +
                    "cover methods for interacting with those objects."
                 };

        String[] initStyles =
                { "regular", "italic", "bold", "small", "large",
                  "regular", "button", "regular", "icon",
                  "regular"
                };

        JTextPane textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);

	// try {
        //    for (int i=0; i < initString.length; i++) {
        //        doc.insertString(doc.getLength(), initString[i],
        //                         doc.getStyle(initStyles[i]));
        //    }
        //} catch (BadLocationException ble) {
        //    System.err.println("Couldn't insert initial text into text pane.");
	// }

        return textPane;
    }


    protected void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);

        s = doc.addStyle("icon", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon pigIcon = createImageIcon("images/Pig.gif",
                                            "a cute pig");
        if (pigIcon != null) {
            StyleConstants.setIcon(s, pigIcon);
        }

        s = doc.addStyle("button", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon soundIcon = createImageIcon("images/sound.gif",
                                              "sound icon");
        JButton button = new JButton();
        if (soundIcon != null) {
            button.setIcon(soundIcon);
        } else {
            button.setText("BEEP");
        }
        button.setCursor(Cursor.getDefaultCursor());
        button.setMargin(new Insets(0,0,0,0));
        button.setActionCommand(buttonString);
        button.addActionListener(this);
        StyleConstants.setComponent(s, button);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path,
                                               String description) {
        java.net.URL imgURL = HVInventory.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }



    public void actionPerformed(ActionEvent evt) {
	//        textArea.append(text + newline);


        //Make sure the new text is visible, even if there
        //was a selection in the text area.
	// textArea.setCaretPosition(textArea.getDocument().getLength());
    }




    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI() {
        //Make sure we have nice window decorations.
	//        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new HVInventory();
        //newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


    public void showInventory() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
