/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
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
