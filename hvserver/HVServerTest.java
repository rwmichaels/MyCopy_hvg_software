// test class HVServer

import java.net.*;
import java.io.*;
import java.util.*;
import hvserver.*;

public class HVServerTest {

   

public  static void  main(String[] args) {

	int port = 5555;
	Hashtable  htable = new Hashtable();
	Hashtable map = new Hashtable();
	HVServer   hvserver = new HVServer(port, htable, map);
	hvserver.start();
	System.out.println("Start Server on Port:"+port);
    }
}

