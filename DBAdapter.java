import java.sql.*;
import java.lang.*;
import java.util.*;
import java.lang.reflect.Array;
import hvframe.*;

public class DBAdapter {
    Connection          connection;
    Statement           statement;
    ResultSet           resultSet;
    String              dbtable = null; // name of database table
    String[]            columnNames = {};
    ResultSetMetaData   metaData;


    public DBAdapter(String url, String driverName,
		     String user, String passwd, String table) {
	dbtable = table;
        try {
            Class.forName(driverName);
            System.out.println("Opening db connection");

            connection = DriverManager.getConnection(url, user, passwd);
            statement = connection.createStatement();
        }
        catch (ClassNotFoundException ex) {
            System.err.println("Cannot find the database driver classes.");
            System.err.println(ex);
        }
        catch (SQLException ex) {
            System.err.println("Cannot connect to this database.");
            System.err.println(ex);
        }
    }
    
    
    public String prepareQuery(String id) {
	String editedParameters = "ncrates,nchan,namecr,namech,dvch,cech,rupch,rdnch,tcch,mvdzch,mcdzch";
	String	queryString = "SELECT " + editedParameters + " FROM " + dbtable +
	    " WHERE ID="+id +";" ;
	return queryString; 
    }


    public Vector executeQuery(String query, int id) {
	System.out.println(query);
        Vector cmd = new Vector();
	if (connection == null || statement == null) {
            System.err.println("There is no database to execute the query.");
            return null;
        }
	try {
            resultSet = statement.executeQuery(query);
            metaData = resultSet.getMetaData();

            int numberOfColumns =  metaData.getColumnCount();
            columnNames = new String[numberOfColumns];
            // Get the column names and cache them.
            // Then we can close the connection.
            for(int column = 0; column < numberOfColumns; column++) {
                columnNames[column] = metaData.getColumnLabel(column+1);
            }

	    //	    System.out.println("class="+getColumnClass(3));
	    
	    if(id==0) {
		while (resultSet.next()) {
		    String s = new String(" ");
		    for(int i=0;i<numberOfColumns;i++) {
			s = s + resultSet.getString(i+1) + " ";
		    } 
		    System.out.println(s);
		    cmd.addElement(s.trim());
		}
	    } else {
		cmd = parseStringFromDB();
	    }
	    
	    //	    statement.close();
	    // connection.close();
	    
	} catch (SQLException ex) {
	    System.err.println(ex);
        }	
	return cmd;
    }
    
    public Vector parseStringFromDB() {
	Vector v = new Vector();
	try {
	    int ncr = 0; // crates number
	    int nch = 0; // channels number
	    Object  crname = null;
	    Object  chname = null;
	    Object  chdv = null;
	    Object  chce = null;
	    Object  chrup = null;
	    Object  chrdn = null;
	    Object  chtc = null;
	    Object  chmvdz = null; 
	    Object  chmcdz = null; 
	    while (resultSet.next()) {
		ncr = resultSet.getInt("ncrates");
		nch = resultSet.getInt("nchan");
		
		//System.out.println(ncr+" "+ nch);
		
	        crname =(resultSet.getArray("namecr")).getArray();
		chname = (resultSet.getArray("namech")).getArray();
		
		//System.out.println(chname);
		
		chdv =   (resultSet.getArray("dvch")).getArray(); //get array of values for DV channel property
		chce =   (resultSet.getArray("cech")).getArray(); //get array of values for CE channel property
		chrup =  (resultSet.getArray("rupch")).getArray();//get array of values for RUP channel property	    
		chrdn =  (resultSet.getArray("rdnch")).getArray(); //get array of values for RDN channel property
		chtc =   (resultSet.getArray("tcch")).getArray();  //get array of values for TC channel property
		chmvdz = (resultSet.getArray("mvdzch")).getArray();//get array of values for MVDZ channel property	    
		chmcdz = (resultSet.getArray("mcdzch")).getArray();//get array of values for MCDZ channel property
		//		System.out.println(chdv);
		
		
		String bl = " ";
		
		    //for(int ic = 0; ic<ncr; ic++) {
		
		//  int cr =  Array.getInt(crname,ic);
		    //		    System.out.println(cr);
		//}
		
		int ncm = 12; // number channels per module
		
		String[] crnumbers = new String[ncm];
		String[] modnumbers = new String[ncm];
		String[] chnumbers = new String[ncm];
		int[] ichnames = new int[ncm];
		
		int ccnt = 0;
		int scnt = 0;
		
		for(int ich = 0; ich < nch ; ich++) {
		    // loop over all hv channels (ich)
		    int ichname = Array.getInt(chname,ich);
		    //if(ichname == 70010) ichname=ichname+2;
		    //if(ichname == 70011) ichname=ichname+3;
		    String ch = String.valueOf(ichname);
		    int ls = ch.length();
		    String crnumber = ch.substring(0,ls-4);		    
		    String modnumber = ch.substring(ls-4,ls-2);		    
		    String chnumber = ch.substring(ls-2,ls);		    

		    System.out.println("chname="+ichname+"  ich="+ich+"   ccnt="+ccnt);
	
			crnumbers[ccnt] = crnumber;
			modnumbers[ccnt] = modnumber;
			chnumbers[ccnt] = chnumber;
			ichnames[ccnt] = ichname;
			if(ccnt==0) ccnt++;
		 
			scnt++;		    
	
		    //check if channel numbers are different by 1
		    if((ccnt>0)&&(Math.abs((ichname - ichnames[ccnt-1]))==1)) {
			ccnt++;
			if(ccnt == ncm) {
			    // create command for module not for every channel
			    String cmd = new String();
			    String address = "L"+ Integer.parseInt(modnumber); //remove 0 from modulenumbers 01,02 ...  
			    String  cdv = new String(" DV ");
			    String  cce = new String(" CE ");
			    String  crup =  new String(" RUP ");
			    String  crdn =  new String(" RDN ");
			    String  ctc =  new String(" TC ");
			    String  cmvdz =  new String(" MVDZ ");
			    String  cmcdz =  new String(" MCDZ ");
			    int ic =0;
			    
			    // create string with parameters for 12 channels and for setteble properties
			    for (int i=0; i<ncm; i++) {
				
				ic = ich + Integer.parseInt(chnumbers[i]) - (ncm-1);
				cdv = cdv + String.valueOf(Array.getFloat(chdv,ic)) +bl; 
				cce = cce + String.valueOf(Array.getInt(chce,ic)) +bl;
				crup = crup + String.valueOf(Array.getFloat(chrup,ic)) +bl;
				crdn = crdn + String.valueOf(Array.getFloat(chrdn,ic)) +bl; 
				ctc = ctc + String.valueOf(Array.getFloat(chtc,ic))+ bl;
				cmvdz = cmvdz + String.valueOf(Array.getFloat(chmvdz,ic)) +bl;
				cmcdz = cmcdz + String.valueOf(Array.getFloat(chmcdz,ic)) +bl;
			    }
			    
			    String host = "hv0"+crnumber;
			    cmd = host+bl+address+cdv;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+address+cce;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+address+crup;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+address+crdn;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+address+ctc;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+address+cmvdz;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+address+cmcdz;
			    System.out.println(cmd);
			    v.add(cmd);			    
			    
			    ccnt = 0;
			    scnt = 0;
			}
		    } //and loop over 12 channels per module 
		    

		    if (scnt == ncm) 
			scnt = 0;
	       
		    if(scnt != ccnt) {
			//create command for every channel in current module
			int ic =0;

			// create string with parameters for every channel and for setteble properties
			for (int i=0; i<ccnt; i++) {

			String cmd = new String();
			String  cdv = new String(" DV ");
			String  cce = new String(" CE ");
			String  crup =  new String(" RUP ");
			String  crdn =  new String(" RDN ");
			String  ctc =  new String(" TC ");
			String  cmvdz =  new String(" MVDZ ");
			String  cmcdz =  new String(" MCDZ ");
			    String address = "L"+ Integer.parseInt(modnumbers[i])+"."+Integer.parseInt(chnumbers[i]); //remove 0 from module and channel numbers			
			    ic = ich - (ccnt) + i;
			    
			    System.out.println("ic="+ic+" ccnt="+ccnt+"  scnt="+scnt);

			    cdv = address +cdv + String.valueOf(Array.getFloat(chdv,ic)); 
			    cce = address +cce + String.valueOf(Array.getInt(chce,ic));
			    crup = address +crup + String.valueOf(Array.getFloat(chrup,ic));
			    crdn = address +crdn + String.valueOf(Array.getFloat(chrdn,ic)); 
			    ctc = address +ctc + String.valueOf(Array.getFloat(chtc,ic));
			    cmvdz =  address +cmvdz+ String.valueOf(Array.getFloat(chmvdz,ic));
			    cmcdz = address +cmcdz + String.valueOf(Array.getFloat(chmcdz,ic));
			    
			    String host = "hv0"+crnumbers[i];
			    cmd = host+bl+cdv;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+cce;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+crup;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+crdn;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+ctc;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+cmvdz;
			    System.out.println(cmd);
			    v.add(cmd);
			    cmd = host+bl+cmcdz;
			    System.out.println(cmd);
			    v.add(cmd);			    
			}
			

			ccnt = scnt = 0;
			
			crnumbers[ccnt] = crnumber;
			modnumbers[ccnt] = modnumber;
			chnumbers[ccnt] = chnumber;
			ichnames[ccnt] = ichname;
			
			ccnt++;
			scnt++;		    

		    }
		    
		    //System.out.println(crnumber+"."+modnumber+"."+chnumber);


		} // end loop over all hv channels

	    }

	} catch (SQLException ex) {
	    System.err.println(ex);
	    return null;
	}
	
	return v;
    }
    
    
    public boolean executeUpdate(String update) {
	try {
	    statement.executeUpdate(update);
	    statement.close();
	    connection.close();
	    
	    
	} catch (SQLException ex) {
	    System.err.println(ex);
	    return false;
	}
	return true;
    }
    
    public String prepareInsertString(Hashtable hvft, String com) {
	String insertString = new String();
	//	String table = "rcs_hv_status";
	String tablecolumns = "(ncrates,nchan,namecr,statcr,namech,dvch,cech,rupch,rdnch,tcch,mvdzch,mcdzch,hvlch,stch,mvch,mcch,comments)";

	int ncrates = 0;
	int nchan = 0;
	String opa = "'{";
	String cla = "}'";
	String dl =new String();
	String namecr = new String(opa);
	String statcr = new String(opa);
	String namech = new String(opa);
	String dvch = opa;
	String cech = opa;
	String rupch = opa;
	String rdnch = opa;
	String tcch = opa;
	String mvdzch = opa;
	String mcdzch = opa;
	String hvlch = opa;
	String svlch = opa;
	String stch = opa;
	String mvch = opa;
	String mcch = opa;
	
	String crname = new String();

	for (Enumeration e = hvft.elements() ; e.hasMoreElements() ;) {
	    HVframe f =(HVframe)e.nextElement() ;
	    ncrates++;
	    String hname = f.name;
	    // take last 2 symbols from hostname of frame (its should be digits like 01,02)
	    // and first CONFIG word of frame
	    int hl = hname.length();
	    crname = hname.substring(hl-1,hl);
	    if(ncrates<hvft.size()) {
		namecr = namecr + crname + ",";
		statcr = statcr + f.getConfigWord() + ",";
	    } else  {
		namecr = namecr + crname + cla;
		statcr = statcr + f.getConfigWord() + cla;
	    }	    
	  
	    int nmodules = f.nunit;
	    // count numbers of all channels in all frames
	    String pdelimiter = new String("");
	    String zdelimiter = new String("0");
	    String firstdel = pdelimiter;
	    String seconddel = pdelimiter;

	    for(int i =0 ; i<nmodules;i++) {
		int nch = f.hvm[i].nchn;
		nchan = nchan + nch;
		for(int k=0; k<nch; k++) {
		   String dv = f.hvm[i].ch[k].getValue("DV");
		   String ce = f.hvm[i].ch[k].getValue("CE"); 
		   String rup = f.hvm[i].ch[k].getValue("RUP"); 
		   String rdn = f.hvm[i].ch[k].getValue("RDN"); 
		   String tc = f.hvm[i].ch[k].getValue("TC"); 
		   String mvdz = f.hvm[i].ch[k].getValue("MVDZ"); 
		   String mcdz = f.hvm[i].ch[k].getValue("MCDZ"); 
		   String hvl = f.hvm[i].ch[k].getValue("HVL"); 
		   String st = f.hvm[i].ch[k].getValue("ST"); 
		   String mv = f.hvm[i].ch[k].getValue("MV"); 
		   String mc = f.hvm[i].ch[k].getValue("MC"); 
		   // define name channels(address)  
		   if(i < 10) { firstdel = zdelimiter;}
		   else  { firstdel = pdelimiter;}
		   if(k < 10) { seconddel = zdelimiter;}
		   else { seconddel = pdelimiter;}
		   
		   if(ncrates == hvft.size()&& (k==(nch-1)) &&(i==(nmodules-1))) {
		       dl= cla;
		   } else {
		       dl=",";
		   }
		   
		   namech = namech + crname +firstdel +i +seconddel +k +dl;

		   dvch = dvch + dv +dl;
		   cech = cech + ce +dl;
		   rupch =rupch + rup +dl;
		   rdnch = rdnch + rdn +dl;
		   tcch = tcch + tc +dl;
		   mvdzch = mvdzch + mvdz +dl;
		   mcdzch = mcdzch + mcdz +dl;
		   hvlch = hvlch + hvl +dl;
		   stch = stch + st +dl;
		   mvch = mvch + mv +dl;
		   mcch = mcch + mc +dl;		   

		}
	    }

	}


	//	System.out.println(ncrates+" "+nchan+" "+namecr+" "+statcr);
	//System.out.println(namech);
	//System.out.println(mvch);
	dl = ",";
	String values = "( " +
	    ncrates +dl + nchan +dl + namecr +dl + statcr +dl + 
	    namech +dl + dvch +dl + cech +dl + rupch +dl + rdnch +dl + tcch +dl + 
	    mvdzch +dl + mcdzch +dl + hvlch +dl + stch +dl + mvch +dl + mcch +dl + "'"+com+"'" +
	    " )";


	insertString = "INSERT INTO " + dbtable + " " +
	    tablecolumns + 
	    " VALUES " + values +";" ;
 
	//System.out.println(insertString);
	
	return insertString;
    }


    public Class getColumnClass(int column) {
        int type;
        try {
            type = metaData.getColumnType(column+1);
        }
        catch (SQLException e) {
            return Object.class;
        }

        switch(type) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return String.class;

        case Types.BIT:
            return Boolean.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return Integer.class;

        case Types.BIGINT:
            return Long.class;

        case Types.FLOAT:
        case Types.DOUBLE:
            return Double.class;

        case Types.DATE:
            return java.sql.Date.class;
        case Types.ARRAY:
	    System.out.println("array");
	    return java.sql.Array.class;    
        default:
            return Object.class;
        }
    }

    
    public void close() throws SQLException {
        System.out.println("Closing db connection");
	try {
	    resultSet.close();
	    statement.close();
	    connection.close();
	} catch (NullPointerException ne) {}
	}
    
    protected void finalize() throws Throwable {
        close();
    }
    
}
