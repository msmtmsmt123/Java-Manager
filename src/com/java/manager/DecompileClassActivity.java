package com.java.manager;
import android.os.*;
import android.widget.*;
import android.app.*;
import android.view.View.*;
import android.view.*;
import android.content.*;
import com.ipaulpro.afilechooser.*;
import android.net.*;
import com.ipaulpro.afilechooser.utils.*;
import net.daum.adam.publisher.*;
import net.daum.adam.publisher.AdView.*;
import net.daum.adam.publisher.impl.*;

public class DecompileClassActivity extends Activity implements OnClickListener
{
	private static final int REQUEST_FOR_CLASS_FILE = 0;
	private static final int DIALOG_ID_PROGRESS = 0;

	Button btnChooseClassFile, btnDecompile;
	TextView txtClassPath;

	String classPath = "";

	Handler h;

	DecompileThread decompileThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decompileclass);

		initAdam();
		
		setTitle("Decompile Java");

		h = new Handler();

		txtClassPath = (TextView) findViewById(R.id.txtClassPath);

		btnChooseClassFile = (Button) findViewById(R.id.btnChooseClassFile);
		btnDecompile = (Button) findViewById(R.id.btnDecompile);

		btnChooseClassFile.setOnClickListener(this);
		btnDecompile.setOnClickListener(this);
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
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if(decompileThread != null) decompileThread.interrupted();
	}

	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnChooseClassFile :
				Intent jintent = new Intent(this, FileChooserActivity.class);

				startActivityForResult(jintent, REQUEST_FOR_CLASS_FILE);

				break;
			case R.id.btnDecompile :
				decompileThread = new DecompileThread(classPath);

				decompileThread.start();

				break;
		}
	}

	public Dialog onCreateDialog(int id, Bundle args) {
		switch(id) {
			case DIALOG_ID_PROGRESS:
				ProgressDialog progressDialog = new ProgressDialog(this);

				progressDialog.setTitle("Please Wait...");
				progressDialog.setMessage("Deompile...");
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
			case REQUEST_FOR_CLASS_FILE :

				if(path != null && path.endsWith(".class")) {
					classPath = path;

					txtClassPath.setText(classPath);
				} else {
					reportError(new Exception("File Type " + path.substring(path.lastIndexOf("."), path.length()) + " is Not Match .class"));
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void reportError(Exception pe) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("Error");

			builder.setMessage(pe.getMessage());

			builder.setPositiveButton("OK", null);

			builder.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public class DecompileThread extends Thread {
		private String filePath;

		public DecompileThread(String filePath) {
			this.filePath = filePath;
		}

		public void run() {
			try {
				h.post(new Runnable() {
						public void run() {
							showDialog(DIALOG_ID_PROGRESS);
						}
					});

				Exception decompileError = ManageJava.decompileClass(filePath);

				if(decompileError != null) {
					throw decompileError;
				}

				h.post(new Runnable() {
						public void run() {
							dismissDialog(DIALOG_ID_PROGRESS);

							AlertDialog.Builder builder = new AlertDialog.Builder(DecompileClassActivity.this);

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
