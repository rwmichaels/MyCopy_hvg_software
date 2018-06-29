package hvtools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ListDB extends JDialog 
                      implements ListSelectionListener {
    private static ListDB dialog;
    private static String value = "";
    private JList list;
    private DefaultListModel listModel;

    private static final String hireString = "Cancel";
    private static final String fireString = "Select";
    private JButton fireButton;
    private JTextField employeeName;
    public static String name ="";
    boolean buttonPressed = false;

    public ListDB(Frame frame, String title,
                       String labelText) {
        super(frame, title, true);

        listModel = new DefaultListModel();
        listModel.addElement("ID         DATE                      COMMENTS");


        //Create the list and put it in a scroll pane
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	list.setSelectedIndex(0);
	list.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    if (e.getClickCount() == 2) {
			fireButton.doClick();
		    }
		}
	    }); 
	list.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(list);

        JButton hireButton = new JButton(hireString);
        hireButton.setActionCommand(hireString);
        hireButton.addActionListener(new HireListener());

        fireButton = new JButton(fireString);
        fireButton.setActionCommand(fireString);
        fireButton.addActionListener(new FireListener());

        employeeName = new JTextField(21);
        employeeName.addActionListener(new HireListener());
        name = listModel.getElementAt(
                              list.getSelectedIndex()).toString();
        employeeName.setText(name);

	fireButton.setEnabled(false);
	employeeName.setText("");

        //Create a panel that uses FlowLayout (the default).
        JPanel buttonPane = new JPanel();
        buttonPane.add(employeeName);
        buttonPane.add(hireButton);
        buttonPane.add(fireButton);

        Container contentPane = getContentPane();
        contentPane.add(listScrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);
	pack();
    }
    
    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  String title,
                                  String labelText) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new ListDB(frame, title, labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static String showDialog(Component comp, String initialValue) {
        if (dialog != null) {
	    dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setSize(450,250);
	    dialog.setVisible(true);
	    
        } else {
            System.err.println("ListDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return value;
    }


    private void setValue(String newValue) {
        value = newValue;
        list.setSelectedValue(value, true);
    }



    public static void addElement(Object e) {
	dialog.listModel.addElement(e);
    }

    class FireListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    ListDB.value = (String)(list.getSelectedValue());	    
	    ListDB.dialog.setVisible(false);   
	}
    }

    //This listener is shared by the text field and the hire button
    class HireListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    ListDB.value = null;
	    ListDB.dialog.setVisible(false);   
	}
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable fire button.
                fireButton.setEnabled(false);
                employeeName.setText("");
	    }
	    if (list.getSelectedIndex() == 0) {
            //No selection, disable fire button.
                fireButton.setEnabled(false);
                employeeName.setText("");
	    } else {
		//Selection, update text field.
                fireButton.setEnabled(true);
                String name = list.getSelectedValue().toString();
                employeeName.setText(name);
            }
        }
    }


    public static void main(String s[]) {
        JFrame frame = new JFrame();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
		// System.exit(0);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }
}
