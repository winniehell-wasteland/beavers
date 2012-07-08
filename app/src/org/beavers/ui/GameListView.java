package org.beavers.ui;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.GameInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
/**
 * list view for GameList
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
@SuppressLint("ViewConstructor")
public abstract class GameListView extends ListView
	implements OnItemClickListener {

	public GameListView(final Context pContext,
	                    final ListViewAdapter pAdapter) {
		super(pContext);

		setPadding(20, 10, 10, 10);
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT
				));

		setAdapter(pAdapter);

		setChoiceMode(CHOICE_MODE_SINGLE);
		setOnItemClickListener(this);
	}

	@Override
	public BaseAdapter getAdapter() {
		return (BaseAdapter)super.getAdapter();
	}

	@Override
	public void onItemClick(final AdapterView<?> pParent, final View pView,
			final int pPosition, final long pID) {
		setItemChecked(pPosition, true);
		showContextMenu();
	}

	public static abstract class ListViewAdapter extends BaseAdapter {

		protected abstract LayoutInflater getLayoutInflater();

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {

			final Settings settings =
				((App) parent.getContext().getApplicationContext())
				.getSettings();

			ViewHolder holder;
			if (convertView == null) {
				convertView = getLayoutInflater()
					.inflate(R.layout.custom_row_view, null);

				holder = new ViewHolder();
				holder.txtName =
						(TextView) convertView.findViewById(R.id.name);
				holder.txtServer =
						(TextView) convertView.findViewById(R.id.server);
				holder.txtState =
						(TextView) convertView.findViewById(R.id.state);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final GameInfo item = (GameInfo) getItem(position);

			holder.txtName.setText(item.getGame().getName());

			if(item.isServer(settings.getPlayer()))
			{
				holder.txtServer.setText("");
			} else {
				holder.txtServer.setText(item.getServer().getName());
			}

			holder.txtState.setText(
				parent.getContext().getString(R.string.state) + ": "
				+ item.getState().getName(parent.getContext()));

			return convertView;
		}

		class ViewHolder {
			TextView txtName;
			TextView txtServer;
			TextView txtState;
		}
	}
}
