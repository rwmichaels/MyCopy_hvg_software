
package hvtools;

import javax.swing.*; 
import javax.swing.text.*; 

import java.awt.Toolkit;
import java.text.*;
import java.util.Locale;

/**
 * Implements methods to get and check formatted input from user 
 * @version 1.1
 * Last update: 17-May-01
 */
public class FormattedDocument extends PlainDocument {
    private Format format;

    public FormattedDocument(Format f) {
        format = f;
    }

    public Format getFormat() {
        return format;
    }

   public void insertString(int offs, String str, AttributeSet a) 
        throws BadLocationException {

        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(offs, currentText.length());
	//        String proposedResult = beforeOffset + str + afterOffset;
	 String proposedResult = str ;
	 //System.out.println("currStr:"+currentText+" STR:"+ str +" offs::"+offs);
 
	if (((offs == 0)&&(str.equals("-"))) || 
	      ((currentText.indexOf(".")<0) && (str.equals(".")) ) ) {
	    super.insertString(offs, str, a);
	} else {
	    try {
		format.parseObject(proposedResult);
		super.insertString(offs, str, a);
	    } catch (ParseException e) {
		Toolkit.getDefaultToolkit().beep();
		System.err.println("insertString: could not parse: "
				   + proposedResult);
	    }
	}
   }

    public void Myremove(int offs, int len) throws BadLocationException {
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(len + offs,
                                                   currentText.length());
        String proposedResult = beforeOffset + afterOffset;

        try {
            if (proposedResult.length() != 0)
                format.parseObject(proposedResult);
            super.remove(offs, len);
        } catch (ParseException e) {
            Toolkit.getDefaultToolkit().beep();
            System.err.println("remove: could not parse: " + proposedResult);
        }
    }
}
