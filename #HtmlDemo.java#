/*
 * @(#)HtmlDemo.java	1.4 99/07/23
 *
 * Copyright (c) 1997-1999 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

/**
 * Html Demo
 *
 * @version 1.4 99/07/23
 * @author Jeff Dinkins
 * modif: 29 May 2001
 */
public class HtmlDemo extends JPanel {
    public static int INITIAL_WIDTH = 700;
    public static int INITIAL_HEIGHT = 600;

    JEditorPane html;
    static JViewport vp ;
    /**
     * main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
	HtmlDemo demo = new HtmlDemo("hv/HVhelp");
	JFrame frame = new JFrame();	

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               System.exit(0);
            }
        });
 
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	//buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(closeButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = frame.getContentPane();
	contentPane.setLayout(new BorderLayout());
        contentPane.add(demo, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width/2 - INITIAL_WIDTH/2,
                          screenSize.height/2 - INITIAL_HEIGHT/2);
        frame.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        frame.show();
   }
    
    /**
     * HtmlDemo Constructor
     */
    public HtmlDemo(String path) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the SwingSet2 app.
        //super(swingset, "HtmlDemo", "toolbar/JEditorPane.gif");
	setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setLayout(new BorderLayout());
	
        try {
	    URL url = null;
	    // System.getProperty("user.dir") +
	    // System.getProperty("file.separator");
	    // String path = null;
	    try {
		//path = "hv/HVhelp";
		url = getClass().getResource(path);
            } catch (Exception e) {
		System.err.println("Failed to open " + path);
		url = null;
            }
	    
            if(url != null) {
                html = new JEditorPane(url);
                html.setEditable(false);
                html.addHyperlinkListener(createHyperLinkListener());
		html.setBorder(BorderFactory.createCompoundBorder(
		      BorderFactory.createLoweredBevelBorder(),
	              BorderFactory.createEmptyBorder(5,5,5,5)));
		//html.setPreferredSize(new Dimension(700, 600));
		JScrollPane scroller = new JScrollPane();
		scroller.setBorder(BorderFactory.createLoweredBevelBorder());
		vp = scroller.getViewport();
		vp.add(html);				
		add(scroller, BorderLayout.CENTER);
 
	    }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public HyperlinkListener createHyperLinkListener() {
	return new HyperlinkListener() {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		    if (e instanceof HTMLFrameHyperlinkEvent) {
			((HTMLDocument)html.getDocument()).processHTMLFrameHyperlinkEvent(
			    (HTMLFrameHyperlinkEvent)e);
		    } else {
			try {
			    html.setPage(e.getURL());
			} catch (IOException ioe) {
			    System.out.println("IOE: " + ioe);
			}
		    }
		}
	    }
	};
    }
    

}







