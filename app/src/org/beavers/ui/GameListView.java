package org.beavers.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
/**
 * list view for GameList
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
@SuppressLint("ViewConstructor")
public abstract class GameListView extends ListView
	implements OnItemClickListener {

	public GameListView(final Context pContext,
	                    final GameListAdapter pAdapter) {
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
	public GameListAdapter getAdapter() {
		if(super.getAdapter() instanceof GameListAdapter)
		{
			return (GameListAdapter)super.getAdapter();
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> pParent, final View pView,
	                        final int pPosition, final long pID) {
		setItemChecked(pPosition, true);
		showContextMenu();
	}
}
