package com.importing.alexanderzatsepin.importfromgallery;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_IMPORT_FILES = 101;
	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button importFromGallery = (Button) findViewById(R.id.importFromGalley);
		importFromGallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				importFiles();
			}
		});
	}

	private void importFiles() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		}
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
			startActivityForResult(intent, REQUEST_CODE_IMPORT_FILES);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQUEST_CODE_IMPORT_FILES:
					if (data != null) {
						handleImportResult(data);
					}
					break;
			}
		}
	}

	private void handleImportResult(@NonNull Intent data) {
		List<Uri> uris = new ArrayList<>();
		ClipData clipData = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			clipData = data.getClipData();
		}

		if (clipData != null) {
			for (int i = 0; i < clipData.getItemCount(); i++) {
				uris.add(clipData.getItemAt(i).getUri());
			}
		} else {
			Uri uri = data.getData();
			if (uri == null) {
				return;
			}
			uris.add(uri);
		}

		//TODO: assume that we selected just one file, because we are just testing mobileiron work with contentprovider and uri mechanism
		Uri uri = uris.get(0);
		Log.d(TAG, "uri = " + uri);
		Log.d(TAG, "Now, we will try to read the inputstream using the ContentResolver.openInputStreamMethod(uri)");

		writeContentUriToWorkingFile(uri);
	}

	private boolean writeContentUriToWorkingFile(@Nullable Uri currentUri) {
		boolean success = false;
		InputStream inputStream = null;
		try {
			if (currentUri == null) {
				throw new AssertionError("Trying to get content input stream while uri is null");
			}
			inputStream = getContentResolver().openInputStream(currentUri);
			if (inputStream == null) {
				return false;
			}
			byte[] bytes = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(bytes)) != -1) {
				Log.d(TAG, bytesRead + " bytes were read");
			}
			success = true;
		} catch (IOException e) {
			success = false;
			Log.e(TAG, "An I/O exception occurred during copying the content by uri '" + currentUri + "'", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "Cannot close content input stream during copying the content by uri '" + currentUri + "'", e);
				}
			}
		}
		return success;
	}
}
