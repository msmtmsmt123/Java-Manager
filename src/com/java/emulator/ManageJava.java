package com.java.emulator;

import android.util.*;
import dalvik.system.*;
import java.io.*;
import java.util.*;

public class ManageJava
{
	public ManageJava() {
	
	}
	
	public static Exception compileJava(String javaPath, String includeClassPath) {
		PrintStream stream;
		try {
			File classed = new File(javaPath.substring(0, javaPath.lastIndexOf(".")) + ".class");		
			if(classed.exists()) classed.delete();
			
			stream = new PrintStream("/data/data/com.java.emulator/files/tmp");

			sun.tools.javac.Main compiler = new sun.tools.javac.Main(stream, "javac");
			compiler.compile(("-nowarn -classpath " + includeClassPath + " -g "+ javaPath).split(" "));
		
			stream.close();
		
			classed = new File(javaPath.substring(0, javaPath.lastIndexOf(".")) + ".class");
			if(classed.exists()) return null;
		
			Scanner scanner = new Scanner(new File("/data/data/com.java.emulator/files/tmp"));
	    	String output = "";
	    	while(scanner.hasNextLine()) {
		    	output += scanner.nextLine();
		    	if(scanner.hasNextLine()) output += "\n";
		    }
		
		    return new Exception("Failed to Compile Java : "  + output);
		}
		catch (Exception e) {
			return new Exception("Failed to create Stream");
		}
	}
	
	public static void archiveJar() {
		
	}
	
	public static Exception dexClass(String classPath, String dexPath) {
		File dexed = new File(dexPath);
		if(dexed.exists()) dexed.delete();
		
		com.java.emulator.dx.command.dx.main(("--dex --output=" + dexPath + " " + classPath).split(" "));
		
		dexed = new File(dexPath);
		if(dexed.exists()) return null;
		return new Exception("Failed to dexing Class");
	}
	
	public static String[] getAllClassName(String dexPath, String tmpPath) {
		try {
			File tmpFile = new File(tmpPath);	
			if(tmpFile.exists()) tmpFile.delete();
			
			ArrayList<String> classesName = new ArrayList<String>();
			
			DexFile dex = DexFile.loadDex(dexPath, tmpPath, 0);
			
			Enumeration<String> classEnumeration = dex.entries();
			
			while(classEnumeration.hasMoreElements()) {
				String className = classEnumeration.nextElement();
				classesName.add(className);
			}
		    
			return classesName.toArray(new String[]{});
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	    return new String[]{};
	}
}
