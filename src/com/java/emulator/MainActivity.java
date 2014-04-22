package com.java.emulator;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.preference.Preference.*;
import com.ipaulpro.afilechooser.*;
import com.ipaulpro.afilechooser.utils.*;
import java.io.*;

public class MainActivity extends PreferenceActivity implements OnPreferenceClickListener {
	Preference pRunJava, pRunClass, pRunJar, pCompileJava, pArchiveJar, pAbout;
	String nowKey = "";
	
	private static final int REQUEST_CHOOSER = 0;
	private static final String EXTRA_FILE_PATH = "com.java.emulator.Extra.FilePath";
	private static final String EXTRA_OPTION_COMPILE = "com.java.emulator.Extra.CompileOption";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mainactivity_preference);
		
		try {
			File f = new File(getFilesDir() + "/dalvik-cache");
			f.mkdir();
		} catch(Exception e) {
		}
		
		copyRTJarAssets();
		
	    pRunJava = (Preference) findPreference("runjava");
		pRunClass = (Preference) findPreference("runclass");
		pRunJar = (Preference) findPreference("runjar");
		
	    pCompileJava = (Preference) findPreference("compilejava");
		//pArchiveJar = (Preference) findPreference("archivejar");
		
		pAbout = (Preference) findPreference("about");
		
		
		pRunJava.setOnPreferenceClickListener(this);
		pRunClass.setOnPreferenceClickListener(this);
		pRunJar.setOnPreferenceClickListener(this);
		
		pCompileJava.setOnPreferenceClickListener(this);
		//pArchiveJar.setOnPreferenceClickListener(this);
		
		pAbout.setOnPreferenceClickListener(this);
	}
	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		nowKey = preference.getKey();
		
		if(nowKey.equals("about")) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			
			return false;
		} else if(nowKey.equals("compilejava")) {
			Intent intent = new Intent(this, CompileJavaActivity.class);
			startActivity(intent);

			return false;
		} else if(nowKey.equals("archivejar")) {
			Intent intent = new Intent(this, ArchiveJarActivity.class);
			startActivity(intent);

			return false;
		}
		
		Intent intent = new Intent(this, FileChooserActivity.class);
		
		startActivityForResult(intent, REQUEST_CHOOSER);
		
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	    case REQUEST_CHOOSER:   
			if (resultCode == RESULT_OK) {
	            try {
		            Uri uri = data.getData();
	
		            String path = FileUtils.getPath(this, uri);
	
		 
		            if (path != null) {
			            Intent i;
			                
			       		if(nowKey.equals("runjava")) {	
							i = new Intent(this, RunActivity.class);
							
							i.putExtra(EXTRA_FILE_PATH, path);
							i.putExtra(EXTRA_OPTION_COMPILE, 0);
						} else if(nowKey.equals("runclass")) {
							i = new Intent(this, RunActivity.class);
							
							i.putExtra(EXTRA_FILE_PATH, path);
							i.putExtra(EXTRA_OPTION_COMPILE, 1);
						} else if(nowKey.equals("runjar")) {
							i = new Intent(this, RunActivity.class);
							
							i.putExtra(EXTRA_FILE_PATH, path);
							i.putExtra(EXTRA_OPTION_COMPILE, 2);
			        	} else if(nowKey.equals("compilejava")) {
			        		i = new Intent(this, CompileJavaActivity.class);
							
							i.putExtra(EXTRA_FILE_PATH, path);
			        	} else if(nowKey.equals("archivejar")) {
			        		i = new Intent(this, ArchiveJarActivity.class);
			        	} else {
			        		break;
			        	}
		        		
						startActivity(i);
		            }		        		
		        		
	            } catch(Exception e) {
	            	e.printStackTrace();
					reportError(e);
	            }
	        }
	        break;
	    }
	}
	
	public void reportError(Exception pe) {
		try {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle("Error!");
			
			builder.setMessage(pe.getMessage());
			
			builder.setPositiveButton("OK", null);
			
			builder.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void copyRTJarAssets() {
		try {
			InputStream rtstream = getAssets().open("classpath/rt.jar");

			File copyed = new File(getFilesDir() + "/rt.jar");
			if(copyed.exists()) return;

		    OutputStream copyedstream = new FileOutputStream(copyed);

			byte[] b = new byte[1024];
			int len = 0;

			while((len = rtstream.read(b)) > 0) {
				copyedstream.write(b);
			}

			rtstream.close();
			copyedstream.close();
		} catch(Exception e) {
			e.printStackTrace();
			
			reportError(e);
		}
	}
	
}
