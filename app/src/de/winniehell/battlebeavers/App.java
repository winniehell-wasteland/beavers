/*
	(c) winniehell (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers;

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
