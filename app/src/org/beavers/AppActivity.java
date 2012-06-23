package org.beavers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.beavers.communication.Client;
import org.beavers.communication.CustomDTNClient;
import org.beavers.communication.CustomDTNDataHandler;
import org.beavers.communication.Server;
import org.beavers.gameplay.GameActivity;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.ui.GameListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class AppActivity extends Activity {
	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		dtnClient = new CustomDTNClient(this);
		dtnDataHandler = new CustomDTNDataHandler(this, dtnClient);

        dtnClient.setDataHandler(dtnDataHandler);
        dtnClient.initialize();

	    announcedGamesView = new GameListView(this, Client.announcedGames) {
			@Override
			public boolean onMenuItemClick(final MenuItem pItem) {
				switch(pItem.getItemId())
				{
				case R.id.menu_join:
					assert announcedGamesView.getCheckedItemIds().length == 1;
					final Intent intent = new Intent(Client.JOIN_GAME_INTENT);
					intent.putExtra("game", (GameInfo) announcedGamesView.getItemAtPosition(announcedGamesView.getCheckedItemPosition()));
					startActivity(intent);

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

	    runningGamesView = new GameListView(this, Client.runningGames) {
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

	    startActivity(new Intent(AppActivity.this, Server.class));
	    startActivity(new Intent(AppActivity.this, Client.class));
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
	    final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

	    return true;
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
		switch (item.getItemId()) {
		case R.id.menu_start_game:

			// show game
			final Intent intent = new Intent(AppActivity.this, GameActivity.class);
			startActivity(intent);

			// announce
			final GameInfo newGame = new GameInfo(Settings.playerID, new GameID(UUID.randomUUID().toString()));
			Server.initiateGame(this, newGame);

			return true;
			// TODO
			/*
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
			 */
		default:
			return super.onOptionsItemSelected(item);
		}
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

	private static final String TAG = "AppActivity";

	private CustomDTNClient dtnClient;
	private CustomDTNDataHandler dtnDataHandler;

	private ViewGroup frameLayout;
	private GameListView announcedGamesView;
	private GameListView runningGamesView;

	private boolean isShowing(final View pView) {
		return (pView.getParent() == frameLayout);
	}
}

