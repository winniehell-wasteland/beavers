/**
 *
 */
package org.beavers.ui;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.communication.Client;
import org.beavers.communication.Client.ClientRemoteException;
import org.beavers.communication.Server;
import org.beavers.communication.Server.ServerRemoteException;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameActivity;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.Player;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.EditText;

/**
 * activity for displaying a game list
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameListActivity extends FragmentActivity
	implements OnMenuItemClickListener {

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = GameListActivity.class.getName();
	/**
	 * @}
	 */

	/**
	 * @name intents
	 * @{
	 */
	public static final String ANNOUNCED =
			GameListActivity.class.getName() + ".ANNOUNCED";
	public static final String RUNNING =
		GameListActivity.class.getName() + ".RUNNING";
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
	public boolean onMenuItemClick(final MenuItem pItem) {
		if(listView.getSelectedItemPosition() == -1) {
			return false;
		}

		final Game game = listView.getSelectedItem();
		listView.setSelection(-1);

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
		case R.id.context_menu_load_game:
		{
			showGame(game);

			return true;
		}
		case R.id.context_menu_add_player:
		{
			Log.d(TAG, "Adding dummy player...");

			try {
				server.getService().addPlayer(game, new Player(UUID.randomUUID(), "dummy player"));
			} catch (final RemoteException e) {
				((ServerRemoteException)e).log();
			}

			showGame(game);

			return true;
		}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_announced_games:
		{
			showAnnouncedGames();

			return true;
		}
		case R.id.menu_running_games:
		{
			showRunningGames();

			return true;
		}
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

			showAnnouncedGames();

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
	public boolean onPrepareOptionsMenu(final Menu menu) {
		menu.findItem(R.id.menu_announced_games).setVisible(!getIntent().getAction().equals(ANNOUNCED));
		menu.findItem(R.id.menu_running_games).setVisible(!getIntent().getAction().equals(RUNNING));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		loadList();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		(new Timer()).schedule(
			new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							loadList();
						}
					});
				}
			}, 1000);

		updateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(final Context context, final Intent intent) {
				// update game list
				listView.getAdapter().notifyDataSetChanged();
			}
		};
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

		if(isFinishing() && !getIntent().getAction().equals(RUNNING))
		{
			showRunningGames();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		final Settings settings = ((App) getApplication()).getSettings();
		Log.d(TAG, "This is player " + settings.getPlayer().getId());

		registerReceiver(updateReceiver,
			new IntentFilter(Game.STATE_CHANGED_INTENT));

		// update game list
		if(listView != null) {
			listView.getAdapter().notifyDataSetChanged();
		}
	}

	private GameListView listView;
	private BroadcastReceiver updateReceiver;

	private final Client.Connection client;
	private final Server.Connection server;

	private void loadList()
	{
		GameListAdapter adapter = null;

		if(getIntent().getAction().equals(ANNOUNCED))
		{
			adapter = new GameListAdapter() {

				@Override
				protected Game[] fetchList() {
					try {
						return client.getService().getAnnouncedGames();
					} catch (final RemoteException e) {
						((ClientRemoteException)e).log();
					}

					return null;
				}

				@Override
				protected LayoutInflater getLayoutInflater() {
					return GameListActivity.this.getLayoutInflater();
				}

			};
		}
		else
		{
			if(!getIntent().getAction().equals(RUNNING))
			{
				setIntent(new Intent(RUNNING));
			}

			adapter = new GameListAdapter() {

				@Override
				protected Game[] fetchList() {
					try {
						return client.getService().getRunningGames();
					} catch (final RemoteException e) {
						((ClientRemoteException)e).log();
					}

					return null;
				}

				@Override
				protected LayoutInflater getLayoutInflater() {
					return GameListActivity.this.getLayoutInflater();
				}

			};
		}

		listView = new GameListView(this, adapter) {
			@Override
			protected void onCreateContextMenu(final ContextMenu menu) {
				final Game game = (Game) getItemAtPosition(getCheckedItemPosition());

				if(game == null)
				{
					menu.clear();
					return;
				}

		        final MenuInflater inflater = getMenuInflater();

				if(getIntent().getAction().equals(ANNOUNCED))
				{
					inflater.inflate(R.menu.context_announced_game, menu);

					menu.findItem(R.id.context_menu_join)
					.setVisible(
						game.isInState(GameListActivity.this,
						               GameState.ANNOUNCED)
					);
				}
				else if(getIntent().getAction().equals(RUNNING))
				{
			        inflater.inflate(R.menu.context_running_game, menu);
				}

		        for(int i = 0; i < menu.size(); ++i)
		        {
		        	menu.getItem(i).setOnMenuItemClickListener(GameListActivity.this);
		        }
			}

		};

		setContentView(listView);
	}

	private void showAnnouncedGames() {
		final Intent intent = new Intent(GameListActivity.this, GameListActivity.class);
		intent.setAction(ANNOUNCED);
		startActivity(intent);
	}

	private void showGame(final Game game) {
		showRunningGames();

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

	private void showRunningGames() {
		final Intent intent =
			new Intent(GameListActivity.this, GameListActivity.class);
		intent.setAction(RUNNING);
		startActivity(intent);
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

						activity.showAnnouncedGames();
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
