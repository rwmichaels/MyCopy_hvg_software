package hvmap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * HVMapDialog - show dialog window to  add/remove/edit  map parameters.
 * @version 1.1
 * Last update: 26-Jul-05 
 */

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See the main method for an example of using ListDialog.  The
 * basics:
 * <pre>
    HVListDialog.initialize(componentInControllingFrame,
                          "Dialog Title",
                          "A description of the list:");
 * </pre>
 */
public class HVMapDialog extends JDialog 
                           implements ListSelectionListener, ActionListener  {
    private static HVMapDialog dialog;
    private static String value = "";
    private JList list;
    private DefaultListModel listModel;
    private HVMapList hvList;

    private static final String addString = "Add";
    private static final String newString = "New";
    private static final String editString = "Edit";
    private static final String removeString = "Delete";
    private static final String okString = "Apply";
    private static final String closeString = "Close";
    private JButton newButton;
    private JButton editButton;
    private JButton removeButton;
    private JButton okButton;
    private JButton closeButton;

    private JTextField inputName;
    private JTextField inputSizeX;
    private JTextField inputSizeY;
    private JTextField inputStartX;
    private JTextField inputStartY;
    private JTextField inputFileName;
    private JCheckBox  checkX;
    private JCheckBox  checkY;
    ImageIcon left  = new ImageIcon("./images/2left.gif"); //left arow icon
    ImageIcon right  = new ImageIcon("./images/2right.gif"); //right arow icon
    ImageIcon up  = new ImageIcon("./images/2up.gif"); //up arow icon
    ImageIcon down  = new ImageIcon("./images/2down.gif"); //down arow icon
    private boolean isChanged = false;

    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  String title,
                                  String labelText) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new HVMapDialog(frame, title, labelText);
	dialog.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dialog.closeDialog();
		    //System.exit(0);
		}
	    });
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static String showDialog(Component comp, String initialValue) {
        if (dialog != null) {
	    //            dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
	    
        } else {
            System.err.println("HVMapDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return value;
    }

    private void setValue(String newValue) {
        value = newValue;
        list.setSelectedValue(value, true);
    }

    private HVMapDialog(Frame frame, String title,
                       String labelText) {
        super(frame, title, true);

        Border empty5 = BorderFactory.createEmptyBorder(5,5,5,5);
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        Border compound5 = BorderFactory.createCompoundBorder(empty5,loweredbevel);

        Border empty510 = BorderFactory.createEmptyBorder(5,10,5,10);
        Border compound510 = BorderFactory.createCompoundBorder(empty510,loweredbevel);
 
        Border blackline = BorderFactory.createLineBorder(Color.black);
        Border empty10 = BorderFactory.createEmptyBorder(10,10,10,10);
        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border compound1 = BorderFactory.createCompoundBorder(
                                      raisedbevel,loweredbevel );
        Border compound2 = BorderFactory.createCompoundBorder(
                                      blackline, empty10);
        Border compound3 = BorderFactory.createCompoundBorder(
                                       compound1,empty10);
 
	hvList = new HVMapList();

        listModel = new DefaultListModel();
	if (hvList.getMapNum() == 0) {
	    //		    listModel.addElement((String)hvList.defaultName);
		    //System.out.println("add default host: "+hvList.defaultName);
	}

	for(int i=0;i<hvList.getMapNum();i++) {
	    listModel.addElement((String)hvList.getMapName().get(i));
	}

        //Create the list and put it in a scroll pane
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	//       list.setSelectedIndex(0);
        list.addListSelectionListener(this);

 
        JScrollPane listScrollPane = new JScrollPane(list);
	        listScrollPane.setPreferredSize(new Dimension(250, 60));
        //XXX: Must do the following, too, or else the scroller thinks
        //XXX: it's taller than it is:
        listScrollPane.setMinimumSize(new Dimension(250, 60));
        listScrollPane.setAlignmentX(CENTER_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
	JLabel label = new JLabel(labelText);
        //label.setLabelFor(list);
	label.setHorizontalAlignment(JLabel.CENTER );
        listPane.add(label);
	listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScrollPane);

        JPanel upbuttonPane = new JPanel();
        upbuttonPane.setLayout(new BoxLayout(upbuttonPane, BoxLayout.Y_AXIS));

	newButton = new JButton(newString);
	newButton.setActionCommand(newString);
        newButton.addActionListener(this);
	newButton.setToolTipText("Press to create new map configuration");
	//newButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5,10 ));
	newButton.setPreferredSize(new Dimension(80,30));

        removeButton = new JButton(removeString);
        removeButton.setActionCommand(removeString);
        removeButton.addActionListener(new RemoveListener());
	//removeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5,10 ));
	removeButton.setToolTipText("Press to delete selected map configuration");
	removeButton.setPreferredSize(new Dimension(80,30));


	editButton = new JButton(editString);
	editButton.setActionCommand(editString);
	editButton.setToolTipText("Press to edit selected map configuration");
	//editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5,10 ));
        editButton.addActionListener(this);
	editButton.setPreferredSize(new Dimension(80,30));


	upbuttonPane.add(Box.createRigidArea(new Dimension(0,15)));
	upbuttonPane.add(newButton);
	upbuttonPane.add(Box.createRigidArea(new Dimension(0,10)));
	upbuttonPane.add(editButton);
	upbuttonPane.add(Box.createRigidArea(new Dimension(0,10)));
	upbuttonPane.add(removeButton);
	upbuttonPane.add(Box.createRigidArea(new Dimension(0,5)));

        JPanel upPane = new JPanel();
        upPane.setLayout(new BoxLayout(upPane, BoxLayout.X_AXIS));
	upPane.add(listPane);
	upPane.add(Box.createRigidArea(new Dimension(15,0)));
	upPane.add(upbuttonPane);
        upPane.setBorder(compound2);
	
        okButton = new JButton(okString);
        okButton.setActionCommand(okString);
        okButton.addActionListener(new OkListener());


        closeButton = new JButton(closeString);
        closeButton.setActionCommand(closeString);
        closeButton.addActionListener(new CloseListener());

	// input fields
        inputName = new JTextField(15);
        //inputName.addActionListener(new AddListener());
	//        inputName.setText(name);
	inputName.setFont(new Font("Serif", Font.PLAIN, 14));
	inputName.setForeground(Color.blue);
	inputName.setToolTipText("Input name of map configuration");
	inputName.setEditable(false);

        inputSizeX = new JTextField(5);
	// inputSizeX.addActionListener(new AddListener());
	// inputSizeX.setText(name);
	inputSizeX.setToolTipText("Input size in range of(1-100) for X axis of the map");
	inputSizeX.setFont(new Font("Serif", Font.PLAIN, 14));
	inputSizeX.setEditable(false);

        inputSizeY = new JTextField(5);
        //inputSizeY.addActionListener(new AddListener());
        //inputSizeY.setText(name);
	inputSizeY.setToolTipText("Input size in range of(1-100) for Y axis of the map");
	inputSizeY.setFont(new Font("Serif", Font.PLAIN, 14));
	inputSizeY.setEditable(false);

	//**** start index input fields
        inputStartX = new JTextField(5);
	inputStartX.setToolTipText("Input start index in range of(0-100) for X axis of the map");
	inputStartX.setFont(new Font("Serif", Font.PLAIN, 14));
	inputStartX.setEditable(false);

        inputStartY = new JTextField(5);
	inputStartY.setToolTipText("Input start index in range of(0-100) for Y axis of the map");
	inputStartY.setFont(new Font("Serif", Font.PLAIN, 14));
	inputStartY.setEditable(false);

        inputFileName = new JTextField(15);
        //inputFileName.addActionListener(new AddListener());
        //inputFileName.setText(name);
	inputFileName.setToolTipText("Input filename(without any path!) where map data are stored");
	inputFileName.setFont(new Font("Serif", Font.PLAIN, 14));
	inputFileName.setEditable(false);
	
	// check box
	checkX = new JCheckBox("incremental",right);
	checkX.addItemListener(new CheckBoxListener());
	checkX.setToolTipText("Press to change direction for X axis");
	checkX.setEnabled(false);
	checkX.setSelectedIcon(left);

	checkY = new JCheckBox("incremental",up);
	checkY.addItemListener(new CheckBoxListener());
	checkY.setToolTipText("Press to change direction for Y axis");
	checkY.setEnabled(false);
	checkY.setSelectedIcon(down);


	// labels
	JLabel	nameLabel = new JLabel("Map Name");
        nameLabel.setLabelFor(inputName);
	JLabel	fileLabel = new JLabel("Map File Name");
        fileLabel.setLabelFor(inputFileName);
	JLabel	sizeXLabel = new JLabel("Size X");
	JLabel	sizeYLabel = new JLabel("Size Y");
	JLabel	startXLabel = new JLabel("First X");
	JLabel	startYLabel = new JLabel("First Y");
        sizeXLabel.setLabelFor(inputSizeX);
        sizeYLabel.setLabelFor(inputSizeY);
        startXLabel.setLabelFor(inputStartX);
        startYLabel.setLabelFor(inputStartY);
	JLabel	dirLabel = new JLabel("Direction");

	// map name panel
	JPanel namePane = new JPanel();
	namePane.setLayout(new BoxLayout(namePane, BoxLayout.X_AXIS));
	namePane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5,10 ));
	namePane.add(nameLabel);
	namePane.add(Box.createRigidArea(new Dimension(10,0)));
	namePane.add(inputName);

	// file name panel
	JPanel filePane = new JPanel();
	filePane.setLayout(new BoxLayout(filePane, BoxLayout.X_AXIS));
	filePane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5,10 ));
	filePane.add(fileLabel);
	filePane.add(Box.createRigidArea(new Dimension(10,0)));
	filePane.add(inputFileName);

	// size x and direction panel
	JPanel sizeXPane = new JPanel();
	sizeXPane.setLayout(new BoxLayout(sizeXPane, BoxLayout.X_AXIS));
	sizeXPane.setBorder(BorderFactory.createEmptyBorder(5,10, 5, 10));
	sizeXPane.add(sizeXLabel);
	sizeXPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeXPane.add(inputSizeX);
	sizeXPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeXPane.add(startXLabel);
	sizeXPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeXPane.add(inputStartX);

	sizeXPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeXPane.add(checkX);

	// size y and direction panel
	JPanel sizeYPane = new JPanel();
	sizeYPane.setLayout(new BoxLayout(sizeYPane, BoxLayout.X_AXIS));
	sizeYPane.setBorder(BorderFactory.createEmptyBorder(5,10, 5, 10));
	sizeYPane.add(sizeYLabel);
	sizeYPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeYPane.add(inputSizeY);
	sizeYPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeYPane.add(startYLabel);
	sizeYPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeYPane.add(inputStartY);

	sizeYPane.add(Box.createRigidArea(new Dimension(10,0)));
	sizeYPane.add(checkY);
	
	// parameters panel
	JPanel paramPane = new JPanel();
	paramPane.setLayout(new BoxLayout(paramPane, BoxLayout.Y_AXIS));
	paramPane.add(namePane, BorderLayout.CENTER);
	paramPane.add(filePane, BorderLayout.CENTER);
	paramPane.add(sizeXPane, BorderLayout.CENTER);
	paramPane.add(sizeYPane, BorderLayout.CENTER);
	paramPane.setBorder(compound2);
        //Create a panel that uses FlowLayout (the default).
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
 
	//	buttonPane.add(inputName);
	buttonPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
	//	buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(okButton);
	//        buttonPane.add(removeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(80, 0)));
        buttonPane.add(closeButton);

        Container contentPane = getContentPane();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(upPane, BorderLayout.CENTER);
	contentPane.add(Box.createRigidArea(new Dimension(0,10)));
        contentPane.add(paramPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.CENTER);

        setEditableInput(false);
	inputName.setEditable(false);
	//	System.out.println("init_list_size:"+listModel.getSize());
	if (listModel.getSize()>0) {
	    list.setSelectedIndex(0);
	    String s = (String)hvList.getMapSize().elementAt(0);
	    String dir = hvList.getToken(3, s);
	    if(dir.equalsIgnoreCase("0")) checkX.setSelected(true);
	    dir = hvList.getToken(4, s);
	    if(dir.equalsIgnoreCase("0")) checkY.setSelected(true);

	} else {
	    removeButton.setEnabled(false); 
	    editButton.setEnabled(false); 
	}
	
	// set axis direction selection


	//        String name = listModel.getElementAt(
        //                      list.getSelectedIndex()).toString();
	
	isChanged = false;
	
	pack();
	

    }


    
    public void setEnabledUpButtons(boolean b) {
	newButton.setEnabled(b);
	editButton.setEnabled(b);    
	removeButton.setEnabled(b);    
    }

    public void setEditableInput(boolean b) {
	inputFileName.setEditable(b);
	inputSizeX.setEditable(b);
	inputSizeY.setEditable(b);
	inputStartX.setEditable(b);
	inputStartY.setEditable(b);
	checkX.setEnabled(b);
	checkY.setEnabled(b);    
    }

    // Calls when "Edit" or "New" buttons are pressed 
    public void actionPerformed(ActionEvent e) {
        System.out.println("in actionPerformed...");
        if (e.getActionCommand().equals(newString)) {
	    // enable all inputs field to enter text
	    inputName.setEditable(true);
	    setEditableInput(true);
	    setEnabledUpButtons(false);
	    inputName.setText("");
	    inputFileName.setText("");
	    inputSizeX.setText("");
	    inputSizeY.setText("");
	    inputName.requestFocus();
	    isChanged = true;
	    closeButton.setEnabled(false);
	    return;
        }
        if (e.getActionCommand().equals(editString)) {
	    // enable input fields to enter exept inputName field
	    setEditableInput(true);
	    setEnabledUpButtons(false);
	    inputFileName.requestFocus();
	    isChanged = true;
	    closeButton.setEnabled(false);
	    return;
        }
 	
    }
    
    
    
    class CheckBoxListener implements ItemListener {
	public void  itemStateChanged(ItemEvent e) {
            //This method can be called only if
            //there's a valid selection
            //so go ahead and remove whatever's selected.
	    Object source = e.getItemSelectable();
	    if (source == checkX) {
		//System.out.println("source:"+source);
		if (e.getStateChange() == ItemEvent.DESELECTED) {
		    ((JCheckBox)source).setText("incremental");
		} else ((JCheckBox)source).setText("decremental");
	    }
	    
	    if (source == checkY) {
		    //System.out.println("source:"+source);
		    if (e.getStateChange() == ItemEvent.DESELECTED) {
			((JCheckBox)source).setText("incremental");
		    } else ((JCheckBox)source).setText("decremental");
	    }
			    
	}
    }

    class RemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    //System.out.println("in RemoveListener...");
            //This method can be called only if
            //there's a valid selection
            //so go ahead and remove whatever's selected.
            int index = list.getSelectedIndex();
	    // remove name from hvList	  	  

	    hvList.removeMapName((String)listModel.elementAt(index));
            listModel.remove(index); // after that valueChanged is called!!!
	    //	    isChanged = true;

            int size = listModel.getSize();

            if (size == 0) {
		removeButton.setEnabled(false);
		editButton.setEnabled(false);
            //Nobody's left, disable firing.
		//
            } else {
            //Adjust the selection.
                if (index == listModel.getSize())//removed item in last position
                    index--;
		System.out.println("index_in_remove:"+index);
                list.setSelectedIndex(index);   //otherwise select same index
            }
        }
    }

    //This listener is  for the 'Ok' button
    class OkListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    //System.out.println("in ApplyListener...");
	    String s;
            //User didn't type in a name...
            if (inputName.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (inputFileName.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (inputSizeX.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (inputSizeY.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (inputStartX.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (inputStartY.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            int index = list.getSelectedIndex();
	    //	    if (index<0) index =0;
            int size = listModel.getSize();

	    //System.out.println("index:"+index + " size:"+size);
	    
            //If no selection or if item in last position is selected,
            //add the new add to end of list, and select new add.
	    s = inputName.getText();
	    
	    s=s.trim();
	    if ((listModel.contains(s))&&(inputName.isEditable())) {
                Toolkit.getDefaultToolkit().beep();
                return;		
	    }
	    
	    if (inputName.isEditable()) {
		// add mode
		// listModel.addElement(s);
		
		//	listModel.insertElementAt(s, index+1);
		//list.setSelectedIndex(index+1);
		//hvList.insertMapName(index+1,s);
		
		listModel.addElement(s);
		index++;
		list.setSelectedIndex(index);
		hvList.addMapName(s);
		hvList.addFileName(inputFileName.getText().trim());
		s=inputSizeX.getText().trim()+":"+inputSizeY.getText().trim();
		String chkb = new String("1");
		if(checkX.isSelected()) chkb="0";
		s=s+":"+chkb;
		if(checkY.isSelected()) chkb="0";
		else chkb="1";
		s=s+":"+chkb;
		s=s+ ":"+ inputStartX.getText().trim()+":"+inputStartY.getText().trim();
		hvList.addMapSize(s);
		
	    

	    } else if(inputFileName.isEditable() && !inputName.isEditable()) {
		// edit mode
		System.out.println("edit mode");
		//get selected map name
		s=(String)listModel.elementAt(index);		
		// get index this map in hvList.MapName
		int indx = hvList.getMapName().indexOf(s);

		//replace other parameters by index 'indx'
		hvList.getFileName().set(indx,inputFileName.getText().trim());
		s=inputSizeX.getText().trim()+":"+inputSizeY.getText().trim();
		String chkb = new String("1");
		if(checkX.isSelected()) chkb="0";
		s=s+":"+chkb;
		
		if(checkY.isSelected()) chkb="0";
		else chkb="1";
		s=s+":"+chkb;
		s=s+ ":"+ inputStartX.getText().trim()+":"+inputStartY.getText().trim();
		hvList.getMapSize().set(indx,s);

	    } else {
		Toolkit.getDefaultToolkit().beep();
		return;			
	    }
	    setEnabledUpButtons(true);
	    setEditableInput(false);
	    inputName.setEditable(false); 
	    closeButton.setEnabled(true);
	    //	    hvList.addHostName(s);
	    //    System.out.println("aplly_index:"+index);
      	    list.setSelectedIndex(index);
	    isChanged = false;
            //Otherwise insert the new add after the current selection,
            //and select new add.
	    
	}
    }
    
    //This listener is shared by close button
    class CloseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    //System.out.println("in closeListener...");
	    closeDialog();
	    //if(listModel.getSize()>0)
	}
    }

    public void closeDialog() {
	if (isChanged) { 
	    // no Apply button is pressed
	    // dialog: save new parameters?
	    if(quitConfirmed(null ,"Yes", "Apply new changes?" ,"Save parameters")) {
		okButton.doClick(); // apply changes
		hvList.saveParameters(); // save parameters
	    }
	    
	    isChanged = false;
	} else {
	    hvList.saveParameters(); //save parameters to file
	}
	    setEnabledUpButtons(true);
	    setEditableInput(false);
	    inputName.setEditable(false); 
	    HVMapDialog.dialog.setVisible(false);   
    }
    
    public void valueChanged(ListSelectionEvent e) {
        //stem.out.println("in valueChanged...");
	if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable remove button.
		System.out.println(list.getSelectedIndex());
                removeButton.setEnabled(false);
                editButton.setEnabled(false);
		inputName.setText("");
		inputFileName.setText("");
		inputSizeX.setText("");
		inputSizeY.setText("");
		inputStartX.setText("0");
		inputStartY.setText("0");

            } else {
            //Selection, update text field.
                removeButton.setEnabled(true);
                editButton.setEnabled(true);
                String name = list.getSelectedValue().toString();
                inputName.setText(name);
		int indx = hvList.getMapName().indexOf(name);
		if(indx!=-1) {
		    inputFileName.setText((String)hvList.getFileName().elementAt(indx));
		    String s = (String)hvList.getMapSize().elementAt(indx);
		    inputSizeX.setText(hvList.getToken(1,s));
		    inputSizeY.setText(hvList.getToken(2,s));
		    String dir = hvList.getToken(3, s);
		    if(dir.equalsIgnoreCase("0")) checkX.setSelected(true);
		    dir = hvList.getToken(4, s);
		    if(dir.equalsIgnoreCase("0")) checkY.setSelected(true);
		    inputStartX.setText(hvList.getToken(5,s));
		    inputStartY.setText(hvList.getToken(6,s));

		}
            }
        }
    }


    private boolean quitConfirmed(JFrame frame,String bOK, String message, String title) {
        String s1 = bOK;
        String s2 = "No";
        Object[] options = {s1, s2};
        int n = JOptionPane.showOptionDialog(frame,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                s1);
        if (n == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    

    /**
     * This is here so that you can view ListDialog even if you
     * haven't written the code to include it in a program. 
     */
    public static void main(String[] args) {
        String[] names = {"Arlo", "Cosmo", "Elmo", "Hugo",
                          "Jethro", "Laszlo", "Milo", "Nemo",
                          "Otto", "Ringo", "Rocco", "Rollo"};
        JFrame f = new JFrame("Name That Baby");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                 System.exit(0);
            }
        });

        JLabel intro = new JLabel("The chosen name:");

        final JLabel name = new JLabel("Cosmo");
        intro.setLabelFor(name);
        name.setForeground(Color.black);

        JButton button = new JButton("Pick a new name...");
        HVMapDialog.initialize(f, "Name Chooser",
                              "Map List");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedName = HVMapDialog.showDialog(null,
                                                            name.getText());
                name.setText(selectedName);
            }
        });

        JPanel contentPane = new JPanel();
        f.setContentPane(contentPane);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        contentPane.add(intro);
        contentPane.add(name);
        contentPane.add(Box.createRigidArea(new Dimension(0,10)));
        contentPane.add(button);
        intro.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        name.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        f.pack();
        f.setVisible(true);
    }
}




