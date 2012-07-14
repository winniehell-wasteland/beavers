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
import org.beavers.communication.Server;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameActivity;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameState;

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
		switch(pItem.getItemId())
		{
		case R.id.context_menu_join:
		{
			assert listView.getCheckedItemIds().length == 1;
			final GameInfo game = (GameInfo) listView.getItemAtPosition(
				listView.getCheckedItemPosition()
			);

			Log.d(TAG, "Trying to join "+game+"...");

			try {
				client.getService().joinGame(game);
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_join_game:
		{
			final Intent intent = new Intent(GameListActivity.this, GameListActivity.class);
			intent.setAction(ANNOUNCED);
			startActivity(intent);

			return true;
		}
		case R.id.menu_running_games:
		{
			final Intent intent = new Intent(GameListActivity.this, GameListActivity.class);
			intent.setAction(RUNNING);
			startActivity(intent);

			return true;
		}
		case R.id.menu_start_game:
		{
			showGameStartDialog();

			return true;
		}
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		menu.findItem(R.id.menu_join_game).setVisible(!getIntent().getAction().equals(ANNOUNCED));
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
			Log.e(TAG, getString(R.string.error_binding_client_failed));
			return;
		}

		intent = new Intent(GameListActivity.this, Server.class);
		if(!bindService(intent, server, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_server_failed));
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
			final Intent intent = new Intent(GameListActivity.this, GameListActivity.class);
			intent.setAction(RUNNING);
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(updateReceiver,
			new IntentFilter(Client.GAME_STATE_CHANGED_INTENT));
	}

	private GameListView listView;
	private BroadcastReceiver updateReceiver;

	void showGameStartDialog() {
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

	private void loadList()
	{
		GameListAdapter adapter = null;

		if(getIntent().getAction().equals(ANNOUNCED))
		{
			adapter = new GameListAdapter() {

				@Override
				protected String[] fetchKeys() {
					try {
						return client.getService().getAnnouncedGames();
					} catch (final RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return null;
				}

				@Override
				public int getCount() {
					try {
						return client.getService().getAnnouncedGamesCount();
					} catch (final RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return 0;
				}

				@Override
				protected GameInfo getItem(final String pKey) {
					try {
						return client.getService().getAnnouncedGame(pKey);
					} catch (final RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				protected String[] fetchKeys() {
					try {
						return client.getService().getRunningGames();
					} catch (final RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return null;
				}

				@Override
				public int getCount() {
					try {
						return client.getService().getRunningGamesCount();
					} catch (final RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return 0;
				}

				@Override
				protected GameInfo getItem(final String pKey) {
					try {
						return client.getService().getRunningGame(pKey);
					} catch (final RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				final GameInfo game = (GameInfo) getItemAtPosition(getCheckedItemPosition());

				if(game == null)
				{
					menu.clear();
					return;
				}

		        final MenuInflater inflater = getMenuInflater();

				if(getIntent().getAction().equals(ANNOUNCED))
				{
			        inflater.inflate(R.menu.context_announced_game, menu);

			        menu.findItem(R.id.context_menu_join).setVisible(
			        	game.getState().equals(GameState.ANNOUNCED));
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

	/**
	 * gets opened when presses "start game"
	 */
	public static class GameStartDialog extends DialogFragment {

		/** default constructor */
		public GameStartDialog()
		{

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final LayoutInflater inflater = LayoutInflater.from(getActivity());
	        final View layout = inflater.inflate(R.layout.start_game_dialog, null);

	        final Settings settings = ((App)getActivity().getApplication())
	        	.getSettings();

	        return new AlertDialog.Builder(getActivity())
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
						final GameInfo game = new GameInfo(
							settings.getPlayer(),
							new Game(UUID.randomUUID(),
							         input.getText().toString()));

						try {
							// announce to clients
							((GameListActivity)getActivity()).server.getService()
								.initiateGame(game);
						} catch (final RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// close dialog before Activity gets destroyed
						dismiss();

						// show game
						final Intent intent =
							new Intent(getActivity(), GameActivity.class);
						intent.putExtra(GameInfo.PARCEL_NAME, game);
						startActivity(intent);
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

	private final Client.Connection client;
	private final Server.Connection server;
}
