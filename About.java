import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;


class About {

    private static void showAbout() {
	final JFrame frame = new JFrame("About HVS Program");
	HtmlDemo demo = new HtmlDemo("about.html");
	
	//	JTextArea textArea = new JTextArea();
	//textArea.append("High Voltage System\n");
	//textArea.append("Version 1.3  September 30, 2005\n");
	//textArea.append("mailto: romanip@jlab.org\n");
	//Color bc= upperPane.getBackground();
	//textArea.setBackground(bc);
	//upperPane.setLayout(new BoxLayout(upperPane, BoxLayout.Y_AXIS));
	//upperPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	//buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
	//upperPane.add(textArea);

	JButton closeButton = new JButton("Close");
	closeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    frame.dispose();
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
	
	//contentPane.add(upperPane, BorderLayout.NORTH);
	contentPane.add(buttonPane, BorderLayout.SOUTH);
	
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    frame.dispose();
		}
	    });
	
	frame.pack();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	frame.setLocation(screenSize.width/2 - demo.INITIAL_WIDTH/2,
			      screenSize.height/2 - demo.INITIAL_HEIGHT/2);
	frame.setSize(demo.INITIAL_WIDTH, demo.INITIAL_HEIGHT);
	frame.show();	
	
    }

    public static void main(String[] args) {
	
	showAbout();
    
    }

}
