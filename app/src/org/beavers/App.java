package org.beavers;

import android.app.Application;

public class App extends Application {

	public Settings getSettings() {
		return settings;
	}

	@Override
	public void onCreate() {
		settings = new Settings(this);

	}

	private Settings settings;
}
