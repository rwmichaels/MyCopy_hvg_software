// test class HVServer

import java.net.*;
import java.io.*;
import java.util.*;

public class HVServerTest {

    static void  main(String[] args) {

	int port = 5555;
	Hashtable  htable = new Hashtable();
	Vector map = new Vector();
	HVServer hvserver = new HVServer(port, htable, map);
	hvserver.start();
	System.out.println("Start Server on Port:"+port);
    }
}

