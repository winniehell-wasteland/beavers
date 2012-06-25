package org.beavers;

import java.util.Timer;
import java.util.TimerTask;

import org.beavers.communication.CustomDTNClient;
import org.beavers.communication.CustomDTNDataHandler;
import org.beavers.ui.GameListActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

public class AppActivity extends Activity {
	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		dtnClient = new CustomDTNClient(this);
		dtnDataHandler = new CustomDTNDataHandler(this, dtnClient);

        dtnClient.setDataHandler(dtnDataHandler);
        dtnClient.initialize();

	    final Intent intent = new Intent(AppActivity.this, GameListActivity.class);
	    intent.setAction(GameListActivity.RUNNING);
	    startActivityForResult(intent, 0);

	    finish();
	}

	@Override
	protected void onDestroy() {
		// unregister at the daemon
		dtnClient.unregister();

		dtnDataHandler.stop();

		// destroy DTN client
		dtnClient.terminate();

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		(new Timer()).schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if((dtnClient.getDTNService() != null)
							&& dtnClient.getDTNService().isRunning())
					{
						return;
					}
				} catch (final RemoteException e) {
					// something bad happened to the binder
				}

				AppActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						final AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
						builder.setMessage("Error initializing DTN service! Check if daemon is running!")
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int id) {
								AppActivity.this.finish();
							}
						});
						final AlertDialog alert = builder.create();
						alert.show();
					}
				});
			}
		}, 100);
	};

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = "AppActivity";
	/**
	 * @}
	 */

	private CustomDTNClient dtnClient;
	private CustomDTNDataHandler dtnDataHandler;
}

