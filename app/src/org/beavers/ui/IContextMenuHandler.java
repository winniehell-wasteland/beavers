package org.beavers.ui;

import org.beavers.gameplay.GameActivity;

import android.view.ContextMenu;
import android.view.MenuItem;

public interface IContextMenuHandler  {
	int getMenuID();
	void onMenuCreated(final ContextMenu pMenu);
	boolean onMenuItemClick(final GameActivity pActivity, final MenuItem pItem);
}
