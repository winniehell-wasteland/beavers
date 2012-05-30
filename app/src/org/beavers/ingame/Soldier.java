package org.beavers.ingame;

import java.util.ArrayList;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.beavers.AppActivity;


public class Soldier extends AnimatedSprite implements GameObject {

	public Soldier(final int pTeam, final TMXTile pTile) {
		super(pTile.getTileX() + pTile.getTileWidth()/2,
				pTile.getTileY() + pTile.getTileHeight()/2,
				getTexture(pTeam));

		setPosition(getX() - getWidth()/2, getY() - getHeight()/2);

		tile = pTile;
		target = pTile;

		wayPoints = new ArrayList<WayPoint>();

		//selectionMark = new Rectangle(getWidth()/2, getHeight()/2, 10, 10);
		//selectionMark.setColor(1,0,0);

		final AppActivity pApp = null;

		//Selection Circle
		final BitmapTextureAtlas circleTextureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		final TextureRegion circleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(circleTextureAtlas, pApp, "circle.png", 0, 0);
		pApp.getEngine().getTextureManager().loadTexture(circleTextureAtlas);
		selectionMark = new Sprite(0, 0, circleTextureRegion);
		selectionMark.setPosition((getWidth()-selectionMark.getWidth())/2, (getHeight()-selectionMark.getHeight())/2+5);

		stopAnimation();
		setRotationCenter(getWidth()/2, getHeight()/2);
		setZIndex(1);
	}

	@Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
            // TODO Auto-generated method stub
            super.onManagedUpdate(pSecondsElapsed);
            if(((int)getX()+getWidth()/2<=getTargetX() && (int)getX()+getWidth()/2>=getTargetX())
            		&&((int)getY()+getHeight()/2<=getTargetY() && (int)getY()+getHeight()/2>=getTargetY())){
            	stopAnimation();
            	// TODO was macht das?
            	setCurrentTileIndex(0);
            }

    }

	public void markSelected(){
		attachChild(selectionMark);
	}

	public void markDeselected(){
		detachChild(selectionMark);
	}

	public void move(final TMXTile pTarget){
		target = pTarget;

		//Bewegung nach pTarget
		final float distx = Math.abs(getTargetX() - (getX()+getWidth()/2));
		final float disty = Math.abs(getTargetY() - (getY()+getHeight()/2));

		mod = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/SPEED),getX(),
				getTargetX()-getWidth()/2, getY(),getTargetY()-getHeight()/2);
		registerEntityModifier(mod);

		//Rotation
		final float angleX = getTargetX() - (getX()+getWidth()/2);
		final float angleY = getTargetY() - (getY()+getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
		RotationByModifier rotate;

		if((angle-getRotation())>180)angle=(angle-getRotation())-360;
		else if((angle-getRotation())<-180)angle=360+(angle-getRotation());
		else angle=angle-getRotation();

		rotate= new RotationByModifier(0.2f, angle);

		registerEntityModifier(rotate);

		animate(new long[]{200, 200}, 1, 2, true);
	}

	public void faceTarget(final float faceX,final float faceY){
		final float angleX=faceX-(getX()+getWidth()/2);
		final float angleY=faceY-(getY()+getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;


		if((angle-getRotation())>180)angle=(angle-getRotation())-360;
		else if((angle-getRotation())<-180)angle=360+(angle-getRotation());
		else angle=angle-getRotation();

		final RotationByModifier rotateView= new RotationByModifier(0.6f, angle);

		registerEntityModifier(rotateView);
	}

	public void shootAt(final float centerX, final float centerY){
		faceTarget(centerX, centerY);

		//new Shot(this, pApp,gscene,sprite.getX()+sprite.getWidth()/2,sprite.getY()+sprite.getHeight()/2,centerX,centerY);
	}

	public int getHealthPercentage()
	{
		return -1;
	}

	public TMXTile getTile() {
		return tile;
	}

	public Weapon getWeapon()
	{
		return null;
	}

	public void stop()
	{
		stopAnimation();

		if(mod != null)
		{
			unregisterEntityModifier(mod);
		}
	}

	private static final int SPEED = 80;

	private final TMXTile tile;
	private TMXTile target;

	private final ArrayList<WayPoint> wayPoints;

	private MoveModifier mod;
	private final Sprite selectionMark;

	private static TiledTextureRegion getTexture(final int pTeam) {
		switch (pTeam) {
		case 0:
			return Textures.SOLDIER_TEAM0.deepCopy();
		default:
			return null;
		}
	}

	private int getTargetX() {
		return target.getTileX() + target.getTileWidth()/2;
	}

	private int getTargetY() {
		return target.getTileY() + target.getTileHeight()/2;
	}

	// TODO unused?
	private float[] getSceneCoordinates(){
		return convertLocalToSceneCoordinates(10, 10); //10x10 TMX Map
	}

	// TODO unused?
	private float[] getXY(){

		final float[] pos= new float[2];
		pos[0]=getX()+getWidth()/2;
		pos[1]=getY()+getHeight()/2;
		return pos;
	}
}
