package org.beavers.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.GameInfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public abstract class GameListAdapter extends BaseAdapter {

	public GameListAdapter() {
		keys = new ArrayList<String>(Arrays.asList(fetchKeys()));
	}

	@Override
	public Object getItem(final int pPosition) {
		if((0 <= pPosition) && (pPosition < keys.size()))
		{
			return getItem(keys.get(pPosition));
		}
		else
		{
			return null;
		}
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

		final GameInfo item = (GameInfo) getItem(pPosition);

		holder.txtName.setText(item.getGame().getName());

		if(item.isServer(settings.getPlayer()))
		{
			holder.txtServer.setText("");
		} else {
			holder.txtServer.setText(item.getServer().getName());
		}

		holder.txtState.setText(
			app.getString(R.string.state) + ": "
			+ item.getState().getName(app));

		return pConvertView;
	}

	@Override
	public void notifyDataSetChanged() {
		keys = new ArrayList<String>(Arrays.asList(fetchKeys()));

		super.notifyDataSetChanged();
	}

	protected abstract String[] fetchKeys();
	protected abstract GameInfo getItem(String pKey);
	protected abstract LayoutInflater getLayoutInflater();

	private ArrayList<String> keys;

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
