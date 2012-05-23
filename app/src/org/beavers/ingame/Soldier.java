package org.beavers.ingame;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.modifier.MoveYModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.beavers.AppActivity;

import android.content.Context;

public class Soldier extends GameObject {
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion playerTextureRegion;
	private AnimatedSprite player;
	private int speed=80;
	
	public Soldier(final AppActivity pApp,int team, int x, int y){
		
		if(team==0){
			mBitmapTextureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, pApp, "96x96anim.png", 0, 0, 3, 2);
			pApp.getEngine().getTextureManager().loadTexture(mBitmapTextureAtlas);
		}
		else{}
	}
	
	public void createSoldier(Scene s,int x, int y){
		player=new AnimatedSprite(x,y,playerTextureRegion);
		player.stopAnimation();
		
		s.attachChild(player);
	}
	
	public AnimatedSprite getSprite(){
		return player;
	}
	
	public float[] getSceneCoordinates(){
		return player.convertLocalToSceneCoordinates(10, 10); //10x10 TMX Map
	}
	
	
	public void move(int x, int y){
		//Bewegung nach x,y
		float distx=Math.abs(x-player.getX());
		float disty=Math.abs(y-player.getY());
		MoveModifier mod = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/speed),player.getX(),x, player.getY(),y);
		player.registerEntityModifier(mod);
		
		//Rotation
		float angleX=x-player.getX();
		float angleY=y-player.getY();
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
		RotationByModifier rotate= new RotationByModifier(0.2f, angle);
		player.registerEntityModifier(rotate);
		
		player.animate(new long[]{200, 200}, 1, 2, true);
		
		
	
	}
	
	public int getHealthPercentage()
	{
		return -1;
	}
	
	public Weapon getWeapon()
	{
		return null;
	}
	
	public ViewDirection getViewDirection()
	{
		return null;
	}
	
	public Action getAction(int index)
	{
		return null;
	}

	@Override
	public String getType() {
		return this.getClass().getSimpleName();
	}
	
	
}
