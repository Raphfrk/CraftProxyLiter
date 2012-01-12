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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MyPropertiesFile {

	static final String slash = System.getProperty("file.separator");

	static HashMap<String,HashMap<String,String>> propertiesDatabase = new HashMap<String,HashMap<String,String>>();

	static Object syncObject = new Object();

	HashMap<String,String> databaseTable;

	String tableName = null;

	String prefix = "";

	MyPropertiesFile(String filename) {

		tableName = new String(filename);

	}

	MyPropertiesFile(String prefix, String filename) {

		this.prefix = new String(prefix);

		tableName = new String(filename);

	}
	
	boolean containsKey(String name) {
		return databaseTable.containsKey(name);
	}

	void load() {

		String[] lines = MiscUtils.fileToString(prefix + tableName);

		addTable();
		
		if( lines == null ) {
			return;
		}

		for( String line : lines ) {
						
			if( line.indexOf('#') == -1 ) {

				String[] split = line.split("=",2);

				if( split.length == 2 ) {
					
					addRecord(split[0],split[1]);

				}
			}

		}

	}

	void save() {

		ArrayList<String> strings = new ArrayList<String>();

		Iterator<String> itr = databaseTable.keySet().iterator();
		while( itr.hasNext() ) {
			String current = itr.next();
			strings.add(current + "=" + databaseTable.get(current));
		}

		MiscUtils.stringToFile(strings, prefix + tableName);

	}

	void unLoadTable() {

		synchronized(syncObject) {
			if(!propertiesDatabase.containsKey(tableName)) {
				propertiesDatabase.remove(tableName);
			}
		}

	}

	void addTable() {

		synchronized(syncObject) {
			if(!propertiesDatabase.containsKey(tableName)) {
				propertiesDatabase.put(tableName, new HashMap<String,String>());
			}
			databaseTable = propertiesDatabase.get(tableName);
		}

	}

	void addRecord(String name, String value) {
		synchronized(syncObject) {
			databaseTable.put(name, value);		
		}
	}
	
	void removeRecord(String name) {
		synchronized(syncObject) {
			databaseTable.remove(name);		
		}
	}
	
	int getInt( String name ) {
		synchronized(syncObject) {
			return Integer.parseInt(databaseTable.get(name));	
		}
	}

	long getLong( String name ) {
		synchronized(syncObject) {
			return Long.parseLong(databaseTable.get(name));		
		}
	}

	Boolean getBoolean( String name ) {
		synchronized(syncObject) {
			return Boolean.parseBoolean(databaseTable.get(name));	
		}
	}

	Double getDouble( String name ) {
		synchronized(syncObject) {
			return Double.parseDouble(databaseTable.get(name));	
		}
	}

	String getString( String name ) {
		synchronized(syncObject) {
			return new String(databaseTable.get(name));		
		}
	}


	int getInt( String name , int defaultValue ) {
		synchronized(syncObject) {
			if( !databaseTable.containsKey(name)) {
				addRecord(name,Integer.toString(defaultValue));
			}
			return Integer.parseInt(databaseTable.get(name));	
		}
	}

	long getLong( String name , long defaultValue ) {
		synchronized(syncObject) {
			if( !databaseTable.containsKey(name)) {
				addRecord(name,Long.toString(defaultValue));
			}
			return Long.parseLong(databaseTable.get(name));		
		}
	}

	Boolean getBoolean( String name , boolean defaultValue ) {
		synchronized(syncObject) {
			if( !databaseTable.containsKey(name)) {
				addRecord(name,Boolean.toString(defaultValue));
			}
			return Boolean.parseBoolean(databaseTable.get(name));	
		}
	}

	Double getDouble( String name , double defaultValue ) {
		synchronized(syncObject) {
			if( !databaseTable.containsKey(name)) {
				addRecord(name,Double.toString(defaultValue));
			}
			return Double.parseDouble(databaseTable.get(name));	
		}
	}

	String getString( String name , String defaultValue ) {
		synchronized(syncObject) {
			if( !databaseTable.containsKey(name)) {
				addRecord(name,new String(defaultValue));
			}
			return new String(databaseTable.get(name));		
		}
	}

	void setInt( String name, int value) {
		addRecord( name, Integer.toString(value));
	}

	void setLong( String name, long value) {
		addRecord( name, Long.toString(value));
	}
	
	void setDouble( String name, double value) {
		addRecord( name, Double.toString(value));
	}
	
	void setBoolean( String name, boolean value) {
		addRecord( name, Boolean.toString(value));
	}
	
	void setString( String name, String value) {
		addRecord( name, new String(value));
	}
}
