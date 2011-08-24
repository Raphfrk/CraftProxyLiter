package com.raphfrk.craftproxyliter;

import java.io.File;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class HeapDump extends Thread {

	public void run() {
		try {
			System.out.println("Attempting to dump memory");
			String url = "service:jmx:rmi:///jndi/rmi://localhost:9004/jmxrmi";  
			JMXServiceURL jmxURL = new JMXServiceURL(url);  
			JMXConnector connector = JMXConnectorFactory.connect(jmxURL);  
			MBeanServerConnection connection = connector.getMBeanServerConnection();  
			String hotSpotDiagName = "com.sun.management:type=HotSpotDiagnostic";  
			ObjectName name = new ObjectName(hotSpotDiagName);  
			String operationName = "dumpHeap";  
			File tempFile = new File("craftproxy.hprof");
			tempFile.delete();  
			String dumpFilename = tempFile.getAbsolutePath();  
			Object[] params = new Object[] { dumpFilename, Boolean.TRUE };  
			String[] signature = new String[] { String.class.getName(), boolean.class.getName() };  
			Object result = connection.invoke(name, operationName, params, signature);  
			System.out.println("Dumped to " + dumpFilename);  
		} catch (Exception e) {
			System.out.println("Unable to dump memory");
			e.printStackTrace();
		}
	}

}
