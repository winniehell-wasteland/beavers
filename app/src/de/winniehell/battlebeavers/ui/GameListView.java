/*
	(c) winniehell (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.ui;

import de.winniehell.battlebeavers.gameplay.Game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
/**
 * list view for GameList
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
@SuppressLint("ViewConstructor")
public abstract class GameListView extends ListView
	implements OnItemClickListener, OnMenuItemClickListener {

	public GameListView(final Context pContext) {
		super(pContext);

		setPadding(20, 10, 10, 10);
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT
				));

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
	public Game getSelectedItem() {
		return (Game) getItemAtPosition(selectedItem);
	}

	@Override
	public long getSelectedItemId() {
		return getItemIdAtPosition(selectedItem);
	}

	@Override
	public int getSelectedItemPosition() {
		return selectedItem;
	}

	@Override
	public void setSelection(final int pPosition) {
		selectedItem = pPosition;
	}

	@Override
	protected void onCreateContextMenu(final ContextMenu pMenu) {
		final Game game = getSelectedItem();

		if(game == null)
		{
			pMenu.clear();
			return;
		}

		onPrepareContextMenu(new MenuInflater(getContext()), pMenu, game);

        for(int i = 0; i < pMenu.size(); ++i)
        {
        	pMenu.getItem(i).setOnMenuItemClickListener(this);
        }
	}

	@Override
	public void onItemClick(final AdapterView<?> pParent, final View pView,
	                        final int pPosition, final long pID) {
		setSelection(pPosition);
		showContextMenu();
	}

	@Override
	public void setAdapter(final ListAdapter pAdapter) {
		if(pAdapter instanceof GameListAdapter) {
			super.setAdapter(pAdapter);
		}
		else {
			throw new IllegalArgumentException(
				"Adapter has to be of type "
				+ GameListAdapter.class.getName() + "!"
			);
		}
	}

	abstract protected void onPrepareContextMenu(
		MenuInflater pInflater, ContextMenu pMenu, Game pGame
	);

	private int selectedItem = -1;
}
