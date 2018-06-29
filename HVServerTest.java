// test class HVServer

import java.net.*;
import java.io.*;
import java.util.*;
import hvserver.*;

public class HVServerTest {

   

public  static void  main(String[] args) {

	int port = 5555;
	Hashtable<String, Integer>  htable = new Hashtable<String, Integer>();
	htable.put("one", 1);
	htable.put("two", 2);
	htable.put("three", 3);	
//	Vector map = new Vector();
	Hashtable<String, Integer>   map = new Hashtable<String, Integer>();
	map.put("m_one", 1);
	map.put("m_two", 2);
	map.put("m_three", 3);	

	HVServer   hvserver = new HVServer(port, htable, map);
	hvserver.start();
	System.out.println("Start Server on Port:"+port);
    }
}

