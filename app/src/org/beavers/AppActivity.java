package org.beavers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.beavers.communication.Client;
import org.beavers.communication.CustomDTNClient;
import org.beavers.communication.CustomDTNDataHandler;
import org.beavers.communication.Server;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.PlayerID;
import org.beavers.ui.GameListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.tubs.ibr.dtn.api.DTNClient.Session;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionDestroyedException;

public class AppActivity extends Activity {

	public AppActivity()
	{
		playerID = new PlayerID(UUID.randomUUID().toString());

		client = new Client(this);
		server = new Server(this);
	}

	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		dtnClient = new CustomDTNClient(this);
		dtnDataHandler = new CustomDTNDataHandler(dtnClient, server, client);

        dtnClient.setDataHandler(dtnDataHandler);

        try {
        	final Registration registration = new Registration("game/server");

        	registration.add(Server.GROUP_EID);
        	registration.add(Client.GROUP_EID);

			dtnClient.initialize(registration);
		} catch (final ServiceNotAvailableException e) {
			// handled in onStart()
		}

	    announcedGamesView = new GameListView(this, client.announcedGames) {
			@Override
			public boolean onMenuItemClick(final MenuItem pItem) {
				switch(pItem.getItemId())
				{
				case R.id.menu_join:
					assert announcedGamesView.getCheckedItemIds().length == 1;
					client.joinGame((GameInfo) announcedGamesView.getItemAtPosition(announcedGamesView.getCheckedItemPosition()));

					return true;
				default:
					return false;
				}
			}

			@Override
			protected int getContextMenuRes() {
				return R.menu.context_announced_game;
			}

	    };

	    runningGamesView = new GameListView(this, client.runningGames) {
			@Override
			public boolean onMenuItemClick(final MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected int getContextMenuRes() {
				return R.menu.context_running_game;
			}

	    };

	    frameLayout = new FrameLayout(this);
	    final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
	    		FrameLayout.LayoutParams.FILL_PARENT,
	    		FrameLayout.LayoutParams.FILL_PARENT);
	    setContentView(frameLayout, layoutParams);

	    frameLayout.addView(runningGamesView);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
	    final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

	    return true;
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "destroying app...");


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

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {

		if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {

			if(isShowing(runningGamesView))
		    {
				finish();
		    }
			else
			{
		    	frameLayout.removeAllViews();
			    frameLayout.addView(runningGamesView);
			}

			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// TODO
		/*
			switch (item.getItemId()) {
			case R.id.menu_start_game:

				if(!isShowing(mRenderSurfaceView))
				{
					frameLayout.removeAllViews();

					final FrameLayout.LayoutParams surfaceViewLayoutParams = new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());
					frameLayout.addView(mRenderSurfaceView, surfaceViewLayoutParams);
				}

				final GameInfo newGame = new GameInfo(getPlayerID(), new GameID(UUID.randomUUID().toString()));
				server.initiateGame(newGame);

				return true;
			case R.id.menu_join_game:

				if(!isShowing(announcedGamesView))
				{
					frameLayout.removeAllViews();
					frameLayout.addView(announcedGamesView);
				}

				return true;
			case R.id.menu_running_games:

				if(!isShowing(runningGamesView))
				{
					frameLayout.removeAllViews();
					frameLayout.addView(runningGamesView);
				}

				return true;
			case R.id.menu_quit:
				finish();

				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		*/

		return false;
	}

	public PlayerID getPlayerID() {
		return playerID;
	}

	public Client getClient() {
		return client;
	}

	public Session getDTNSession() throws SessionDestroyedException, InterruptedException {
		return dtnClient.getSession();
	}

	public Server getServer() {
		return server;
	}

	/**
	 * update the visible game
	 * @param pGame changed game
	 */
	public void updateGame(final GameInfo pGame) {
		if(isShowing(announcedGamesView))
		{
			announcedGamesView.getAdapter().notifyDataSetChanged();
		}
		else if(isShowing(runningGamesView))
		{
			runningGamesView.getAdapter().notifyDataSetChanged();
		}
	}

	@SuppressWarnings("unused")
	private static final String TAG = "AppActivity";

	private final PlayerID playerID;

	private final Client client;
	private final Server server;

	private CustomDTNClient dtnClient;
	private CustomDTNDataHandler dtnDataHandler;

	private ViewGroup frameLayout;
	private GameListView announcedGamesView;
	private GameListView runningGamesView;

	private boolean isShowing(final View pView) {
		return (pView.getParent() == frameLayout);
	}
}
