package org.beavers.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameState;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public abstract class GameListAdapter extends BaseAdapter {

	public GameListAdapter() {
		games = new ArrayList<Game>();
	}

	@Override
	public int getCount() {
		return games.size();
	}

	@Override
	public Object getItem(final int pPosition) {
		return games.get(pPosition);
	}

	@Override
	public long getItemId(final int pPosition) {
		return pPosition;
	}

	@Override
	public View getView(final int pPosition, View pConvertView,
	                    final ViewGroup pParent) {
		final App app = ((App) pParent.getContext().getApplicationContext());
		final Settings settings = app.getSettings();

		ViewHolder holder;
		if (pConvertView == null) {
			pConvertView = LayoutInflater.from(pParent.getContext())
				.inflate(R.layout.game_list_row, null);

			holder = new ViewHolder();
			holder.txtName = findTextView(pConvertView, R.id.name);
			holder.txtServer = findTextView(pConvertView, R.id.server);
			holder.txtState = findTextView(pConvertView, R.id.state);

			pConvertView.setTag(holder);
		} else {
			holder = (ViewHolder) pConvertView.getTag();
		}

		final Game item = (Game) getItem(pPosition);

		holder.txtName.setText(item.getName());

		if(item.isServer(settings.getPlayer()))
		{
			holder.txtServer.setText("");

			holder.txtState.setText(
				app.getString(R.string.state) + ": "
				+ (item.isInState(app, GameState.JOINED)?
				  app.getString(R.string.state_waiting):
				  app.getString(item.getState(app).getResId())));
		} else {
			holder.txtServer.setText(item.getServer().getName());

			holder.txtState.setText(
				app.getString(R.string.state) + ": "
				+ app.getString(item.getState(app).getResId()));
		}

		return pConvertView;
	}

	@Override
	public void notifyDataSetChanged() {
		games = new ArrayList<Game>(Arrays.asList(fetchList()));

		super.notifyDataSetChanged();
	}

	protected abstract Game[] fetchList();

	private ArrayList<Game> games;

	private TextView findTextView(final View pParent, final int pID)
	{
		final View view = pParent.findViewById(pID);

		if(view instanceof TextView)
		{
			return (TextView) view;
		}
		else
		{
			return null;
		}
	}

	private class ViewHolder {
		TextView txtName;
		TextView txtServer;
		TextView txtState;
	}
}
