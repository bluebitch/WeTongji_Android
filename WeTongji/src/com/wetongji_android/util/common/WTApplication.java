/**
 * 
 */
package com.wetongji_android.util.common;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.wetongji_android.util.data.DbHelper;
import java.io.File;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * @author zihe
 * 
 */
public class WTApplication extends Application {
	/**
	 * static constants
	 */
	// account type
	public static final String ACCOUNT_TYPE = "com.wetongji";
	public static final String AUTHTOKEN_TYPE = "com.wetongji";
	// api_constrants
	public static final String API_DEVICE = "android";
	public static final String API_VERSION = "3.0";

	// database loader IDs
	public static final int DB_LIST_LOADER = 5;
	public static final int DB_LIST_SAVER = 6;
	public static final int ACTIVITIES_SAVER = 7;
	public static final int EVENTS_SAVER = 8;
	public static final int COURSES_SAVER = 9;
	public static final int EXAMS_SAVER = 10;
	public static final int ACTIVITIES_LOADER = 11;
	public static final int EVENTS_LOADER = 12;
	public static final int INFORMATION_SAVER = 14;
	public static final int PERSON_SAVER = 15;
	public static final int INFORMATION_LOADER = 16;
	public static final int EVENT_LOADER = 17;
	public static final int SCHEDUL_LOADER = 18;
	public static final int USER_SAVER = 19;
	public static final int USER_LOADER = 20;
	public static final int PERSON_LOADER = 21;
	public static final int SEARCH_SAVER = 22;
	public static final int SEARCH_LOADER = 23;
	public static final int NOTIFICCATOINS_SAVER = 31;
	public static final int NOTIFICATIONS_LOADER = 13;
	public static final int USER_PROFILE_SAVER = 32;

	// network loader IDs
	public static final int NETWORK_LOADER_DEFAULT = 1;
	public static final int NETWORK_LOADER_1 = 2;
	public static final int NETWORK_LOADER_2 = 3;
	public static final int NETWORK_LOADER_3 = 4;
	public static final int UPLOAD_AVATAR_LOADER = 24;
	public static final int NETWORK_LOADER_SEARCH = 25;
	public static final int NETWORK_LOADER_INVITE = 26;
	public static final int NETWORK_LOADER_ACCEPT_FRIEND = 27;
	public static final int NETWORK_LOADER_IGNORE_FRIEDN = 28;
	public static final int NETWORK_LOADER_LIKE = 29;
	public static final int NETWORK_LOADER_FRIENDS = 30;
	public static final int NETWORK_LOADER_FORGET_PWD = 33;

	public static final String FLURRY_API_KEY = "GN5KJMW6XWCSD5DTCWRW";
	public static final String MISSING_IMAGE_URL = "http://we.tongji.edu.cn/images/original/missing.png";

	// singleton
	private static WTApplication application = null;

	private DbHelper dbHelper;

	private AQuery aq;

	private Activity activity = null;
	private DisplayMetrics displayMetrics = null;

	public boolean hasAccount;
	public String session;
	public String uid;

	public static WTApplication getInstance() {
		return application;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		application = this;
		dbHelper = OpenHelperManager.getHelper(this, DbHelper.class);

		upDateDatabase();
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType(ACCOUNT_TYPE);
		hasAccount = accounts.length > 0;
	}

	private void upDateDatabase() {
		// drop old data base
		int versionCode = 0;
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			versionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (versionCode >= 5) {
			String preferenceName = "FirstOpen-v" + versionCode;
			SharedPreferences sp = getAppPreference();
			boolean isFirst = sp.getBoolean(preferenceName, true);
			if (isFirst) {
				deleteDatabase("wetongji.db");
				sp.edit().putBoolean(preferenceName, false).apply();
			}
		}
	}

	public DbHelper getDbHelper() {
		return dbHelper;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		OpenHelperManager.releaseHelper();
	}

	public AQuery getAq(Activity activity) {
		aq = new AQuery(activity);
		// Instantiate AQuery and configure cache directory
		if (Environment.getExternalStorageState().compareTo(
				Environment.MEDIA_MOUNTED) == 0) {
			// File ext = Environment.getExternalStorageDirectory();
			File downloadCacheDir = getExternalFilesDir("imgCache");

			AQUtility.setCacheDir(downloadCacheDir);
		}
		return aq;
	}

	public DisplayMetrics getDisplayMetrics() {
		if (displayMetrics != null) {
			return displayMetrics;
		} else {
			Activity a = getActivity();
			if (a != null) {
				Display display = getActivity().getWindowManager()
						.getDefaultDisplay();
				DisplayMetrics metrics = new DisplayMetrics();
				display.getMetrics(metrics);
				this.displayMetrics = metrics;
				return metrics;
			} else {
				// default screen is 800x480
				DisplayMetrics metrics = new DisplayMetrics();
				metrics.widthPixels = 480;
				metrics.heightPixels = 800;
				return metrics;
			}
		}
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public String getServerBaseUrl() {
		SharedPreferences sp = getAppPreference();
		Boolean bUseTest = sp.getBoolean("pre_use_test_server", true);
		String url = bUseTest ? "http://leiz.name:8080/api/call"
				: "http://we.tongji.edu.cn/api/call";
		return url;
	}

	public SharedPreferences getAppPreference() {
		StringBuilder sb = new StringBuilder(getPackageName());
		SharedPreferences sp = getSharedPreferences(sb.append("_preferences")
				.toString(), MODE_PRIVATE);
		return sp;
	}
}
