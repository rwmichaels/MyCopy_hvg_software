package hvmap;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;

import hvtools.*;

/**
 * Implements methods to manipulate with list of hvmaps 
 * @version 1.1
 * 
 * Last update: 22-Aug-2005: Add properties for start index of X and Y axises
 *                         
 */
public class HVMapList extends Parameters {
    //default parameters
    private int   defaultNum = 1;
    public String defaultName = "hvmap1";
    public String defaultX = "2";
    public String defaultY = "3";
    public String defaultXdir = "1";       
    public String defaultYdir = "1";
    public String defaultFileName = "hvmap1.map";
    // parameter names   
    private int nMaps = 0;
    private String numMaps = "num.maps";
    private String nameMaps = "name.maps";
    private String sizeX = "size.x";
    private String sizeY = "size.y";
    private String dirX = "dir.x";
    private String dirY = "dir.y";
    private String indX = "start.x";
    private String indY = "start.y";
    private String fileName = "file.name";
    private static String propsFileName = "HVmaps.conf"; 
    private static String propsTitleName = "HV Maps List" ;

    private boolean loadstatus = false;
    private Vector mapNameList = new Vector();
    private Vector mapSizeList = new Vector(); // stored in form: "x:y:dirX:dirY"
    private Vector mapFileList = new Vector();
    

    /**
     * Constructor init and load parameters from file HVMapList.props
     */
    public HVMapList() {
	super(propsFileName, propsTitleName);	
	loadstatus = getParameters();
    }
    

    /**
     * Returns status of loaded parameters.
     * @return boolean if true - parameters loaded, else false
     */   
    public boolean getStatus() {
	return loadstatus;
    }  
    
    // override
    public void saveParameters() {
	super.saveParameters();
    }


    /**
     * Setup parameters values to default.
     * @param defaults Properties
     */  
    protected void setDefaults(Properties defaults) {
	//        defaults.put(numMaps, new Integer(defaultNum).toString() );
	//	defaults.put(nameMaps, defaultName);
	//	defaults.put(sizeX, defaultX);
	//	defaults.put(sizeY, defaultY);
	//	defaults.put(dirX, defaultXdir);
	//	defaults.put(dirY, defaultYdir);
	//	defaults.put(fileName, defaultFileName);
    }
	

    /**
     * Gets properties values from file
     */	
    protected void updateSettingsFromProperties() {
        try {
            String tmp;
	    String names;
	    Vector vtmp1;
	    Vector vtmp2;
	    Vector vtmp3;
	    Vector vtmp4;
	    Vector vtmp5;
	    Vector vtmp6;
	    // get number of maps

            tmp = properties.getProperty(numMaps);
            nMaps = Integer.parseInt(tmp);
	    // get names of HVMaps
	    names = properties.getProperty(nameMaps);
	    mapNameList = parseList(names);

	    // get size X
	    tmp = properties.getProperty(sizeX);	    
	    vtmp1 = parseList(tmp);
	    //System.out.println("dbg: "+tmp+" : "+vtmp1);
	    // get size Y
	    tmp = properties.getProperty(sizeY);	    
	    vtmp2 = parseList(tmp);
	    //System.out.println("dbg: "+tmp+" : "+vtmp2);

	    // get  direction X
	    tmp = properties.getProperty(dirX);
	    vtmp3 =  parseList(tmp);
	    //System.out.println("dbg: "+tmp+" : "+vtmp3);

	    // get direction Y
	    tmp = properties.getProperty(dirY);
	    vtmp4 =  parseList(tmp);
	    //System.out.println("dbg: "+tmp+" : "+vtmp4);

	    // get start index X
	    tmp = properties.getProperty(indX);
	    vtmp5 =  parseList(tmp);
	    //System.out.println("dbg: "+tmp+" : "+vtmp5);

	    // get start index Y
	    tmp = properties.getProperty(indY);
	    vtmp6 =  parseList(tmp);
	    //System.out.println("dbg: "+tmp+" : "+vtmp6);
	    
	    
	    nMaps = mapNameList.size();
	    for(int i=0; i<nMaps; i++) {
		names="";
		names = names+(String)vtmp1.get(i) +":"+(String)vtmp2.get(i)+":"+
		    (String)vtmp3.get(i) +":"+(String)vtmp4.get(i)+":"+
		    (String)vtmp5.get(i) +":"+(String)vtmp6.get(i); 
		mapSizeList.add((String)names);	    
	    }

	    // get file name list
	    tmp = properties.getProperty(fileName);
	    mapFileList =  parseList(tmp);
	    
	    
        } catch (NumberFormatException e) {
            // we don't care if the property was of the wrong format,
            // they've all got default values. So catch the exception
            // and keep going.
        }
    }

    /**
     * Store properties value in file
     */	
    protected void updatePropertiesFromSettings() {
	String names = "";
	String tmp = "";
	String tmp1 = "";
	String tmp2 = "";
	String tmp3 = "";
	String tmp4 = "";
	String tmp5 = "";
	String tmp6 = "";

	nMaps = mapNameList.size();
        properties.put(numMaps,
                       new Integer(nMaps).toString());

	for(int i=0; i<nMaps; i++) {
	    names = names+(String)mapNameList.get(i) +" "; 
	}
        properties.put(nameMaps,
                       names);

	for(int i=0; i<nMaps; i++) {
	    tmp = (String)mapSizeList.get(i);
 	    tmp1 = tmp1 + " " + getToken(1,tmp);// size X
	    tmp2 = tmp2 + " " + getToken(2,tmp);// size Y  
	    tmp3 = tmp3 + " " + getToken(3,tmp);// direction X
	    tmp4 = tmp4 + " " + getToken(4,tmp);// direction Y
	    tmp5 = tmp5 + " " + getToken(5,tmp);// start index X
	    tmp6 = tmp6 + " " + getToken(6,tmp);// start index Y
	    
	    
	}
	properties.put(sizeX,tmp1);
	properties.put(sizeY,tmp2);
	properties.put(dirX,tmp3);
	properties.put(dirY,tmp4);
	properties.put(indX,tmp5);
	properties.put(indY,tmp6);

	names="";
	for(int i=0; i<nMaps; i++) {
	    names = names+(String)mapFileList.get(i) +" "; 
	}
        properties.put(fileName, names);	
    }
    

    /**
     * Sets number of maps to the property nMaps
     * @param mapnum int number of hv maps
     */
    public void setMapNum(int mapnum) {
	this.nMaps = mapnum;
    }

   /**
     * Returns number of maps from property  nMaps
     * @return int number of maps
     */
     public  int getMapNum() {
	return nMaps;
    }
 
     
    /**
     * Sets list of map names of hvmaps to the property mapNameList
     * @param nameList Vector  map names list of hvmaps
     */
    public  void setMapName(Vector nameList) {
	this.mapNameList = nameList;
	nMaps = mapNameList.size();
	//	saveParameters();
    }
    
    
    /**
     * Returns list of map names of hvmaps from property mapNameList
     * @return Vector  hostnames list of hvframes
     */
    public  Vector getMapName() {
	return mapNameList;
    }
    
    /**
     * Returns list of file names of hvmaps from property mapFileList
     * @return Vector  file names list of hvmaps
     */
    public  Vector getFileName() {
	return mapFileList;
    }

    /**
     * Returns list of sizes of hvmaps from property mapSizeList
     * @return Vector  size list of hvmaps
     */
    public  Vector getMapSize() {
	return mapSizeList;
    }

    /**
     * Adds map name to the mapname list property mapNameList and
     * increments number of map propertie nMaps by 1.
     * @param name String map name to add
     */ 	
     public void addMapName(String name) {
	mapNameList.add((String)name);
	nMaps++;
    }

    /**
     * Adds map file name to the property mapFileList.
     * @param name String file name to add
     */ 	
     public void addFileName(String name) {
	mapFileList.add((String)name);
    }

    /**
     * Adds map size string to the property mapSizeList.
     * @param name String size string to add in form:"X:Y:dirX:dirY"
     */ 	
     public void addMapSize(String name) {
	mapSizeList.add((String)name);
    }

    /**
     * Inserts the specified map name as a component in mapNameList
     * at the specified index.
     * @param ind int index where to insert the new component.
     * @param name String the map name to insert.
     */
     public void insertMapName(int ind, String name) {
	mapNameList.add(ind, (String)name);
	nMaps++;
    }
	
    /**
     * Tests if the specified map name is a component in mapNameList.
     * @param name String map name of tested
     * @return true if and only if the specified object is the same 
     *         as a component in mapNameList, false otherwise.
     */
     public boolean contains(String name) {
	return mapNameList.contains((String)name);
    }


    /**
     * Tests if the specified name is a component in mapNameList.
     * @param name String map name of tested
     * @return true if and only if the specified object is match region 
     *         of a component in mapNameList, false otherwise.
     */
     public boolean regionMatches(String name) {

	for (Enumeration e = mapNameList.elements() ; e.hasMoreElements() ;) {
	    String hs =(String)e.nextElement() ;
	    if(hs.regionMatches(0,name,0,name.length())) return true;
	}
	return false;
     }


    /**
     * Overrides method Vector.elements()
     * @return 	Enumeration list of vector components
     */
    public Enumeration elements() {
	return mapNameList.elements();
    }

    /**
     * Removes the first occurrence of the specified element in 
     * mapNameList and mapFileList and mapSizeList. If the mapNameList does not contain the element, 
     * it is unchanged.
     * @param name String map name to remove.
     */
    public  void removeMapName(String name) {
	int indx;
	if(!mapNameList.isEmpty()) {
	    indx = mapNameList.indexOf((String)name);
	    if(indx>-1) {
		mapNameList.remove(indx); 
		mapFileList.remove(indx); 
		mapSizeList.remove(indx); 

	    nMaps--;	    

	    }
	}
    }



   /**
     * Removes the element at the specified position in mapNameList.
     * Decrement number of map name in propery nMaps by 1
     * @param ind int the index of the map name to removed.
     */
     public void removeByindex(int indx) {
	if(!mapNameList.isEmpty()) {
	    mapNameList.remove(indx);
	    nMaps--;
	    
	}
    }

    /**
     * Parses the string argument as a list of parameters
     * @param theStringList String with list of parameters
     *   delimited by blank character
     * @return Vector with map names
     */
    protected static Vector parseList(String theStringList) {
        Vector v = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            v.addElement(name);
        }
        return v;
    }

    public static String getToken(int ind, String s) {
	String name = null;
	int cnt = 0;
	StringTokenizer tokenizer = new StringTokenizer(s, ":");
	if(ind <= tokenizer.countTokens()) {
	    while (tokenizer.hasMoreTokens()) {
		name = tokenizer.nextToken();
		cnt++;
		if(ind == cnt) break;	   
	    }
	} 
	return name;
    }



    /**
     * main method used only for testing 
     */
    public static void main(String arg[]) {
	
	HVMapList hvList = new HVMapList();

	for(int i=0;i<hvList.getMapNum();i++) {
	    System.out.println("name: " +(String)hvList.getMapName().get(i));
	    System.out.println("file: "+(String)hvList.getFileName().get(i));
	    System.out.println("size: " + (String)hvList.getMapSize().get(i));

	}
	
	hvList.saveParameters();

	System.out.println("nhost:"+hvList.getMapNum());
	System.out.println("name:"+hvList.getMapName());

	
	for(int i=0;i<hvList.getMapNum();i++) {
	    System.out.println("name: " +(String)hvList.getMapName().get(i));
	    System.out.println("file: "+(String)hvList.getFileName().get(i));
	    System.out.println("size: " + (String)hvList.getMapSize().get(i));

	}
    }
}

