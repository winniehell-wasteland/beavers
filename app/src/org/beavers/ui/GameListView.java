package org.beavers.ui;

import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;

import android.annotation.SuppressLint;
import android.app.Activity;
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

	public GameListView(final Activity pActivity, final GameList pList) {
		super(pActivity);

		activity = pActivity;
		list = pList;

		setPadding(20, 10, 10, 10);
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT
				));

		setAdapter(new ListViewAdapter());

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

	private final Activity activity;
	private final GameList list;

	class ListViewAdapter extends BaseAdapter {

		public ListViewAdapter() {
			 inflater = activity.getLayoutInflater();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(final int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.custom_row_view, null);
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

			final GameInfo item = list.get(position);

			holder.txtName.setText(item.getGame().getName());

			if(item.isServer(Settings.playerID))
			{
				holder.txtServer.setText("");
			} else {
				holder.txtServer.setText(item.getServer().getName());
			}

			holder.txtState.setText(
				activity.getString(R.string.state) + ": "
				+ item.getState().getName(activity));

			return convertView;
		}

		class ViewHolder {
			TextView txtName;
			TextView txtServer;
			TextView txtState;
		}

		private final LayoutInflater inflater;
	}
}
