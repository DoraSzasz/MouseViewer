/*
 * ======================================================================== VES
 * --- VTK OpenGL ES Rendering Toolkit http://www.kitware.com/ves Copyright 2011
 * Kitware, Inc. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ========================================================================
 */

package com.ovidora.MouseViewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MouseViewerActivity extends Activity {

	public static final String TAG = "MouseViewerActivity";

	protected MouseGLSurfaceView mView;

	protected ImageButton mLoadButton;
	protected ImageButton mInfoButton;
	protected ImageButton mResetViewButton;

	public ArrayList<String> mBuiltinDatasetNames;

	protected String fileToOpen;
	protected int datasetToOpen = -1;

	protected ProgressDialog mProgressDialog = null;

	public static final int DATASETTABLE_REQUEST_CODE = 1;

	protected void showProgressDialog() {
		showProgressDialog("Opening data...");
	}

	protected void showProgressDialog(String message) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage(message);
		mProgressDialog.show();
	}

	public void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	public void showErrorDialog(String title, String message) {

		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setIcon(R.drawable.alert_dialog_icon);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		dialog.show();
	}

	public void showWelcomeDialog() {

		String title = getString(R.string.welcome_title);
		String message = getString(R.string.welcome_message);

		final SpannableString s = new SpannableString(message);
		Linkify.addLinks(s, Linkify.WEB_URLS);

		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle(title);
		dialog.setIcon(R.drawable.mouse_small_icon);
		dialog.setMessage(s);
		dialog.setButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});

		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(final DialogInterface iface) {
				maybeLoadDefaultDataset();
			}
		});

		dialog.show();

		((TextView) dialog.findViewById(android.R.id.message))
				.setMovementMethod(LinkMovementMethod.getInstance());
	}

	public void showHeadImageDialog() {

		String title = getString(R.string.head_image_title);
		String message = getString(R.string.head_image_message);

		// final SpannableString s = new SpannableString(message);
		// Linkify.addLinks(s, Linkify.WEB_URLS);

		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setIcon(R.drawable.info_icon);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});

		dialog.show();
		// ((TextView) dialog.findViewById(android.R.id.message))
		// .setMovementMethod(LinkMovementMethod.getInstance());
	}

	public void showCannotOpenAssetDialog() {

		// String title = getString(R.string.cannot_open_asset_title);
		// String message = getString(R.string.cannot_open_asset_message);

		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setIcon(R.drawable.alert_dialog_icon);
		// dialog.setTitle(title);
		// dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Open in Browser",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// openUrlInBrowser(getString(R.string.external_data_url));
					}
				});

		dialog.show();
	}

	protected void openUrlInBrowser(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	protected void handleUriFromIntent(Uri uri) {
		if (uri != null) {
			if (uri.getScheme().equals("file")) {
				fileToOpen = uri.getPath();
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleUriFromIntent(intent.getData());
	}

	public void initBuiltinDatasetNames() {
		if (mBuiltinDatasetNames == null) {
			String assetDir = getExternalFilesDir(null).getAbsolutePath();
			String[] fileNames = new File(assetDir).list();
			Arrays.sort(fileNames);
			int numberOfDatasets = fileNames.length;
			mBuiltinDatasetNames = new ArrayList<String>(numberOfDatasets);
			for (int i = 0; i < numberOfDatasets; ++i) {
				mBuiltinDatasetNames.add(i, fileNames[i]);
			}

		}
	}

	public String loadDatasetFilename(int DatasetIndex) {
		initBuiltinDatasetNames();
		return mBuiltinDatasetNames.get(DatasetIndex);
	}

	void maybeLoadDefaultDataset() {

		if (getIntent().getData() == null) {
			try {
				copyVolumeFilesToAssets();
			}
			catch (IOException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
			String assetDir = getExternalFilesDir(null).getAbsolutePath();
			mView.postLoadDefaultDataset(this, assetDir);
		}
		else {
			KiwiNative.clearExistingDataset();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mView.stopRendering();
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		handleUriFromIntent(getIntent().getData());

		this.setContentView(R.layout.mousevieweractivity);

		mView = (MouseGLSurfaceView) this.findViewById(R.id.glSurfaceView);

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String versionStr = getString(R.string.version_string);

		if (!versionStr.equals(prefs.getString("version_string", ""))) {
			prefs.edit().putString("version_string", versionStr).commit();
			showWelcomeDialog();
		}
		else {
			maybeLoadDefaultDataset();
		}

		mLoadButton = (ImageButton) this.findViewById(R.id.loadDataButton);
		mInfoButton = (ImageButton) this.findViewById(R.id.infoButton);
		mResetViewButton = (ImageButton) this.findViewById(R.id.resetButton);

		mLoadButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent datasetTableIntent = new Intent();
				datasetTableIntent.setClass(MouseViewerActivity.this,
						DatasetListActivity.class);
				initBuiltinDatasetNames();
				datasetTableIntent.putExtra(
						"com.ovidora.MouseViewer.bundle.DatasetList",
						mBuiltinDatasetNames);
				startActivityForResult(datasetTableIntent,
						DATASETTABLE_REQUEST_CODE);
			}
		});

		mInfoButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent infoIntent = new Intent();
				infoIntent.setClass(MouseViewerActivity.this,
						InfoActivity.class);
				startActivity(infoIntent);
			}
		});

		mResetViewButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mView.resetCamera();
			}
		});

	}

	public void copyVolumeFilesToAssets() throws IOException {

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		}

		File assetDir = new File(getExternalFilesDir(null).getAbsolutePath());
		File storageDir = new File(
				String.format(getString(R.string.volumes_directory)));

		if (storageDir.isDirectory()) {
			if (!assetDir.exists()) {
				assetDir.mkdir();
			}
			else {
				if (assetDir != null) {
				for (File child: assetDir.listFiles()) {
				child.delete();
				}
				}

				}
			File[] files = storageDir.listFiles();
			Arrays.sort(files);
			for (File file: files) {
				InputStream in = new FileInputStream(file);
				OutputStream out = new FileOutputStream(assetDir + "/"
						+ file.getName());

				// Copy the bits from input stream to output stream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
		}
	}

	private class BuiltinDataLoader extends AsyncTask<String, Integer, String> {

		public int mBuiltinDatasetIndex;

		BuiltinDataLoader(int builtinDatasetIndex) {
			mBuiltinDatasetIndex = builtinDatasetIndex;
		}

		@Override
		protected String doInBackground(String... filename) {

			String assetDir = getExternalFilesDir(null).getAbsolutePath();
			String destFilename = assetDir + "/" + filename[0];
			return destFilename;

		}

		@Override
		protected void onPreExecute() {
			showProgressDialog();
		}

		@Override
		protected void onPostExecute(String filename) {
			mView.loadDataset(filename, mBuiltinDatasetIndex,
					MouseViewerActivity.this);
		}
	}

	public void loadDataset(int builtinDatasetIndex) {

		String filename = loadDatasetFilename(builtinDatasetIndex);

		// don't attempt to open large asset files on android api 8
		int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion <= 8
				&& (filename.equals("visible-woman-hand.vtp")
						|| filename.equals("AppendedKneeData.vtp")
						|| filename.equals("cturtle.vtp") || filename
							.equals("model_info.txt"))) {
			showCannotOpenAssetDialog();
			return;
		}

		new BuiltinDataLoader(builtinDatasetIndex).execute(filename);
	}

	public void loadDataset(String filename) {
		showProgressDialog();
		mView.loadDataset(filename, MouseViewerActivity.this);
	}

	public void postLoadDataset(String filename, boolean result,
			String errorTitle, String errorMessage) {
		dismissProgressDialog();
		if (!result) {
			showErrorDialog(errorTitle, errorMessage);
		}
		else {
			if (filename.endsWith("head.vti")) {
				showHeadImageDialog();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mView.onResume();

		if (fileToOpen != null) {
			loadDataset(fileToOpen);
			fileToOpen = null;
		}
		else if (datasetToOpen >= 0) {
			loadDataset(datasetToOpen);
			datasetToOpen = -1;
		}
	}

	/**
	 * Get results from the dataset dialog
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle curBundle = null;

		if (data != null) {
			curBundle = data.getExtras();
		}
		if (requestCode == DATASETTABLE_REQUEST_CODE
				&& curBundle != null
				&& curBundle
						.containsKey("com.ovidora.MouseViewer.bundle.DatasetName")) {

			int offset = curBundle
					.getInt("com.ovidora.MouseViewer.bundle.DatasetOffset");
			datasetToOpen = offset;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

}
