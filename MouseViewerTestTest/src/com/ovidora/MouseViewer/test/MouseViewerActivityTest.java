/**
 * Test Class for MouseViewerActivity
 */
package com.ovidora.MouseViewer.test;

import junit.framework.Assert;
import android.app.Activity;
import android.test.AndroidTestCase;
import com.ovidora.MouseViewer.MouseViewerActivity;

/**
 * @author Ovi
 * 
 */
public class MouseViewerActivityTest extends AndroidTestCase {
	private Activity myActivity;
	public static final String TAG = "MouseViewerTestCase";
	MouseViewerActivity mMouseViewerTest;

	public MouseViewerActivityTest(String name) {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		mMouseViewerTest = new MouseViewerActivity();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Test to show that mBuiltinDatasetNames has contains the file names of the
	// volumes
	public void initBuiltinDatasetNames_Test_Volumes_Exist_Returns_Not_Null() {
		Assert.assertNull(mMouseViewerTest.mBuiltinDatasetNames);
		mMouseViewerTest.initBuiltinDatasetNames();
		Assert.assertNotNull(mMouseViewerTest.mBuiltinDatasetNames);
		Assert.assertTrue(mMouseViewerTest.mBuiltinDatasetNames.size() > 0);
	}

	// Test to show that mBuiltinDatasetNames has size 0 when there are no
	// volumes
	public void initBuiltinDatasetNames_Test_No_Volumes_Size_Returns_0() {
		Assert.assertNull(mMouseViewerTest.mBuiltinDatasetNames);
		mMouseViewerTest.initBuiltinDatasetNames();
		Assert.assertNotNull(mMouseViewerTest.mBuiltinDatasetNames);
		Assert.assertTrue(mMouseViewerTest.mBuiltinDatasetNames.size() == 0);
	}
}
