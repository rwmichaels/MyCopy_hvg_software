
package hvtools;

/**
 * Implements few  methods to print error messages to stderror
 *
 * @versioin 1.0
 * Last update: 17-May-01
 */

public class ErrorMessages {
    public static boolean DEBUG = false;


    public static void error(String message) {
	//XXX Do we really want to print non-fatal messages to stderr?
	System.err.print("HVS: ");
	System.err.println(message);
    }

    public static void error(String message, Exception e) {
	if (DEBUG) {
	    if (e != null) {
	        e.printStackTrace();
	    }
	}
	//XXX Do we really want to print non-fatal messages to stderr?
	System.err.print("HVS: ");
	System.err.println(message);
    }

    public static void fatalError(String message, Exception e) {
	if (DEBUG) {
	    if (e != null) {
	        e.printStackTrace();
	    }
	}
	System.err.print("HVS: ");
	System.err.println(message);
	System.err.println("Exiting.....");
	System.exit(-1);
    }
}
