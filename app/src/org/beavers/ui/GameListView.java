package org.beavers.ui;

import org.beavers.AppActivity;
import org.beavers.R;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class GameListView extends ListView implements OnMenuItemClickListener, OnItemClickListener {

	public GameListView(final AppActivity pApp, final GameList pList) {
		super(pApp);

		app = pApp;
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
	public void onItemClick(final AdapterView<?> pParent, final View pView, final int pPosition, final long pID) {
		//setSelection(pPosition);
		setItemChecked(pPosition, true);
		showContextMenu();
	}

	@Override
	protected void onCreateContextMenu(final ContextMenu menu) {
        final MenuInflater inflater = app.getMenuInflater();
        inflater.inflate(getContextMenuRes(), menu);

        for(int i = 0; i < menu.size(); ++i)
        {
        	menu.getItem(i).setOnMenuItemClickListener(this);
        }
	}

	/**
	 * @return the context menu resource ID
	 */
	protected abstract int getContextMenuRes();

	private final AppActivity app;
	private final GameList list;

	class ListViewAdapter extends BaseAdapter {

		public ListViewAdapter() {
			 inflater = app.getLayoutInflater();
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
			holder.txtState.setText(app.getString(R.string.state) + ": " + item.getState().getName(app));

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
