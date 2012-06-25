/**
 *
 */
package org.beavers.ui;

import java.util.UUID;

import org.beavers.R;
import org.beavers.Settings;
import org.beavers.communication.Client;
import org.beavers.communication.Server;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameActivity;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

/**
 * activity for displaying a game list
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameListActivity extends Activity
	implements OnMenuItemClickListener {

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
		updateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(final Context context, final Intent intent) {
				// update game list
				listView.getAdapter().notifyDataSetChanged();
			}
		};
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Client.announcedGames.add(new GameInfo(Settings.playerID, new Game(UUID.randomUUID(), "my game"), "test"));
		Client.runningGames.add(new GameInfo(Settings.playerID, new Game(UUID.randomUUID(), "my game2"), "test"));

		loadList();
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
		case R.id.menu_join:
		{
			assert listView.getCheckedItemIds().length == 1;
			final Intent intent = new Intent(Client.JOIN_GAME_INTENT);
			intent.putExtra("game", (GameInfo) listView.getItemAtPosition(listView.getCheckedItemPosition()));
			startActivity(intent);

			return true;
		}
		}
		return false;
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		loadList();
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
			// show game
			final Intent intent = new Intent(GameListActivity.this, GameActivity.class);
			startActivity(intent);

			// announce
			final GameInfo newGame = new GameInfo(
				Settings.playerID,
				new Game(UUID.randomUUID(), "my game"),
				"map");
			Server.initiateGame(this, newGame);

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
	private final BroadcastReceiver updateReceiver;

	private void loadList()
	{
		GameList list = null;

		if(getIntent().getAction().equals(ANNOUNCED))
		{
			list = Client.announcedGames;
		}
		else if(getIntent().getAction().equals(RUNNING))
		{
			list = Client.runningGames;
		}
		else
		{
			finish();
		}

		listView = new GameListView(this, list) {
			@Override
			protected void onCreateContextMenu(final ContextMenu menu) {
		        final MenuInflater inflater = getMenuInflater();

				if(getIntent().getAction().equals(ANNOUNCED))
				{
			        inflater.inflate(R.menu.context_announced_game, menu);
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
}
