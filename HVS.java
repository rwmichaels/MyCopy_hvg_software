/* HVS - implements main method to start High Voltage System application
 *  @version 1.1
 *  Last update:17 May 2001 
 *  12-Jan-02:add parameters for database operation 
 */
public class HVS {

    //*** Main method initialize HVmainMenu window
   public static void main(String[] args)  {
	if (args.length != 5) {
	    System.out.println("Usage: java HVS <db_url> <db_driver_name> <username> <password> <table>");
	    System.out.println("Example: java HVS \"jdbc:postgresql://adaqlr3/rcs_lg\" \"org.postgresql.Driver\" \"user\" \"password\" \"rcs_hv_status\"");
	    return;
        }
	// String urldb ="jdbc:postgresql://adaqlr3/rcs_lg";
	// String driverdb ="org.postgresql.Driver"; 
	//String user = "postgres" ;
	//String password = "halla" ;
	//String tabledb = "rcs_hv_status" ;

        
	HVmainMenu f = new HVmainMenu("High Voltage System Control");	
	f.setDBparameters(args[0], args[1], args[2], args[3], args[4]);
    }
    
}




