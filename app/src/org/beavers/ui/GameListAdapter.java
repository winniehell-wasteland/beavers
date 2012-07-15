package org.beavers.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameInfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.TextView;

public abstract class GameListAdapter extends BaseAdapter {

	public GameListAdapter() {
		games = new ArrayList<Game>(Arrays.asList(fetchList()));
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
			pConvertView = getLayoutInflater()
				.inflate(R.layout.custom_row_view, null);

			holder = new ViewHolder();
			holder.txtName = findTextView(pConvertView, R.id.name);
			holder.txtServer = findTextView(pConvertView, R.id.server);
			holder.txtState = findTextView(pConvertView, R.id.state);

			pConvertView.setTag(holder);
		} else {
			holder = (ViewHolder) pConvertView.getTag();
		}

		final Game item = (Game) getItem(pPosition);
		final GameInfo info = GameInfo.fromFile(app, item);

		holder.txtName.setText(item.getName());

		if(item.isServer(settings.getPlayer()))
		{
			holder.txtServer.setText("");
		} else {
			holder.txtServer.setText(item.getServer().getName());
		}

		holder.txtState.setText(
			app.getString(R.string.state) + ": "
			+ app.getString(info.getState().getResId()));

		return pConvertView;
	}

	@Override
	public void notifyDataSetChanged() {
		games = new ArrayList<Game>(Arrays.asList(fetchList()));

		super.notifyDataSetChanged();
	}

	protected abstract Game[] fetchList();
	protected abstract LayoutInflater getLayoutInflater();

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

	private class ViewHolder implements Checkable {
		TextView txtName;
		TextView txtServer;
		TextView txtState;

		private boolean checked = false;

		@Override
		public boolean isChecked() {
			return checked;
		}
		@Override
		public void setChecked(final boolean checked) {
			this.checked = checked;
		}
		@Override
		public void toggle() {
			checked = !checked;
		}
	}
}
