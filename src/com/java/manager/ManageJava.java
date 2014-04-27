package com.java.manager;

import android.util.*;
import dalvik.system.*;
import java.io.*;
import java.util.*;
import org.benf.cfr.reader.state.*;
import org.benf.cfr.reader.util.getopt.*;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider.*;

public class ManageJava
{
	public ManageJava() {
	
	}
	
	public static Exception compileJava(String javaPath, String includeClassPath) {
		PrintStream stream;
		try {
			File classed = new File(javaPath.substring(0, javaPath.lastIndexOf(".")) + ".class");		
			if(classed.exists()) classed.delete();
			
			PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream pin = new PipedInputStream(pout);
			
			stream = new PrintStream(pout);

			sun.tools.javac.Main compiler = new sun.tools.javac.Main(pout, "javac");
			compiler.compile(("-nowarn -classpath " + includeClassPath + " -g "+ javaPath).split(" "));
		
			stream.close();
		
			classed = new File(javaPath.substring(0, javaPath.lastIndexOf(".")) + ".class");
			if(classed.exists()) return null;
		
			Scanner scanner = new Scanner(pin);
	    	String output = "";
	    	while(scanner.hasNextLine()) {
		    	output += scanner.nextLine();
		    	if(scanner.hasNextLine()) output += "\n";
		    }
			
			pout.close();
			pin.close();
			stream.close();
			scanner.close();
		
		    return new Exception("Failed to Compile Java : "  + output);
		}
		catch (Exception e) {
			return new Exception("Failed to create Stream");
		}
	}
	
	public static Exception decompileClass(final String classPath) {
		try {
			org.benf.cfr.reader.Main decompiler = new org.benf.cfr.reader.Main();
			
			String[] args = new String[]{classPath, "--outputdir", classPath.substring(0, classPath.lastIndexOf("/"))};
			
			GetOptParser parser = new GetOptParser();
			
			Options option = (Options) parser.parse(args, OptionsImpl.getFactory());
			
			DCCommonState state = new DCCommonState(option);
			
			PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream pin = new PipedInputStream(pout);
			
			PrintStream stream = new PrintStream(pout);

			System.setOut(stream);
			
			decompiler.doClass(state, classPath);
			
			File decompiled = new File(classPath.substring(0, classPath.lastIndexOf(".")) + ".java");
			if(decompiled.exists()) return null;
			
			Scanner scanner = new Scanner(pin);
			String output = "";
			while(scanner.hasNextLine()) {
				output += scanner.nextLine() + "\n";
			}
			
			pout.close();
			pin.close();
			stream.close();
			scanner.close();
			
			return new Exception(output);
		} catch(Exception e) {
			return new Exception("Failed to create Stream");
		}
	}
	
	public static void archiveJar() {
		
	}
	
	public static Exception dexClass(String classPath, String dexPath) {
		File dexed = new File(dexPath);
		if(dexed.exists()) dexed.delete();
		
		com.java.manager.dx.command.dx.main(("--dex --output=" + dexPath + " " + classPath).split(" "));
		
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
