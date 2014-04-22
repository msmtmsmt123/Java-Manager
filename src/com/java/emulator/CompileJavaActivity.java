package com.java.emulator;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.ipaulpro.afilechooser.*;
import com.ipaulpro.afilechooser.utils.*;
import com.sun.tools.internal.ws.processor.model.java.*;

public class CompileJavaActivity extends Activity implements OnClickListener {
	private static final int REQUEST_FOR_JAVA_FILE = 0;
	private static final int REQUEST_FOR_CLASSPATH_FILE = 1;
	private static final int DIALOG_ID_PROGRESS = 0;
	
	Button btnChooseJavaFile, btnChooseClassPathFile, btnResetClassPathFile, btnCompile;
	TextView txtJavaPath, txtClassPath;
	
	String classPath = "Default";
	String javaPath = "";
	
	Handler h;
	
	CompileThread compileThread;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compilejava);
		
		setTitle("Compile Java");
		
		h = new Handler();
		
		txtJavaPath = (TextView) findViewById(R.id.txtJavaPath);
		txtClassPath = (TextView) findViewById(R.id.txtClassPath);
		
		btnChooseJavaFile = (Button) findViewById(R.id.btnChooseJavaFile);
		btnChooseClassPathFile = (Button) findViewById(R.id.btnChooseClassPathFile);
		btnResetClassPathFile = (Button) findViewById(R.id.btnResetClassPathFile);
		btnCompile = (Button) findViewById(R.id.btnCompile);
		
		btnChooseJavaFile.setOnClickListener(this);
		btnChooseClassPathFile.setOnClickListener(this);
		btnResetClassPathFile.setOnClickListener(this);
		btnCompile.setOnClickListener(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		if(compileThread != null) compileThread.interrupted();
	}
	
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnChooseJavaFile :
			Intent jintent = new Intent(this, FileChooserActivity.class);
			
			startActivityForResult(jintent, REQUEST_FOR_JAVA_FILE);
			
			break;
		case R.id.btnChooseClassPathFile :
			Intent cintent = new Intent(this, FileChooserActivity.class);
			
			startActivityForResult(cintent, REQUEST_FOR_CLASSPATH_FILE);
			
			break;
		case R.id.btnResetClassPathFile :
			classPath = "";
			
		    txtClassPath.setText("Default");
			
			break;
		case R.id.btnCompile :
			compileThread = new CompileThread(javaPath, classPath.equals("Default") ? getFilesDir() + "/rt.jar" : classPath);
			
			compileThread.start();
			
			break;
		}
	}
	
	public Dialog onCreateDialog(int id, Bundle args) {
		switch(id) {
		case DIALOG_ID_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(this);
			
			progressDialog.setTitle("Please Wait...");
			progressDialog.setMessage("Compile and Dexing Code");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);

			return progressDialog;
		}

		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != RESULT_OK) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}
		Uri uri = data.getData();

		String path = FileUtils.getPath(this, uri);
		
		switch(requestCode) {
		case REQUEST_FOR_JAVA_FILE :
			
			if(path != null && path.endsWith(".java")) {
				javaPath = path;
				
				txtJavaPath.setText(javaPath);
			} else {
				reportError(new Exception("File Type " + path.substring(path.lastIndexOf("."), path.length()) + " is Not Match .java"));
			}
			break;
		case REQUEST_FOR_CLASSPATH_FILE :
			if(path != null && path.endsWith(".jar")) {
				classPath = path;
				
				txtClassPath.setText(classPath);
			} else {
				reportError(new Exception("File Type " + path.substring(path.lastIndexOf("."), path.length()) + " is Not Match .jar"));
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	
	public class CompileThread extends Thread {
		private String filePath;
		private String classPath;

		public CompileThread(String filePath, String classPath) {
			this.filePath = filePath;
			this.classPath = classPath;
		}

		public void run() {
			try {
				h.post(new Runnable() {
					public void run() {
						showDialog(DIALOG_ID_PROGRESS);
					}
				});

				Exception compileError = ManageJava.compileJava(filePath, classPath);

				if(compileError != null) {
						throw compileError;
				}
				
				h.post(new Runnable() {
					public void run() {
						dismissDialog(DIALOG_ID_PROGRESS);
						
						AlertDialog.Builder builder = new AlertDialog.Builder(CompileJavaActivity.this);
						
						builder.setTitle("Finish");
						builder.setPositiveButton("OK", null);
						
						builder.show();
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
