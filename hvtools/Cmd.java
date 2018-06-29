package hvtools;

/**
 * Interface to store HVframe command set
 * @version 1.1
 * Last update: 17-May-010
 */
public interface Cmd {
    // commands string for HV mainframe
    public static final String LL = "LL"; 
    public static final String LD = "LD";
    public static final String RC = "RC";
    public static final String SRC = "SRC";
    public static final String RM = "RM";
    public static final String SRM = "SRM";
    public static final String SM = "SM";
    public static final String LM = "LM";
    public static final String LS = "LS";
    public static final String PS = "PS";
    public static final String ID = "ID";
    public static final String PROP = "PROP";
    public static final String ATTR = "ATTR";
    public static final String DMP = "DMP";
    public static final String CONFIG = "CONFIG";
    public static final String DATE = "DATE";
    public static final String GS = "GS";
    public static final String HVON = "HVON";
    public static final String HVOFF = "HVOFF";
    public static final String HVSTATUS = "HVSTATUS";
    public static final String PUPSTATUS = "PUPSTATUS";
    public static final String SYSINFO = "SYSINFO";
    public static final String SYSDEF = "SYSDEF";
    public static final String IMOFF = "IMOFF";
    public static final String ENET = "ENET";

    /**
     * delay constant(in millisecond) for monitoring thread;
     */
    public static final int MONITOR_DELAY = 3000; //in milliseconds
}
