package com.java.manager;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import dalvik.system.*;
import java.io.*;
import java.lang.reflect.*;
import net.daum.adam.publisher.*;
import net.daum.adam.publisher.AdView.*;
import net.daum.adam.publisher.impl.*;

public class RunActivity extends Activity implements TextWatcher {
	
	private static final String EXTRA_FILE_PATH = "com.java.emulator.Extra.FilePath";
	private static final String EXTRA_OPTION_COMPILE = "com.java.emulator.Extra.CompileOption";
	
	private static final int DIALOG_ID_PROGRESS = 0;
	
	EditText outputArea;
	
	ReadOutputThread readThread;
	RunThread runThread;
	CompileThread compileThread;
	
	BufferedWriter writer;
	ByteArrayOutputStream reader;
	
	DexClassLoader loader;
	
	String lastOutput = "";
	
	boolean isCodeEditing = false;
	
	Handler h;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);
		
		initAdam();
		
		setTitle("Run");
		
		Intent i = getIntent();

		String filePath = i.getStringExtra(EXTRA_FILE_PATH);
		int option = i.getIntExtra(EXTRA_OPTION_COMPILE, 0);

		switch(option) {
		case 0 :
			if(!filePath.endsWith(".java")) {
				reportError(new Exception("File does not ends with .java"));
				return;
			}
			break;
		case 1 :
			if(!filePath.endsWith(".class")) {
				reportError(new Exception("File does not ends with .class"));
				return;
			}
			break;
		case 2 :
			if(!filePath.endsWith(".jar")) {
				reportError(new Exception("File does not ends with .jar"));
				return;
			}
			break;
		}
		
		h = new Handler();
		
		outputArea = (EditText) findViewById(R.id.outputArea);
		outputArea.addTextChangedListener(this);
		
		compileThread = new CompileThread(filePath, option);
		compileThread.start();
	}
	
	private void initAdam() {
		final AdView adView = (AdView) findViewById(R.id.ad);

		adView.setOnAdClickedListener(new OnAdClickedListener() {
				@Override
				public void OnAdClicked() {
				}
			});

		adView.setOnAdFailedListener(new OnAdFailedListener() {
				@Override
				public void OnAdFailed(AdError error, String message) {
					adView.setVisibility(View.INVISIBLE);
				}
			});

		adView.setOnAdLoadedListener(new OnAdLoadedListener() {
				@Override
				public void OnAdLoaded() {
					adView.setVisibility(View.VISIBLE);
				}
			});

		adView.setVisibility(View.VISIBLE);
	}
	
	public void onDestroy() {
		super.onDestroy();
		
		if(readThread != null) readThread.interrupt();
		if(runThread != null) runThread.interrupt();
		if(compileThread != null) compileThread.interrupt();
	}
	
	public Dialog onCreateDialog(int id, Bundle args) {
		switch(id) {
		case DIALOG_ID_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(RunActivity.this);

			progressDialog.setTitle("Please Wait...");
			progressDialog.setMessage("Compile and Dexing Code");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);

			return progressDialog;
		}
		
		return null;
	}
	
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
	}

	public void onTextChanged(CharSequence text, int start, int before, int count)
	{
		if(isCodeEditing) return;
		if(count > 1) return;
		if(start + count != text.toString().length()) return;
		if(writer == null) return;
		
		String inputed = text.toString().substring(start, start + count);
		
		try{
			if(inputed.equals("\n")) {
				String written = text.toString().substring(text.toString().indexOf(lastOutput) + lastOutput.length(), text.toString().length());
				
				lastOutput = text.toString();
				
				reader.write(written.getBytes());
				reader.flush();
				
				writer.write(written);
				writer.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void afterTextChanged(Editable p1) {
	}
	
	public void runDex(String dexPath, String[] classesName, String className, String[] args) {
		runThread = new RunThread(dexPath, classesName, className, args);
		runThread.start();
	}
	
	public void reportError(Exception pe) {
		try {
			isCodeEditing = true;
	    	outputArea.append(pe.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public class ReadOutputThread extends Thread {
		private String output;
		
		public ReadOutputThread() {
		}
		
		public void run() {
			try {	
				while(!isInterrupted()) {
					
					Thread.sleep(300);
					
					byte[] b = reader.toByteArray();
					
					output = new String(b, "UTF-8");
					
					if(output.length() > lastOutput.length()) {
						
						h.post(new Runnable() {
							public void run() {
								isCodeEditing = true;
							
								outputArea.append(output.substring(output.indexOf(lastOutput) + lastOutput.length(), output.length()));
							
								isCodeEditing = false;
							
								lastOutput = outputArea.getText().toString();
							}	
						});
					}
				}
			} catch(final Exception e) {
				e.printStackTrace();
				h.post(new Runnable() {
					public void run() {
						reportError(e);
					}
				});
			}
		}
	}
	
	public class RunThread extends Thread {
		String[] args, classesName;
		String dexPath, className;
		
		public RunThread(String dexPath, String[] classesName, String className, String[] args) {
			this.dexPath = dexPath;
			this.classesName = classesName;
			this.className = className;
			this.args = args;
		}
		
		public void run() {
			try {
				PipedInputStream oIn = new PipedInputStream();
				PipedOutputStream oOut = new PipedOutputStream(oIn);

				writer = new BufferedWriter(new OutputStreamWriter(oOut));


				reader = new ByteArrayOutputStream();

				PrintStream stream = new PrintStream(reader);


				System.setOut(stream);
				System.setIn(oIn);


				readThread = new ReadOutputThread();
				readThread.start();


				loader = new DexClassLoader(dexPath, getFilesDir() + "/dalvik-cache", null, getClassLoader());

				Class clazz = null;
				
				for(String subClassName : classesName) {
					if(subClassName.equals(className)) clazz = loader.loadClass(className);
					else loader.loadClass(subClassName);
				}
				
				Method method = clazz.getDeclaredMethod("main", new Class[]{args.getClass()});
				
				method.invoke(null, new Object[]{args});
			} catch(final Exception e) {
				e.printStackTrace();
				h.post(new Runnable() {
					public void run() {
						reportError(e);
					}
				});
			}
		}
	}
	
	public class CompileThread extends Thread {
		private String filePath;
		private int option;
		
		public CompileThread(String filePath, int option) {
			this.filePath = filePath;
			this.option = option;
		}
		
		public void run() {
			try {
				h.post(new Runnable() {
					public void run() {
						showDialog(DIALOG_ID_PROGRESS);
					}
				});
				
				switch(option) {
					case 0 :
						Exception compileError = ManageJava.compileJava(filePath, getFilesDir() + "/rt.jar");

						if(compileError != null) {
							throw compileError;
						}
						
					case 1 :
						Exception dexError = ManageJava.dexClass(filePath.substring(0, filePath.lastIndexOf(".")) + ".class", getFilesDir() + "/classes.dex");

						if(dexError != null) {
							throw dexError;
						}

						if(option == 0) new File(filePath.substring(0, filePath.lastIndexOf(".")) + ".class").delete();

						break;
					case 2 :
						
						reportError(new Exception("JarNotSuportedException (#0) : Not already Jar Not Supported\nNot Accept For Jar In cerr #0"));
						
						return;
				}

				final String[] classesName = ManageJava.getAllClassName(getFilesDir() + "/classes.dex", getFilesDir() + "/tmp.odex");

				h.post(new Runnable() {
					public void run() {
						dismissDialog(DIALOG_ID_PROGRESS);
						removeDialog(DIALOG_ID_PROGRESS);
						
						AlertDialog.Builder classBuilder = new AlertDialog.Builder(RunActivity.this);
						
						classBuilder.setTitle("Choose Class to Run");
						classBuilder.setItems(classesName, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface p1, final int index)
							{
								final EditText argArea = new EditText(RunActivity.this);
								
								AlertDialog.Builder paramBuilder = new AlertDialog.Builder(RunActivity.this);
								
								paramBuilder.setView(argArea);
								paramBuilder.setTitle("Please Write Parameters");
								paramBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										try
										{
											String[] args = argArea.getText().toString().split(" ");
											runDex(getFilesDir() + "/classes.dex", classesName, classesName[index], args);
										}
										catch (Exception e) {
											e.printStackTrace();
											reportError(e);
										}
									}		
								});
								
								paramBuilder.show();
							}		
						});
						classBuilder.show();
					}
				});
			} catch(final Exception e) {
				e.printStackTrace();
				h.post(new Runnable() {
					public void run() {
						dismissDialog(DIALOG_ID_PROGRESS);
						reportError(e);
					}
				});
			}
		}
	}
}
