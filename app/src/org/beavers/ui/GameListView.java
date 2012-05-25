package org.beavers.ui;

import org.beavers.R;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GameListView extends ListView {

	public GameListView(final Context pContext, final GameList pList) {
		super(pContext);

		list = pList;

		setPadding(20, 10, 10, 10);
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT
				));

		setAdapter(new ListViewAdapter(pContext));
	}

	final private GameList list;


	class ListViewAdapter extends BaseAdapter {

		public ListViewAdapter(final Context pContext) {
			 inflater = LayoutInflater.from(pContext);
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
		public View getView(final int position, View convertView, final ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.custom_row_view, null);
				holder = new ViewHolder();
				holder.txtName = (TextView) convertView.findViewById(R.id.name);
				holder.txtServer = (TextView) convertView.findViewById(R.id.server);
				holder.txtState = (TextView) convertView.findViewById(R.id.state);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final GameInfo item = list.get(position);

			holder.txtName.setText(item.getID().toString());
			holder.txtServer.setText(item.getServer().toString());
			holder.txtState.setText("Unknown...");

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
