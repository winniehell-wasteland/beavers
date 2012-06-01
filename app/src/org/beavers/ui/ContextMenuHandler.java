package org.beavers.ui;

import android.view.ContextMenu;
import android.view.MenuItem.OnMenuItemClickListener;

public interface ContextMenuHandler extends OnMenuItemClickListener {
	int getMenuID();
	void onMenuCreated(final ContextMenu pMenu);
}
