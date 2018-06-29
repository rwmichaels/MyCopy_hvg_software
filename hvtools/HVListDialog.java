
package hvtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * HVListDialog - show dialog window to add/remove hostname of hvframe to HVFrameList
 * @version 1.1
 * Last update: 01-March-02 
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
public class HVListDialog extends JDialog 
                           implements ListSelectionListener  {
    private static HVListDialog dialog;
    private static String value = "";
    private JList list;
    private DefaultListModel listModel;
    private HVFrameList hvList;

    private static final String addString = "Add";
    private static final String removeString = "Remove";
    private static final String closeString = "Close";
    private JButton removeButton;
    private JTextField inputName;

    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  String title,
                                  String labelText) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new HVListDialog(frame, title, labelText);
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
            System.err.println("HVListDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return value;
    }

    private void setValue(String newValue) {
        value = newValue;
        list.setSelectedValue(value, true);
    }

    private HVListDialog(Frame frame, String title,
                       String labelText) {
        super(frame, title, true);

	hvList = new HVFrameList();

        listModel = new DefaultListModel();
	if (hvList.getHostNum() == 0) {
		    listModel.addElement((String)hvList.defaultName);
		    System.out.println("add default host: "+hvList.defaultName);
	}

	for(int i=0;i<hvList.getHostNum();i++) {
	    listModel.addElement((String)hvList.getHostName().get(i));
	}

        //Create the list and put it in a scroll pane
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
 
        JScrollPane listScrollPane = new JScrollPane(list);
	        listScrollPane.setPreferredSize(new Dimension(250, 80));
        //XXX: Must do the following, too, or else the scroller thinks
        //XXX: it's taller than it is:
        listScrollPane.setMinimumSize(new Dimension(250, 80));
        listScrollPane.setAlignmentX(LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
	JLabel label = new JLabel(labelText);
        label.setLabelFor(list);
        listPane.add(label);
	listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScrollPane);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton addButton = new JButton(addString);
        addButton.setActionCommand(addString);
        addButton.addActionListener(new AddListener());

        removeButton = new JButton(removeString);
        removeButton.setActionCommand(removeString);
        removeButton.addActionListener(new RemoveListener());

        JButton closeButton = new JButton(closeString);
        closeButton.setActionCommand(closeString);
        closeButton.addActionListener(new CloseListener());

        inputName = new JTextField(15);
        inputName.addActionListener(new AddListener());
        String name = listModel.getElementAt(
                              list.getSelectedIndex()).toString();
        inputName.setText(name);
	inputName.setFont(new Font("Serif", Font.PLAIN, 16));

        //Create a panel that uses FlowLayout (the default).
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
 
	buttonPane.add(inputName);
	buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(addButton);
        buttonPane.add(removeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(closeButton);

        Container contentPane = getContentPane();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.CENTER);
	
	pack();
    }

   class RemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //This method can be called only if
            //there's a valid selection
            //so go ahead and remove whatever's selected.
            int index = list.getSelectedIndex();

	    // remove name from hvList
	    
	    hvList.removeHostName((String)listModel.elementAt(index));
            listModel.remove(index);

            int size = listModel.getSize();

            if (size == 0) {
            //Nobody's left, disable firing.
                removeButton.setEnabled(false);

            } else {
            //Adjust the selection.
                if (index == listModel.getSize())//removed item in last position
                    index--;
                list.setSelectedIndex(index);   //otherwise select same index
            }
        }
    }

    //This listener is shared by the text field and the add button
    class AddListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    String s;
            //User didn't type in a name...
            if (inputName.getText().equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            int index = list.getSelectedIndex();
            int size = listModel.getSize();
	    //System.out.println("index:"+index + " size:"+size);
	    
            //If no selection or if item in last position is selected,
            //add the new add to end of list, and select new add.
	    s = inputName.getText();
	    // comment 01-March-02
	    //	    s = s.toLowerCase();
	    // check if no port number
	    if (s.indexOf(":") == -1) {
		s = s + ":" + hvList.defaultPort;
	    }   
	    if (s.endsWith(":")) {
		s = s + hvList.defaultPort;
	    }
	    
	    if (listModel.contains(s)) {
                Toolkit.getDefaultToolkit().beep();
                return;		
	    }
	    
	    // listModel.addElement(s);
	    if((index == 0)&&(size <= 1)) {
		listModel.addElement(s);
		list.setSelectedIndex(index);
		hvList.addHostName(s);
	    } else {
		listModel.insertElementAt(s, index+1);
		list.setSelectedIndex(index+1);
		hvList.insertHostName(index+1,s);
	    }

	    
	    //	    hvList.addHostName(s);
	    //list.setSelectedIndex(size);
	    
            //Otherwise insert the new add after the current selection,
            //and select new add.
	    
	}
    }
 
   //This listener is shared by close button
    class CloseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(listModel.getSize()>0) {
		hvList.saveParameters();
	    }
	    HVListDialog.dialog.setVisible(false);   
        }
    }


    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable remove button.
                removeButton.setEnabled(false);
                inputName.setText("");

            } else {
            //Selection, update text field.
                removeButton.setEnabled(true);
                String name = list.getSelectedValue().toString();
                inputName.setText(name);
            }
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
        HVListDialog.initialize(f, "Name Chooser",
                              "Host list of HV frames");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedName = HVListDialog.showDialog(null,
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




