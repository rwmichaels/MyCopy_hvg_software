package hvmap;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultCellEditor;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.Dimension;
import java.util.*;
import java.awt.Color;
import java.text.*;
import java.io.*;
import javax.swing.text.*;
import javax.swing.*;
import javax.swing.JOptionPane;

import hvtools.*;
import hvframe.*;


/** HVMapTable - present window to view of detector set 
 *   as table with HVchannel status(dis/en)
 * 
 * @version 1.1
 * Last update:16-Oct-05: add exception 'ArrayIndexOutOfBoundsException' in Model.setValueAt() if format of map file and it parameters are defferent
 *             25-Aug-05: add "start index" for axis labels
 */
public class HVMapTable extends JFrame implements Printable {

    protected boolean DEBUG = false;
    protected JMenuBar menuBar;
    protected JMenu menu, submenu;   
    JMenuItem menuItem;
    JRadioButtonMenuItem rbMenuItem;
    JPopupMenu popup;
    public JTable maptable = null;
    protected MyTableModel myModel = null;
    public Vector hvmap = null;
    public Hashtable hvft = null;
    public JTextArea statArea = null;
    protected HVMapMonitor monitor;
    final int TFONTSIZE = 14;
    final int SFONTSIZE = 14;
    private int YMAX = 1;
    private int XMAX = 1;
    private int startY = 0;
    private int startX = 0;

    private boolean incrX = true;// direction of counting of X axis (if true - incremental to right)
    private boolean incrY = true; // direction of counting of Y axis (if true - incremental to up)

    ImageIcon ichen = new ImageIcon("./images/chnen.gif");
    ImageIcon ichdis = new ImageIcon("./images/chndis.gif");
    ImageIcon icherr = new ImageIcon("./images/chnerr.gif");
    ImageIcon ichnc = new ImageIcon("./images/chnnc.gif");
    final String frameTitle = new String("HV MAP: "); 
    final String XcolumnHeader = new String(" ");
    final String YrowHeader = new String(" ");
    final String XYHeader = new String("Y \\ X");
    String[] prop = {new String("CE"), new String("MV"), new String("DV"), new String("MC") };
    String[] proptitle = {new String("Channel Status"), new String("Measured Voltage"), 
			  new String("Demand Voltage"), new String("Measured Current") };
    Color mycolor = new Color(240,245,245);
    int propertyKey = 0;
    private String mapName = new String();
    public int INIT_WIDTH = 100;
    public int INIT_HEIGHT = 100;
    
    /**
     * constructor
     * @param hvmap Vector with addresses of hvchannels
     * @param hvft  Hashtable with connected hvframes
     */
    public HVMapTable() {
	//        super(" ");
	//	setTitle(frameTitle+proptitle[0]);

	menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	//Build the File  menu.
	menu = new JMenu("File");
	menu.setMnemonic(KeyEvent.VK_F);
	menu.getAccessibleContext().setAccessibleDescription(
		 "The File only menu in this program that has menu items");
	menuBar.add(menu);
	addFileMenu(menu);

	//Build the View menu.
	menu = new JMenu("View");
	menu.setMnemonic(KeyEvent.VK_V);
	menu.getAccessibleContext().setAccessibleDescription(
	          "The View only menu in this program that has menu items");
	    	
	menuBar.add(menu);
	addViewMenu(menu);

        popup = new JPopupMenu();
	//	addPopupMenu(popup);

	//	addPopupMenu(popup);

	menuBar.add(Box.createHorizontalGlue());
        addNewMenu("Help");
 
    }
 				

    /**
     * constructor
     * @param hvmap Vector with addresses of hvchannels
     * @param hvft  Hashtable with connected hvframes
     */
    public HVMapTable(Vector hvmap, Hashtable hvft, String params) {
        super(" ");
	//	setTitle(frameTitle+proptitle[0]);
	this.hvmap = hvmap;
	this.hvft = hvft;

	setParameters(params);
	menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	//Build the File  menu.
	menu = new JMenu("File");
	menu.setMnemonic(KeyEvent.VK_F);
	menu.getAccessibleContext().setAccessibleDescription(
		 "The File only menu in this program that has menu items");
	menuBar.add(menu);
	addFileMenu(menu);

	//Build the View menu.
	menu = new JMenu("View");
	menu.setMnemonic(KeyEvent.VK_V);
	menu.getAccessibleContext().setAccessibleDescription(
	          "The View only menu in this program that has menu items");
	    	
	menuBar.add(menu);
	addViewMenu(menu);

        popup = new JPopupMenu();
	//	addPopupMenu(popup);

	//	addPopupMenu(popup);

	menuBar.add(Box.createHorizontalGlue());
        addNewMenu("Help");
 

        myModel = new MyTableModel("CE");
        maptable = new JTable(myModel);
	myModel.addTableModelListener(maptable);
	//	maptable.setFont(new Font("Hevletica", Font.PLAIN, 16));
	      //  maptable.setPreferredScrollableViewportSize(new Dimension(600, 550));
	//maptable.setColumnSelectionAllowed(true);
	maptable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	maptable.setCellSelectionEnabled(true);
 
        DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
	tableRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableRenderer.setToolTipText("Double click on channel cells for details");	
	maptable.setDefaultRenderer(Object.class, tableRenderer);

       //Set up column sizes.
	initColumnSizes(maptable);

	//maptable.setPreferredSize(new Dimension(tableWidth,tableHeight));
	//	maptable.setPreferredScrollableViewportSize(maptable.getSize());

	//maptable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN;
	//	 maptable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	//maptable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	maptable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	//	  maptable.sizeColumnsToFit(-1);
	 //disable reordering
	 maptable.getTableHeader().setReorderingAllowed(false);

      //Create the scroll pane and add the table to it. 
        JScrollPane scrollPane = new JScrollPane(maptable);


	setUpColumnEditor(maptable);

	getContentPane().setLayout(new GridLayout(1, 1)); 
        //Add the scroll pane to this window.
	//	add(scrollPane, BorderLayout.CENTER);


	BoxLayout tBox = new BoxLayout(this, BoxLayout.Y_AXIS);
	//       getContentPane().setLayout(tBox);
	//	getContentPane().add(scrollPane);
	//Add the scroll pane to this window.
	getContentPane().add(scrollPane, BorderLayout.CENTER);


	//	setSize(maptable.getSize());

	//start monitor to watch changing of data
	int delay = 5000; // delay of monitoring loop in 5sec 
	//	monitor = new HVMapMonitor(this, delay);
	//	monitor.start();

       //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();        
        maptable.addMouseListener(popupListener);
        menuBar.addMouseListener(popupListener);
	
	int tableWidth = (int) maptable.getColumnModel().getTotalColumnWidth();
      	int tableHeight = (int) maptable.getRowCount()*(maptable.getRowHeight() +
			     maptable.getRowMargin()) + maptable.getTableHeader().getHeight();

	INIT_WIDTH =  maptable.getColumnCount()*( (int)maptable.getCellRect(1,1,true).getWidth());
	INIT_HEIGHT = maptable.getRowCount()*((int)maptable.getCellRect(1,1,true).getHeight()) + maptable.getTableHeader().getHeight();

	//INIT_WIDTH = tableWidth;
	//	INIT_HEIGHT = tableHeight;

	//	setSize(tableWidth,tableHeight);
	// Size(maptable.getPreferredSize());
	
	//System.out.println("Size:"+getSize());
	
	//System.out.println("PrefSize:"+getPreferredSize());
	//System.out.println("TablePrefSize:"+maptable.getPreferredSize());
	//System.out.println("ScrolPrefSize:"+scrollPane.getPreferredSize());
	//System.out.println("Bound:"+getBounds());

	System.out.println("TabelWidth:"+tableWidth+"  TableHeight:"+tableHeight);
	System.out.println("Init_Width:"+INIT_WIDTH+"  Init_Height:"+INIT_HEIGHT);
	
	//setUndecorated(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
		closeWindow();
		//                System.exit(0);
            }
	    });
    }     
    

    

    // methods

    public int getTableWidth() {
	return (int) maptable.getColumnModel().getTotalColumnWidth();

    }
    
    public int getTableHeight() {
      	return (int) maptable.getRowCount()*(maptable.getRowHeight() +
			     maptable.getRowMargin()) + maptable.getTableHeader().getHeight();
    }

    public void setMapName(String name) {
	setTitle(name+":"+proptitle[0]);
	mapName = name;
    }

    public String getMapName() {
	return mapName;
    }

    public int getMapXmax() {
	return XMAX;
    }

    public int getMapYmax() {
	return YMAX;
    }
    public boolean getMapXdir() {
	return incrX;
    }

    public boolean getMapYdir() {
	return incrY;
    }
    public int getMapXstart() {
	return startX;
    }
    public int getMapYstart() {
	return startY;
    }



    /** 
     * This method sets parameters of the map: sizeX, sizeY, directionX, directionY from
     * parameter string 'param'
     * @param param String with parameters of the map in form: "sizeX:sizeY:directionX:directionY"
     */
    public void setParameters(String param) {
	String dir = null;
	String size =null;
	StringTokenizer tokenizer = new StringTokenizer(param, ":");
	try {
	    if (tokenizer.hasMoreTokens()) {
		size  = tokenizer.nextToken();
		XMAX = Integer.parseInt(size);		
	    }
	    if (tokenizer.hasMoreTokens()) {
		YMAX = Integer.parseInt(tokenizer.nextToken());		
	    }
	    if (tokenizer.hasMoreTokens()) {
		dir  = tokenizer.nextToken();
		if(dir.equalsIgnoreCase("1")) incrX = true;
		else incrX = false;		
	    }
	    if (tokenizer.hasMoreTokens()) {
		dir  = tokenizer.nextToken();
		if(dir.equalsIgnoreCase("1")) incrY = true;
		else incrY = false;
	    }
	    if (tokenizer.hasMoreTokens()) {
		size  = tokenizer.nextToken();
		startX = Integer.parseInt(size);		
	    }
	    if (tokenizer.hasMoreTokens()) {
		startY = Integer.parseInt(tokenizer.nextToken());		
	    }
	    System.out.println("X:" +XMAX +"dir:" +incrX +":" +startX +"  Y=" +YMAX +":" +incrY +":" +startY);
	} catch(NumberFormatException ne) {
	    System.out.println(ne);
	}
    }
    

    /**
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells' 
     * contents, then you can just use column.sizeWidthToFit().
     * @param table JTable 
     * @param model MyTableModel
     */
   protected void initColumnSizes(JTable table) {
        TableColumn column = null;
        Component comph = null;
	Component comp = null;
       int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = myModel.longValues;
        TableCellRenderer headerRenderer = 
            table.getTableHeader().getDefaultRenderer();


        for (int i = 0; i < XMAX+1; i++) {
            column = table.getColumnModel().getColumn(i);

	    //Set up tool tips for the cells.
	    //	    DefaultTableCellRenderer renderer =
            //    new DefaultTableCellRenderer();
	    // renderer.setToolTipText("Click for details");
	    //column.setCellRenderer(renderer);
 
            try {
                comph = headerRenderer.
                                 getTableCellRendererComponent(
                                     null, column.getHeaderValue(), 
                                     false, false, 0, 0);
                headerWidth = comph.getPreferredSize().width;
            } catch (NullPointerException e) {
                System.err.println("Null pointer exception!");
                System.err.println("  getHeaderRenderer returns null in 1.3.");
                System.err.println("  The replacement is getDefaultRenderer.");//getHeaderRenderer()
            }

	    if(true) {  

		comp = table.getDefaultRenderer(myModel.getColumnClass(i)).
		    getTableCellRendererComponent(
						  table,longValues[i] ,
						  false, false, 0, i);
		cellWidth = comp.getPreferredSize().width;

	    } else {
		cellWidth =  headerWidth;
	    }
            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
	    column.setPreferredWidth(Math.max(headerWidth+2, cellWidth+2));
	    //    comph.setSize(Math.max(headerWidth+2, cellWidth+2), comph.getPreferredSize().height);


	}
	//	maptable.setRowHeight(ichen.getIconHeight()+ichen.getIconHeight()/2);
    } 


    //Set up the editor for the  cells.
    protected void setUpColumnEditor(JTable table) {
        //First, set up the button that brings up the dialog.
        final JButton button = new JButton("") {
            public void setText(String s) {
                //Button never shows text -- only color.
            }
        };
        button.setBackground(mycolor);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0,0,0,0));
        button.setHorizontalAlignment(SwingConstants.LEADING);

        //Now create an editor to encapsulate the button, and
        //set it up as the editor for all cells.
        final ColorEditor colorEditor = new ColorEditor(button);
        table.setDefaultEditor(Object.class, colorEditor);

        //Set up the dialog that the button brings up.
       //Here's the code that brings up the dialog.
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		// button.setBackground(colorEditor.currentColor);
                //colorChooser.setColor(colorEditor.currentColor);
                //Without the following line, the dialog comes up
                //in the middle of the screen.
                //dialog.setLocationRelativeTo(button);
		Object info = getChannelSettings();

		JOptionPane.showMessageDialog(button, info, "Channel Settings",
		       JOptionPane.INFORMATION_MESSAGE, null);
		//               dialog.show();
            }
        });
    }

    // return object with channels properies values
    public Object getChannelSettings() {
	JTextArea text = new JTextArea();

	String star = new String("*");
	String hostname = star;
	String module = star;
	String channel = star;
	String label = null;
	String spar = null;
	int imodule = 0;
	int smodule = 0;
	int ichannel = 0;
	String chnstat = new String("***"); // present not connected channels
	String hvstat = new String("HVOFF");
	int row = maptable.getSelectedRow();
	int column = maptable.getSelectedColumn();
	int index = column+row*XMAX -1;
	int x = 0;
	int y = 0;
	// check axis directioin
	if(incrX) {
	 x = column;
	} else {
	    x = XMAX-column+1; // decremental
	}

	if(incrY) {
	 y = YMAX-row;
	} else {
	    y = row+1; // decremental
	}

	//	System.out.println("col:"+column+" :row:"+row+" :ind:"+index);
	String chnaddr =(String) hvmap.elementAt(index);


	text.append("   CHANNEL INFO for " +mapName+"("+(x+startX-1)+","+(y+startY-1)+"):\n");	
	//	text.append("index :"+index+"\n");	
	//text.append("column :"+column+"\n");	
	//text.append("row :"+row+"\n");
	//text.append("address :"+chnaddr+"\n");

	StringTokenizer st =  new StringTokenizer(chnaddr,".");
	if(st.hasMoreTokens())  hostname = st.nextToken(); 
	if(st.hasMoreTokens())  module = st.nextToken(); 
	if(st.hasMoreTokens())  channel = st.nextToken(); 
   	if(!hostname.equalsIgnoreCase(star)) {
	    if(hvft.containsKey(hostname)) { 
		//do next step
		HVframe f =  (HVframe)hvft.get(hostname);
		if(f.HVONstatus) hvstat = ("HVON");
		text.append("Hostname :"+hostname+"\n");
		if(!module.equalsIgnoreCase(star)) {
		    //smodule = Integer.parseInt(module) ; // 3-Sep-05
		    imodule = f.getModuleIndex(module);  // 3-Sep-05, the input parameter now is String 
		    if(imodule>=0) {
			text.append("Slot     :"+module+"\n");
			int numparam =  f.hvm[imodule].numparam;
			if(!channel.equalsIgnoreCase(star)) {
			    ichannel = Integer.parseInt(channel) ;
			    text.append("Channel  :"+ichannel +"\n");
			}
			//trace module to the mainMenu window
			f.ftabb.setSelectedComponent(f);
			f.tabb.setSelectedIndex(imodule);
			f.hvm[imodule].table.changeCellSelection(ichannel,0);
			text.append("HVStatus :"+hvstat+"\n");
			for (int i=0; i<numparam; i++) {
			    spar = f.hvm[imodule].getParameterName(i);
			    label = f.hvm[imodule].pattr[i].getAttr("label");
			    chnstat = f.hvm[imodule].ch[ichannel].getValue(spar);      
			    
			    text.append( label+" : "+chnstat +"\n");
			}
		    }
		} else {
		    text.append( "*** No Module in Slot: "+ module +"\n");
		}
	    } else {
		text.append( "*** No connection ***" +"\n");
	    }
	}
	

	return text;
    }

    /*
     * The editor button that brings up the dialog.
     * We extend DefaultCellEditor for convenience,
     * even though it mean we have to create a dummy
     * check box.  Another approach would be to copy
     * the implementation of TableCellEditor methods
     * from the source code for DefaultCellEditor.
     */
    class ColorEditor extends DefaultCellEditor {
        Object currentIcon = null;

        public ColorEditor(JButton b) {
                super(new JCheckBox()); //Unfortunately, the constructor
                                        //expects a check box, combo box,
                                        //or text field.
            editorComponent = b;
            setClickCountToStart(2); //This is usually 1 or 2.

            //Must do this so that editing stops when appropriate.
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        public Object getCellEditorValue() {
            return currentIcon;
        }

        public Component getTableCellEditorComponent(JTable table, 
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            ((JButton)editorComponent).setText(value.toString());
	    currentIcon = value;
            return editorComponent;
        }
    }

    public String StatusCheck(int chn) {
	String s = new String();
	//      	String s = m.ch[chn].getValue("ST");
	//String[] hex = new String[4];
	int nhex = 0;
	int[] ihex ={0,0,0,0} ;
	//	for( nhex=0;nhex<s.length();nhex++) {
	//	    hex[nhex] = s.substring(nhex,nhex);
	//convert hexidecimal value to integer;
	//	    ihex[nhex] = Integer.parseInt(hex[nhex],16);
	//	}

	if(getBit(ihex[nhex],0)) { 
	    //	    m.ch[chn].setValue("CE","1");
	        s = "Channel:"+chn+" Enabled.";
	} else {
	    // m.ch[chn].setValue("CE","0");
	     s = "Channel:"+chn+" Disabled.";
	}

 	if(getBit(ihex[nhex],1)) { 
	        s= s+" Output is ramping to a higher absolute value.";
	}
 
	if(getBit(ihex[nhex],2)) {
	    s= s+" Output is ramping to a lower absolute value.";
	}

	for (int i=nhex-1;i>0;i--) {
	    if (ihex[i-1] != 0) s = s + " TRIP CONDITION!!!";
	}	
	return s;
    }

    public boolean getBit(int val, int bit) {
	return (((val >> bit)&1) == 1) ? true : false ;
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
	
	Graphics2D  g2 = (Graphics2D) g;
        g2.setColor(Color.black);
        int fontHeight=g2.getFontMetrics().getHeight();
        int fontDesent=g2.getFontMetrics().getDescent();
     	double pageHeight = pageFormat.getImageableHeight() - fontHeight;
     	double pageWidth = pageFormat.getImageableWidth();
      	double tableWidth = (double) maptable.getColumnModel().getTotalColumnWidth();
      	double tableHeight = (double) maptable.getRowCount()*(maptable.getRowHeight() +
			     maptable.getRowMargin()) + maptable.getTableHeader().getHeight();
        double paneWidth = (double) this.getWidth();
        double scale = 1;
	double wscale =1;
	double hscale =1;

        if (tableWidth >= pageWidth) {
                wscale =  pageWidth / tableWidth;
        }
        if (tableHeight >= pageHeight) {
                hscale =  pageHeight / tableHeight;
        }
	scale = Math.min(wscale,hscale);

        double headerHeightOnPage=
                      maptable.getTableHeader().getHeight()*scale;
        double tableWidthOnPage=tableWidth*scale;

        double oneRowHeight=(maptable.getRowHeight()+ maptable.getRowMargin())*scale;

        int numRowsOnAPage=(int)(( pageHeight-headerHeightOnPage)/oneRowHeight);
        double pageHeightForTable=oneRowHeight*numRowsOnAPage;
        int totalNumPages= (int)Math.ceil((
                      (double)maptable.getRowCount())/numRowsOnAPage);

        //if(pageIndex>=totalNumPages) {
	    //                      return NO_SUCH_PAGE;
	// }

        g2.translate(pageFormat.getImageableX(), 
                       pageFormat.getImageableY());

        DateFormat df = DateFormat.getDateInstance();
	String dateString = df.getDateTimeInstance().format(new Date());
       	String outstr = new String("Page " +(pageIndex+1) +". "+proptitle[myModel.propkey]+". " + dateString);
	int strshift = g2.getFontMetrics().stringWidth(outstr);
        g2.drawString(outstr, (int)pageWidth/2 - strshift/2,
                      (int)(pageHeight+fontHeight-fontDesent));//bottom center



	if(false) {
	g2.translate(0f,headerHeightOnPage);
	g2.translate(0f,-pageIndex*pageHeightForTable);
        //If this piece of the table is smaller than the size available,
        //clip to the appropriate bounds.
	if(false) {
	//        if (pageIndex + 1 == totalNumPages) {
                     int lastRowPrinted = numRowsOnAPage * pageIndex;
                     int numRowsLeft = maptable.getRowCount() - lastRowPrinted;
                     g2.setClip(0, (int)(pageHeightForTable * pageIndex),
                       (int) Math.ceil(tableWidthOnPage),
                       (int) Math.ceil(oneRowHeight * numRowsLeft));
        }
        //else clip to the entire area available.
        else{    
        
            g2.setClip(0, (int)(pageHeightForTable*pageIndex), 
                     (int) Math.ceil(tableWidthOnPage),
                     (int) Math.ceil(pageHeightForTable));        
        }
	}

        g2.scale(scale,scale);
	
	maptable.paint(g2);
	g2.scale(1/scale,1/scale);
	g2.translate(0f,pageIndex*pageHeightForTable);
	g2.translate(0f, -headerHeightOnPage);
	g2.setClip(0, 0,(int) Math.ceil(tableWidthOnPage), 
		   (int)Math.ceil(headerHeightOnPage));
    	g2.scale(scale,scale);
	maptable.getTableHeader().paint(g2);//paint header at top
	
        return Printable.PAGE_EXISTS;
    }

    public void printTable() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
            /* Set up Book */
        PageFormat landscape = printJob.defaultPage();
        PageFormat portrait = printJob.defaultPage();
        landscape.setOrientation(PageFormat.LANDSCAPE);
        portrait.setOrientation(PageFormat.PORTRAIT);
        Book bk = new Book();
	bk.append((Printable)this, landscape);
 
        printJob.setPageable(bk);
 
        //    printJob.setPrintable(this);

        //Page dialog
        //PageFormat pf = printJob.pageDialog(printJob.defaultPage());

        //Print dialog
        if(printJob.printDialog()){
            try { printJob.print(); } catch (Exception PrintException) { }
        }
    }

   public void addFileMenu(JMenu m) {
 
       menuItem = new JMenuItem("Print",
				KeyEvent.VK_P);
       menuItem.setAccelerator(KeyStroke.getKeyStroke(
						      KeyEvent.VK_P, ActionEvent.ALT_MASK));
       menuItem.getAccessibleContext().setAccessibleDescription(
                "Print");
       
       menuItem.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent e) {
	       printTable();
	    }       
       });
       menuItem.setToolTipText("Print this window");
       m.add(menuItem);        
       m.addSeparator(); 
       
       menuItem = new JMenuItem("Close",
				KeyEvent.VK_W);
       menuItem.setAccelerator(KeyStroke.getKeyStroke(
						      KeyEvent.VK_W, ActionEvent.ALT_MASK));
       menuItem.getAccessibleContext().setAccessibleDescription(
                "Close window");

       menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		closeWindow();
	    }       
       });
        menuItem.setToolTipText("Close this window");
        m.add(menuItem);         

   }
    /**
     * Close current window and stops mapMonitor thread
     */
    public void closeWindow() {
	//	monitor.stopMonitor();
	this.setVisible(false);
	this.dispose();
   } 
    
    protected void addViewMenu(JMenu m) {
	
	//       menuItem = new JMenuItem("Status",
	//				KeyEvent.VK_S);
	//menuItem.setAccelerator(KeyStroke.getKeyStroke(
	//					      KeyEvent.VK_S, ActionEvent.ALT_MASK));
	//menuItem.getAccessibleContext().setAccessibleDescription(
        //        "Status");

       ButtonGroup group = new ButtonGroup();
       rbMenuItem = new JRadioButtonMenuItem("Channels Status");
       rbMenuItem.setSelected(true);
       rbMenuItem.setMnemonic(KeyEvent.VK_S);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(propertyKey!=0) {
		    propertyKey = 0;
		    myModel.changeData(prop[0]);
		    myModel.updateLongValue();
		    setTitle(mapName+":"+proptitle[0]);
		//maptable.sizeColumnsToFit(-1);
		//		myModel = new MyTableModel("CE");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		    initColumnSizes(maptable);
		} 
	    }       
       });

       rbMenuItem.setToolTipText("View of channels status as icon ");
       group.add(rbMenuItem);
       m.add(rbMenuItem);
       //      m.addSeparator(); 

       rbMenuItem = new JRadioButtonMenuItem("Measured Voltage");
       rbMenuItem.setMnemonic(KeyEvent.VK_V);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(propertyKey!=1) {
		    propertyKey = 1;
		    myModel.changeData(prop[1]);
		    myModel.updateLongValue();
		   setTitle(mapName+":"+proptitle[1]);
		//maptable.sizeColumnsToFit(-1);		
		//myModel = new MyTableModel("MV");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		//Set up column sizes.
		    initColumnSizes(maptable); 
		}
	    }       
       });

       rbMenuItem.setToolTipText("View of Channels Measured Voltage");
       group.add(rbMenuItem);
       m.add(rbMenuItem);


       rbMenuItem = new JRadioButtonMenuItem("Setting Voltage");
       rbMenuItem.setMnemonic(KeyEvent.VK_D);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(propertyKey!=2) {
		    propertyKey = 2;
		    myModel.changeData(prop[2]);
		    myModel.updateLongValue();
		    setTitle(mapName+":" +proptitle[2]);
		//		matable.sizeColumnsToFit(-1);		
		//myModel = new MyTableModel("DV");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		//Set up column sizes.
		    initColumnSizes(maptable); 
		}
	    }       
       });

       rbMenuItem.setToolTipText("View of Channels Demand Voltage");
       group.add(rbMenuItem);
       m.add(rbMenuItem);


       rbMenuItem = new JRadioButtonMenuItem("Measured Current");
       rbMenuItem.setMnemonic(KeyEvent.VK_C);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(propertyKey!=3) {
		    propertyKey = 3;
		    myModel.changeData(prop[3]);
		    myModel.updateLongValue();
		    setTitle(mapName+":"+proptitle[3]);
		//		maptable.sizeColumnsToFit(-1);		
		//myModel = new MyTableModel("MC");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		//Set up column sizes.
		    initColumnSizes(maptable); 
		}
	    }       
       });

       rbMenuItem.setToolTipText("View of Channels Measured Current");
       group.add(rbMenuItem);
       m.add(rbMenuItem);

  }

    public void addPopupMenu(JPopupMenu m) {
	
	//       menuItem = new JMenuItem("Status",
	//				KeyEvent.VK_S);
	//menuItem.setAccelerator(KeyStroke.getKeyStroke(
	//					      KeyEvent.VK_S, ActionEvent.ALT_MASK));
	//menuItem.getAccessibleContext().setAccessibleDescription(
        //        "Status");

       ButtonGroup group = new ButtonGroup();
       rbMenuItem = new JRadioButtonMenuItem("Channels Status");
       rbMenuItem.setSelected(true);
       rbMenuItem.setMnemonic(KeyEvent.VK_S);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(myModel.propkey!=0) {
		    
		    myModel.changeData(prop[0]);
		    myModel.updateLongValue();
		    setTitle(mapName+":"+proptitle[0]);
		//maptable.sizeColumnsToFit(-1);
		//		myModel = new MyTableModel("CE");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		    initColumnSizes(maptable); 
		}
	    }       
       });

       rbMenuItem.setToolTipText("View of channels status as icon ");
       group.add(rbMenuItem);
       m.add(rbMenuItem);        
       //      m.addSeparator(); 

       rbMenuItem = new JRadioButtonMenuItem("Measured Voltage");
       rbMenuItem.setMnemonic(KeyEvent.VK_V);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(myModel.propkey!=0) {
		   myModel.changeData(prop[1]);
		   myModel.updateLongValue();
		    setTitle(mapName+":"+proptitle[1]);
		//maptable.sizeColumnsToFit(-1);		
		//myModel = new MyTableModel("MV");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		//Set up column sizes.
		    initColumnSizes(maptable); 
		} 
	    }
       });

       rbMenuItem.setToolTipText("View of Channels Measured Voltage");
       group.add(rbMenuItem);
       m.add(rbMenuItem);

       rbMenuItem = new JRadioButtonMenuItem("Setting Voltage");
       rbMenuItem.setMnemonic(KeyEvent.VK_D);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(myModel.propkey!=0) {
		    myModel.changeData(prop[2]);
		    myModel.updateLongValue();
		    setTitle(mapName+":"+proptitle[2]);
		//		matable.sizeColumnsToFit(-1);		
		//myModel = new MyTableModel("DV");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		//Set up column sizes.
		    initColumnSizes(maptable); 
		} 
	    }      
       });

       rbMenuItem.setToolTipText("View of Channels Demand Voltage");
       group.add(rbMenuItem);
       m.add(rbMenuItem);

       rbMenuItem = new JRadioButtonMenuItem("Measured Current");
       rbMenuItem.setMnemonic(KeyEvent.VK_C);
       rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(myModel.propkey!=0) {
		    myModel.changeData(prop[3]);
		    myModel.updateLongValue();
		    setTitle(mapName+":"+proptitle[3]);
		//		maptable.sizeColumnsToFit(-1);		
		//myModel = new MyTableModel("MC");
		//maptable.setModel(myModel);
		//myModel.fireTableStructureChanged();
		//Set up column sizes.
		    initColumnSizes(maptable); 
		}
	    }       
       });

       rbMenuItem.setToolTipText("View of Channels Measured Current");
       group.add(rbMenuItem);
       m.add(rbMenuItem);


  }


    public void addNewMenu(String title) {
        JMenu m = (JMenu)menuBar.add(new JMenu(title));
        m.add("Help?");
    } 


    public synchronized void updateChannel(String chnaddr) {
	int index = hvmap.indexOf((String) chnaddr);
	
	int i = 0;
	int k = 0;

	//System.out.println(mapName+"index:"+index);
	if(index>=0) {
	    i = index/XMAX;
	    k = index-(i)*XMAX +1;
	    //System.out.println(mapName+":index:"+index+ " x:"+i+" y:"+k);
	    
	    String chnstat =(String)  getChannelStatus(chnaddr,prop[propertyKey]);
	    ImageIcon icon = ichnc;
	    //System.out.println("PropKey:"+propertyKey +" Status:"+chnstat+" chaddr:"+chnaddr);
	    if(propertyKey==0) {
		if(chnstat.equalsIgnoreCase("1")) icon = ichen; 
		if(chnstat.equalsIgnoreCase("0")) icon = ichdis; 
		if(chnstat.equalsIgnoreCase("3")) icon = icherr; 
		maptable.getModel().setValueAt((ImageIcon)icon , i, k);		
	    }
	    if((propertyKey>0) && (propertyKey<4)) {
		maptable.getModel().setValueAt((String)chnstat , i, k);		
		//data[i][k] =(String) chnstat;
	    }
	    
	    //fireTableCellUpdated(i, k);	    
	}
    }



    /*****************************************************************
     *  MyTableModel
     *  inner class - table model, init data and table header
     *
     */
    class MyTableModel extends AbstractTableModel {
	int propkey = 0;
	String propview = new String();
	
        public  MyTableModel(String propertyname) {
	    //	    super();
	    propview = propertyname;
	    // System.out.println("propview:"+propview+"  name:"+propertyname);
	    for(int i=0; i<4; i++) {
		if(propertyname.equalsIgnoreCase(prop[i])) {
		    propkey = i;
		}
	    }
	}

	String[] columnNames = setcolumnNames();
	

	public String[] setcolumnNames() {
	    int np = XMAX+1;	    
	    String[] s = new String[np];
	    int lab=0;
	    //System.out.println("NUMPARAM:"+np);

	    s[0] = XYHeader;		

	    // 23-Aug-2005 add start index shifting for axis labels
	    if(incrX)
		for(int i=1;i<np;i++) {
		    lab=i+startX-1;
		    if((i+startX)>9) s[i] = XcolumnHeader+(lab);
		    if((i+startX)<10)  s[i] = XcolumnHeader+"0"+(lab);
		    // 	System.out.println("Label:"+i+":"+s[i]);
		}
	    else 
		for(int i=1;i<np;i++) {
		    if((np-i+startX)>9) s[i] = XcolumnHeader+(np-i+startX-1);
		    if((np-i+startX)<10)  s[i] = XcolumnHeader+"0"+(np-i+startX-1);
		    // 	System.out.println("Label:"+i+":"+s[i]);
		}
	    
	    return s;
	}
	
	Object[][] data = setdata(prop[0]);

	public Object[][] setdata(String propertyname) {
	    int ny = YMAX ;
	    int nx = XMAX+1;	    
	    Object[][] ob = new Object[ny][nx];	    

	    propview = propertyname;

	    for(int i=0; i<4; i++) {
		if(propertyname.equalsIgnoreCase(prop[i])) {
		    propkey = i;
		}
	    }

	    int labelY=0;

	    for(int i=0; i<ny; i++) {
		ob[i][0] = new Object();
		//ob[i][0] = YrowHeader + (ny-i);

	    // 24-Aug-2005 add start index shifting for axis labels
		if(incrY) labelY = ny-i+startY-1;
		else  labelY = i+startY;

		if((labelY)>9) ob[i][0] = YrowHeader+(labelY);		
		if((labelY)<10)  ob[i][0] = YrowHeader+"0"+(labelY);

		
		for(int k=1; k<nx; k++) {
		    ob[i][k] = new Object();
		    int m = k-1 + (nx-1)*i;
		    String chnaddr =(String) hvmap.elementAt(m);
		    //System.out.println(mapName+" : " +chnaddr);

		    //ob[i][k] =(ImageIcon) getChannelStatus(chnaddr,propview);

		    String chnstat =(String)  getChannelStatus(chnaddr,prop[propkey]);
		    ImageIcon icon = ichnc;
		    if(propkey==0) {
			if(chnstat.equalsIgnoreCase("1")) icon = ichen; 
			if(chnstat.equalsIgnoreCase("0")) icon = ichdis; 
			if(chnstat.equalsIgnoreCase("3")) icon = icherr; 
			ob[i][k] =(ImageIcon) icon;		
			//if((k>10)&&(k<16)) ob[i][k] =(ImageIcon) ichen;
			//if(k<7) ob[i][k] =(ImageIcon) ichdis;
		    }
		    
		    if(propkey==1) {
			ob[i][k] =(String) chnstat;
			//if(k>10) ob[i][k] =(String) new String("-2501.5");
			// else ob[i][k] =(String) new String("-102.5"); 
		    }

		    if(propkey==2) {
			ob[i][k] =(String) chnstat;
			// if(k>10) ob[i][k] =(String) new String("-2000.");
			//else ob[i][k] =(String) new String("-300."); 
		    }
		    
		    if(propkey==3) {
			ob[i][k] =(String) chnstat;
			//if(k>10) ob[i][k] =(String) new String("-0.5");
			// else ob[i][k] =(String) new String("-1.7"); 
		    }

		}
	    }
	    
	return ob;
	}
		
	public  Object[] longValues = setlongValue();
	public Object[] setlongValue() {
	    int nx = XMAX+1;	    
	    Object[] ob = new Object[nx];
	    ob[0] = new String("Y:37");
	    for(int i=1; i<nx; i++) {
		//ob[i] = data[0][i];
		ob[i] = new String("-3555.5");
	    }
	    return ob;
	}
	
	public void updateLongValue() {
	    longValues = setlongValue(); 
	}

	public synchronized void changeData(String propertyname) {
	    data = setdata(propertyname);
	    fireTableStructureChanged();
	}
	
	


        public synchronized int getColumnCount() {
            return columnNames.length;
        }
        
        public synchronized int getRowCount() {
            return data.length;
        }

        public synchronized String getColumnName(int col) {
            return columnNames[col];
        }

        public synchronized Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public synchronized Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */

        public synchronized boolean isCellEditable(int row, int col) {
	    if (col > 0) {
		    return true;
	    } else {
		return false;
	    }
	}



        public synchronized void setValueAt(Object value, int row, int col) {
	    String command = null;
	    if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of " 
                                   + value.getClass() + ")");
            }

	    if((row <= getRowCount())||(col<=getColumnCount())) {
	    try {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
		
		if (DEBUG) {
		    System.out.println("New value of data:");
		    printDebugData();
		}
	    } catch (ArrayIndexOutOfBoundsException ae) {
				
                System.out.println("Exception in setValueAt (" + row + "," + col+") : "+ae);
	    }
	    }
       }
	
        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    public Object getChannelStatus(String chnaddr, String propname) {
	
	String star = new String("*");
	String hostname = star;
	String module = star;
	String channel = star;
	int imodule = 0;
	int smodule = 0;
	int ichannel = 0;
	String chnstat = new String(" "); // present not connected channels
	ImageIcon reticon = ichnc;

	StringTokenizer st =  new StringTokenizer(chnaddr,".");
	if(st.hasMoreTokens())  hostname = st.nextToken(); 
	if(st.hasMoreTokens())  module = st.nextToken(); 
	if(st.hasMoreTokens())  channel = st.nextToken(); 
        //System.out.println("INIT:"+chnaddr+" : "+propname);
   	if(!hostname.equalsIgnoreCase(star)) {
	    if(hvft.containsKey(hostname)) { 
		//do next step
		if(!module.equalsIgnoreCase(star)) {
//****  03-Sep-05 the input parameter now is String
		    //smodule = Integer.parseInt(module) ; 
		    imodule = ((HVframe)hvft.get(hostname)).getModuleIndex(module);
//****
		    //System.out.println("Slot:"+module+" Lmodule:"+imodule);
		    if(imodule>=0) {
			if(!channel.equalsIgnoreCase(star)) {
			    ichannel = Integer.parseInt(channel) ;
			    boolean isHVON = ((HVframe)hvft.get(hostname)).HVONstatus;
			    boolean alarmSet = ((HVframe)hvft.get(hostname)).isAlarmSet();
			    boolean moduleAlarmSet = ((HVframe)hvft.get(hostname)).isModuleAlarmSet();
			    // System.out.println("MOD:"+module+" : " + module + ":" + propname+":"+hostname+":"+imodule+":"+ichannel);
			    chnstat = ((HVframe)hvft.get(hostname)).hvm[imodule].ch[ichannel].getValue(propname);
			    // System.out.println("OUT:" + propname+":"+hostname+":"+imodule+":"+ichannel+":"+chnstat);
			    if( propname.equalsIgnoreCase("CE")) {
				if(chnstat.equalsIgnoreCase("1")&& isHVON) chnstat = "1";
				if(chnstat.equalsIgnoreCase("1")&& (!isHVON)) chnstat = "0";
			    }
			    
			    if(alarmSet) {
				if(moduleAlarmSet) {
				    Vector chv =  ((HVframe)hvft.get(hostname)).hvm[imodule].getChannelsTrip(); 
				    for(Enumeration ce = chv.elements(); ce.hasMoreElements() ;) {
					int ch = ((Integer) ce.nextElement()).intValue() ;
					if(ch == ichannel)  chnstat = "3";
				    }
				} else  chnstat = "3";
			    }
			    
			}
		    }
		}
	    }
	}
	
	return chnstat;
    }
		
       // load map file to vector hvmap
    public void loadMap(BufferedReader in) {
	String raw = new  String();
	StringTokenizer st ;
	boolean EOF = false;
	int cnt = 0;
	Vector hostnames = new Vector();

	try {
	    while ((raw = in.readLine())!= null) {		
		raw = raw.trim();
		if(!raw.startsWith("#")) {
		    // process first string that should be list of host names
		    st = new StringTokenizer(raw);
		    if( cnt == 0) {
			while(st.hasMoreTokens()) {
			    hostnames.add((String) st.nextToken());
			}
		    } else {
			while(st.hasMoreTokens()) {
			    // extract of the hvchannels address: h#ss#cc# - five digits string,
			    //  
 			    //     h# is one digit hostname index in list of hosnames(first string);
			    //     ss# is two digit slot number;
			    //     cc# is two digit hv channel number;
			    int i =0;
			    int m =0;
			    int k =0;
			    String nhvaddr = new String();
			    String hvaddr =  st.nextToken();
			    try {
				int lenw=hvaddr.length();
				if ( lenw > 4 ) { 
				    String chind   = hvaddr.substring(lenw-2,lenw);
				    String modind  = hvaddr.substring(lenw-4,lenw-2);
				    String hostind = hvaddr.substring(0,lenw-4);
				    // extract of the hostname, module and channel numbers;	
				    i = Integer.parseInt(hostind);
				    m = Integer.parseInt(modind);
				    chind.toUpperCase();
				    if(chind.startsWith("S")) {
//*** 03-Sep-05: we have sumbodule here in form S0,.., so add it ro module slot number
					modind=modind+chind;    
					chind =  hvaddr.substring(5,7);
				    }
				    k = Integer.parseInt(chind);
				
				// get hostname from vector
				
				    if ( i>0 ){
					String hname = (String) hostnames.get(i-1);
					nhvaddr = hname + "." +m +"." +k;
				    }
				}
				
			    } catch (ArrayIndexOutOfBoundsException ae) {
				System.err.println("X loadMap()" + ae);
				 nhvaddr = "*";	   				
			    } catch (NumberFormatException ne ) {
                                System.err.println("X loadMap()" + ne);
                                nhvaddr = "*";     
                            } catch (IndexOutOfBoundsException ie) {
				System.err.println("X loadMap()" + ie);
				nhvaddr = "*";	 
			    }
			  
			    hvmap.add((String) nhvaddr);
			}
		    }
		    cnt++;
		}
	  
	    }
	} catch (EOFException e) {
	    EOF = true;
	} catch (IOException e) {
	    System.err.println("loadMap()" + e);
	}    
	
    }
 


        public static void main(String[] args) {

	    HVMapTable frame = new HVMapTable(new Vector(), new Hashtable(), new String());
	    File file = new File(args[0]);
	    try {
		BufferedReader in
		    = new BufferedReader(new FileReader(file));
		frame.loadMap(in);
		in.close();
	    } catch (FileNotFoundException ef) {
		System.err.println("Load Mao: " + ef);		    
	    } catch (IOException ef) {
		System.err.println("Load Map: " + ef);
	    }
	    
	    for(int i =0;i<32;i++) {
		for(int k=0;k<22;k++) {
		    //		    		   System.out.print("("+(k+22*i)+")"+ (String) frame.hvmap.get(k+22*i));
		}
		//System.out.print("\n");
	    } 
        frame.pack();
        frame.setVisible(true);
        }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }
    
    
}








