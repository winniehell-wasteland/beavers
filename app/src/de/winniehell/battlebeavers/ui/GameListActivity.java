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

package de.winniehell.battlebeavers.ui;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.winniehell.battlebeavers.App;
import de.winniehell.battlebeavers.R;
import de.winniehell.battlebeavers.Settings;
import de.winniehell.battlebeavers.communication.Client;
import de.winniehell.battlebeavers.communication.Client.ClientRemoteException;
import de.winniehell.battlebeavers.communication.Server;
import de.winniehell.battlebeavers.communication.Server.ServerRemoteException;
import de.winniehell.battlebeavers.gameplay.Game;
import de.winniehell.battlebeavers.gameplay.GameActivity;
import de.winniehell.battlebeavers.gameplay.GameState;
import de.winniehell.battlebeavers.gameplay.Player;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

/**
 * activity for displaying a game list
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameListActivity extends FragmentActivity {

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = GameListActivity.class.getName();
	/**
	 * @}
	 */

	/**
	 * @name tabs
	 * @{
	 */
	private static final String TAB_ANNOUNCED = "ANNOUNCED";
	private static final String TAB_RUNNING = "RUNNING";

	private TabHost tabHost;
	/**
	 * @}
	 */

	public GameListActivity()
	{
		super();

		client = new Client.Connection();
		server = new Server.Connection();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
	    final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_start_game:
		{
			showGameStartDialog();

			return true;
		}
		case R.id.menu_debug_dummy_game:
		{
	        final Settings settings = ((App)getApplication()).getSettings();

			// create new game
			final Game game = new Game(settings.getPlayer(),
			                           UUID.randomUUID(),
			                           "created dummy game");

			try {
				// announce to clients
				server.getService().initiateGame(game);
			} catch (final RemoteException e) {
				((ServerRemoteException)e).log();
			}

			tabHost.setCurrentTabByTag(TAB_ANNOUNCED);

			return true;
		}
		case R.id.menu_debug_delete_games:
		{
			try {
				Game.deleteAll(this);

				client.getService().deleteGames();
				server.getService().deleteGames();
			} catch (final RemoteException e) {
				Log.e(TAG, "Could not delete games!", e);
			}
		}
		}

		return false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.game_list_activity);

		final Settings settings = ((App) getApplication()).getSettings();
		Log.d(TAG, "This is player " + settings.getPlayer().getId());

		Intent intent = new Intent(GameListActivity.this, Client.class);
		if(!bindService(intent, client, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_client));
			return;
		}

		intent = new Intent(GameListActivity.this, Server.class);
		if(!bindService(intent, server, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_server));
			return;
		}

		announcedGames = new GameListView(this) {

			@Override
			protected void onPrepareContextMenu(final MenuInflater pInflater,
				final ContextMenu pMenu, final Game pGame) {

				pInflater.inflate(R.menu.context_announced_game, pMenu);

				pMenu.findItem(R.id.context_menu_join)
				.setVisible(
					pGame.isInState(GameListActivity.this, GameState.ANNOUNCED)
					&& !pGame.isServer(getSettings().getPlayer())
				);
			}

			@Override
			public boolean onMenuItemClick(final MenuItem pItem) {
				final Game game = getSelectedItem();

				if(game == null) {
					return false;
				}

				setSelection(-1);

				switch(pItem.getItemId())
				{
				case R.id.context_menu_join:
				{
					Log.d(TAG, "Trying to join "+game+"...");

					try {
						client.getService().joinGame(game);
					} catch (final RemoteException e) {
						((ClientRemoteException)e).log();
					}

					return true;
				}
				case R.id.context_menu_add_player:
				{
					Log.d(TAG, "Adding dummy player...");

					try {
						final Player dummyPlayer =
							new Player(UUID.randomUUID(), "dummy player");

						server.getService().addPlayer(game, dummyPlayer);
					} catch (final RemoteException e) {
						((ServerRemoteException)e).log();
					}

					showGame(game);

					return true;
				}
				default:
					return false;
				}
			}
		};

		announcedGames.setAdapter(new GameListAdapter() {

			@Override
			protected Game[] fetchList() {
				try {
					return client.getService().getAnnouncedGames();
				} catch (final RemoteException e) {
					((ClientRemoteException)e).log();
				}

				return null;
			}
		});

		runningGames = new GameListView(this) {

			@Override
			protected void onPrepareContextMenu(final MenuInflater pInflater,
				final ContextMenu pMenu, final Game pGame) {

				pInflater.inflate(R.menu.context_running_game, pMenu);
			}

			@Override
			public boolean onMenuItemClick(final MenuItem pItem) {
				final Game game = getSelectedItem();

				if(game == null) {
					return false;
				}

				setSelection(-1);

				switch(pItem.getItemId())
				{
				case R.id.context_menu_load_game:
				{
					showGame(game);

					return true;
				}
				default:
					return false;
				}
			}
		};

		runningGames.setAdapter(new GameListAdapter() {

			@Override
			protected Game[] fetchList() {
				try {
					return client.getService().getRunningGames();
				} catch (final RemoteException e) {
					((ClientRemoteException)e).log();
				}

				return null;
			}
		});

		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();

		addTab(TAB_RUNNING, R.string.menu_running_games, runningGames);
		addTab(TAB_ANNOUNCED, R.string.menu_announced_games, announcedGames);

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(final String tabId) {
				updateGameList();
			}

		});

		tabHost = (TabHost) findViewById(android.R.id.tabhost);

		updateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(final Context context, final Intent intent) {
				updateGameList();
			}
		};

		Log.d(TAG, "onCreate() finished");
	}

	private void addTab(final String pTag, final int pIndicator,
	                    final View pContent) {

		tabHost.addTab(tabHost.newTabSpec(pTag)
			.setIndicator(getString(pIndicator))
			.setContent(new TabContentFactory() {

				@Override
				public View createTabContent(final String tag) {
					return pContent;
				}

			})
		);

		final View view = tabHost.getTabWidget().getChildTabViewAt(
			tabHost.getTabWidget().getTabCount() - 1
		);

		view.getLayoutParams().height *= 0.66;

		final TextView tabTitle =
			(TextView) view.findViewById(android.R.id.title);

		tabTitle.setGravity(Gravity.CENTER);
		tabTitle.setSingleLine(false);

		tabTitle.getLayoutParams().height = ViewGroup.LayoutParams.FILL_PARENT;
		tabTitle.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
	}

	private void updateGameList() {
		if(tabHost == null) {
			return;
		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if(tabHost.getCurrentTabTag().equals(TAB_ANNOUNCED)) {
					if(announcedGames != null) {
						announcedGames.getAdapter().notifyDataSetChanged();
					}
				}
				else if(tabHost.getCurrentTabTag().equals(TAB_RUNNING)) {
					if(runningGames != null) {
						runningGames.getAdapter().notifyDataSetChanged();
					}
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		unbindService(client);
		unbindService(server);

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(updateReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(updateReceiver,
			new IntentFilter(Game.STATE_CHANGED_INTENT));

		(new Timer()).schedule(new TimerTask() {

			@Override
			public void run() {
				updateGameList();
			}
		}, 1000);
	}

	/**
	 * @name list views
	 * @{
	 */
	private GameListView announcedGames;
	private GameListView runningGames;
	/**
	 * @}
	 */

	private BroadcastReceiver updateReceiver;

	/**
	 * @name service connections
	 * @{
	 */
	private final Client.Connection client;
	private final Server.Connection server;
	/**
	 * @}
	 */

	private Settings getSettings() {
		if(getApplication() instanceof App) {
			return ((App) getApplication()).getSettings();
		}
		else {
			return null;
		}
	}

	private void showGame(final Game game) {
		tabHost.setCurrentTabByTag(TAB_RUNNING);

		final Intent intent =
			new Intent(GameListActivity.this, GameActivity.class);
		intent.putExtra(Game.PARCEL_NAME, game);
		startActivity(intent);
	}

	private void showGameStartDialog() {
	    final FragmentTransaction transaction =
	    	getSupportFragmentManager().beginTransaction();
	    final Fragment old =
	    	getSupportFragmentManager().findFragmentByTag("dialog");

	    if (old != null) {
	    	transaction.remove(old);
	    }
	    transaction.addToBackStack(null);

	    final DialogFragment dialog = new GameStartDialog();
	    dialog.show(transaction, "dialog");
	}

	/**
	 * gets opened when presses "start game"
	 */
	private static class GameStartDialog extends DialogFragment {

		/** default constructor */
		public GameStartDialog()
		{

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final GameListActivity activity =  (GameListActivity)getActivity();

			final LayoutInflater inflater = LayoutInflater.from(activity);
	        final View layout = inflater.inflate(R.layout.start_game_dialog, null);

	        final Settings settings = ((App)getActivity().getApplication())
	        	.getSettings();

	        return new AlertDialog.Builder(activity)
	        .setView(layout)
			.setTitle(R.string.title_start_game)
            .setCancelable(true)
			.setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
						final EditText input = (EditText) getDialog()
							.findViewById(R.id.edit_game_name);

						if(input.getText().length() < 1)
						{
							dialog.cancel();
							return;
						}

						// create new game
						final Game game = new Game(settings.getPlayer(),
						                           UUID.randomUUID(),
						                           input.getText().toString());

						try {
							// announce to clients
							activity.server.getService().initiateGame(game);
						} catch (final RemoteException e) {
							((ServerRemoteException)e).log();
						}

						// close dialog before Activity gets destroyed
						dismiss();

						activity.tabHost.setCurrentTabByTag(TAB_ANNOUNCED);
					}
				}
			)
			.setNegativeButton(R.string.button_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				}
			).create();
		}
	}
}
