package org.beavers;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class App extends Application {

	public Settings getSettings() {
		return settings;
	}

	@Override
	public void onCreate() {
		Log.d(App.class.getSimpleName(), "onCreate()");
		super.onCreate();
		settings = new Settings(this);
		Log.d(App.class.getSimpleName(), "created...");
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		Log.i(App.class.getSimpleName(), "Conf changed..." );
		//android.os.Process.killProcess(android.os.Process.myPid());
		//super.onConfigurationChanged(newConfig);
	}

	private Settings settings;
}
