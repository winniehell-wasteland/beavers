package org.beavers.ui;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

public class MenuItem extends Rectangle implements IMenuItem {

	private final int ID;
	private final Text text;

	public MenuItem(final int pID, final float pWidth, final Font pFont, final String pText) {
		super(0, 0, pWidth, pFont.getLineHeight());
		 
		this.ID = pID;
				
		this.text = new Text(pWidth/2 - pFont.getStringWidth(pText)/2, 0, pFont, pText);				
		attachChild(text);
	}

	@Override
	public int getID() {
		return this.ID;
	}

	@Override
	public void onSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnselected() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void reset() {
		
		super.reset();
	}
}
