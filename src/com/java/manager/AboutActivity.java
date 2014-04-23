package com.java.manager;

import android.app.*;
import android.content.res.*;
import android.os.*;
import android.text.*;
import android.widget.*;
import java.io.*;
import android.webkit.*;

public class AboutActivity extends Activity
{
	WebView htmlViewer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		setTitle("About");
		
		htmlViewer = (WebView) findViewById(R.id.htmlViewer);
		
		htmlViewer.loadUrl("file:///android_asset/html/about.html");
	}
}
