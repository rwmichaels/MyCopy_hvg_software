
package hvtools;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Parameters - implements methods to load/save properties from/to file
 * @versioin 1.1
 * Last update: 17-May-01
 */ 
public abstract class Parameters {

    private boolean DEBUG = false;
    private String propertiesFilename;
    private String propertiesDescription;
    private String TaskDirKey = "task.dir";

    protected Properties properties = null;
     
    /** 
     * Constructor sets file name with properties
     * @param propertiesFilename String name of file with properties
     * @param propertiesDescription String property name
     */ 	
    protected Parameters(String propertiesFilename, String propertiesDescription) {
	this.propertiesFilename = propertiesFilename;
	this.propertiesDescription = propertiesDescription;
    }

    abstract protected void setDefaults(Properties defaults) ;
    abstract protected void updatePropertiesFromSettings() ;
    abstract protected void updateSettingsFromProperties() ;

    /** 
     * Open and load parameters from file in user directory 
     * @return bollean true if parameters loaded and false otherwise.
     */	
    protected boolean getParameters() {
        Properties defaults = new Properties();
        FileInputStream in = null;

	//No defaults!!!
	setDefaults(defaults);

        properties = new Properties(defaults);
        //properties = new Properties();
        String taskDir = System.getProperty(TaskDirKey);

	try {
	    String folder = System.getProperty("user.home");
	    String filesep = System.getProperty("file.separator");
            in = new FileInputStream(taskDir
				     + filesep
				     + propertiesFilename);
	    properties.load(in);

	} catch (java.io.FileNotFoundException e) {
	    in = null;
	    ErrorMessages.error("Can't find properties file. " 
					+"Using defaults.");
	    updateSettingsFromProperties();
	    return false;
	} catch (java.io.IOException e) {
	    ErrorMessages.error("Can't read properties file. " 
					+"Using defaults.");
	    updateSettingsFromProperties();
	    return false;
	} finally {
	    if (in != null) {
		try { in.close(); } catch (java.io.IOException e) { }
		in = null;
	    }
	}

	
	updateSettingsFromProperties();
	return true;	
    }

    /**
     * Saves parameter to file in user directory
     */	
    protected void saveParameters() {

	updatePropertiesFromSettings();

	if (DEBUG) {
	    System.out.println("Just set properties: " + propertiesDescription);
	    System.out.println(toString());
	}

        FileOutputStream out = null;
        String taskDir = System.getProperty(TaskDirKey);

	try {
	    String folder = System.getProperty("user.home");
	    String filesep = System.getProperty("file.separator");
            out = new FileOutputStream(taskDir
				     + filesep
				     + propertiesFilename);
	    properties.store(out, propertiesDescription);
	} catch (java.io.IOException e) {
	    ErrorMessages.error("Can't save properties to file: " + 
				propertiesFilename);
	} finally {
	    if (out != null) {
		try { out.close(); } catch (java.io.IOException e) { }
		out = null;
	    }
	}
    }
}
