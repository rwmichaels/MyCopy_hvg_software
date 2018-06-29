import java.util.*;
import java.lang.*;
import java.io.*;

public class mapGenerator {
    String framelist;
    final int XMAX = 22;
    final int YMAX = 32;
    int channelspermodule;
    int modulesperframe;

    public mapGenerator() {
	framelist = new String("hv01:1090 hv02:1090 hv03:1090 hv04:1090 hv05:1090");
	modulesperframe = 16;
	channelspermodule = 12;	
    }

    public void save(String filename) {
	File file = new File(filename);
	FileWriter out = null;
	int cntx = 0;
	int cnty = 0;
	boolean loop = true;
	String pdelimiter = new String("");
	String zdelimiter = new String("0");
	String firstdel = pdelimiter;
	String seconddel = pdelimiter;

	try {
	    out = new FileWriter(file);
	    String comment = "# Map file for High Voltage System of RCS calorimeter. Generated\n";
	    out.write(comment);
	    out.write(framelist+"\n")
	      ;
	    for (int i=1; i<6; i++) {
		for(int m=0;m<modulesperframe;m++) {
		    for(int k=0;k<channelspermodule;k++) {
			if(loop) {
			    if(m < 10) { firstdel = zdelimiter;}
			    else  { firstdel = pdelimiter;}
			    if(k < 10) { seconddel = zdelimiter;}
			    else { seconddel = pdelimiter;}
			    out.write(i +firstdel +m +seconddel +k +" ");
			    cntx++;
			}
			if(cntx == XMAX) {
			    cntx = 0;
			    cnty++;
			    out.write("\n");
			}
			if(cnty == YMAX) {
			    cnty = 0;
			    loop = false;   // not so good ?!
			}
		    }
		}
	    }
	    out.close();
	} catch (FileNotFoundException ef) {
	    System.err.println("Save: " + ef);		    
	} catch (IOException ef) {
	    System.err.println("Save: " + ef);
	}	
    }

    public static void main(String[] args) {

	mapGenerator genmap = new mapGenerator();
	if(args.length != 1) throw new 
	    IllegalArgumentException("\nUsage: mapGenerator outputmapfilename\n");
	genmap.save(args[0]);

    }
}








