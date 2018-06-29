
package hvtools;

import java.util.*;
import java.lang.*;

/**
 * UpAttribute implements methods to store, geting, setting 
 * hvchannels attributes
 * @version 1.2 
 * Last update: 05-Nov-12
 * update: 17-May-01
 * 05 Nov 2012 : fix problem with respond "Bad Property Name" on command "ATTR"
 */
public class UpAttribute {
    /**
     * name of property
     */
    public String name = null;
    /**
     * Hashtable to store attributes by key
     */	
    public Hashtable attr = new Hashtable();
    public boolean readonly = false;
    /**
     * Number of channel parameters attributes
     */	
    public final int NATTR = 6;
    /**
     * Names of Keys of attributes stored in hashtable
     */
    public final String[] ATTRKey = {
	"label", "units", "protection", "type", "range", "format"
    };
    
    /**
     * Constructor init hashtable with empty attributes 
     */
    public UpAttribute() {
	for(int i=0;i<NATTR;i++) attr.put(ATTRKey[i],new String());
    }
    
    /**
     * Constructor init hashtable with empty attributes and set
     * parameter name
     * @param name String name of parameter
     */
   public UpAttribute(String name) {
	this.name = name;
	for(int i=0;i<NATTR;i++) attr.put(ATTRKey[i],new String());
	
    }
    
    /**
     * Sets attributes from input string to hashtable
     * @param inpattr String with atributes delimited by blank character
     */ 
    public void setAttr(String inpattr) {
	StringTokenizer s = new StringTokenizer(inpattr.trim()); 
	int i = 0;
	/* 5 Nov 2012 : workaround with issue "Bad Property Name" */
        String[] sattr = new String[NATTR];
        while (s.hasMoreTokens()) {
            sattr[i]=s.nextToken();
            //System.out.println(s.nextToken );                                
            i++;
        }
        if( (!sattr[0].equals("Bad"))&&(i==NATTR)) { /* it is OK */
	    for(int ik=0;ik<NATTR;ik++) 
		attr.put(ATTRKey[ik],sattr[ik]);
	}
        else { /* add dummy attributes */
            attr.put(ATTRKey[0],sattr[0]+"_"+name);
            attr.put(ATTRKey[1],"**");
            attr.put(ATTRKey[2],"M");
            attr.put(ATTRKey[3],"N_");
            attr.put(ATTRKey[4],"-1_0_0.1");
            attr.put(ATTRKey[5],"%1.0f");

        }

	/*
	while (s.hasMoreTokens()) {
	    attr.put(ATTRKey[i] ,s.nextToken());
	    //System.out.println(s.nextToken );
	    i++;
	}
	*/

    }	

    /**
     * Returns attribute from hashtable at specified key
     * @param key String name of key of attribute
     */	
    public String getAttr(String key) {
	return (String)attr.get(key);
    }
    
    /**
     * Returns substring delimited by '_' character
     * @return String substring
     */
    public String getSubstring(String s, int index) {
	String a = null;
        int bind = 0;
        int eind = 1;
	int i = 0;
        boolean br = false;
        int lind= s.lastIndexOf("_",0);
	while (!br) {
            eind = s.indexOf("_",bind);
	    if (eind == lind) {
                eind=s.length();
                br =true;
            }
	    a = s.substring(bind,eind);
	    
	    if(i==index) break;
        //System.out.println(":"+a+" ,b:"+bind+" e:"+eind );
            eind=eind +1;
            bind = eind;
	    i++;
	}
	return a;
    }

    /**
     * Returns minimal value from attribute 'range'
     * @return double 
     */	
    public double getMinRange() {
	String s = getAttr("range");
	String ret = getSubstring(s,0);
	double d = new Double(ret).doubleValue();
	return d;
    }


    /**
     * Returns maximal value from attribute 'range'
     * @return double 
     */	
     public double getMaxRange() {
	String s = getAttr("range");
	String ret = getSubstring(s,1);
	double d = new Double(ret).doubleValue();
	return d;
    }


    /**
     * Returns step value from attribute 'range'
     * @return double 
     */	 
    public double getStepRange() {
	String s = getAttr("range");
	String ret = getSubstring(s,2);
	double d = new Double(ret).doubleValue();
	return d;
    }

    /**
     * Returns name of property
     * @return String name of property
     */	
    public String getName() {
	return name;
    }
    
 
    /**
     * Sets name of property
     * @param name String name of property
     */	
   public void setName(String parname) {
	name = parname;
    }
    
    
}
