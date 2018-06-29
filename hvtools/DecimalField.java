
package hvtools;

import javax.swing.*; 
import javax.swing.text.*; 

import java.awt.Toolkit;
import java.text.*;

/**
 * Implements format input of digital numbers for table 
 * @version 1.1
 * Last update: 17-May-01
 */
public class DecimalField extends JTextField {
    private NumberFormat format;
    private double minvalue;
    private double maxvalue;
    private double oldvalue;

    public DecimalField(double minvalue, double maxvalue, int columns, NumberFormat f) {
        super(columns);
        setDocument(new FormattedDocument(f));
	format = f;
	this.minvalue = minvalue;
	this.maxvalue = maxvalue;
        setValue(minvalue);
	oldvalue = 0.0;
    }

    public double getValue() {
        double retVal = 0.0;
	
        try {
            retVal = format.parse(getText()).doubleValue();
	    if(retVal < minvalue) {
		retVal = oldvalue;
		Toolkit.getDefaultToolkit().beep();
		System.err.println("getValue: out of range: " + getText());
	    }
	    if(retVal > maxvalue) {
		retVal = oldvalue;
		Toolkit.getDefaultToolkit().beep();
		System.err.println("getValue: out of range: " + getText());
	    }
	} catch (ParseException e) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            Toolkit.getDefaultToolkit().beep();
            System.err.println("getValue: could not parse: " + getText());
        }
	oldvalue = retVal;
        return retVal;
    }
    
    public void setValue(double value) {
        setText(format.format(value));
	//System.out.println("Set text:" + value);
    }
}



