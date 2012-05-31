package org.beavers.ingame;

import java.util.Stack;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;
import org.beavers.gameplay.GameScene;


public class Soldier extends AnimatedSprite implements GameObject {

	public Soldier(final int pTeam, final TMXTile pInitialPosition) {
		super(GameScene.getTileCenterX(pInitialPosition),
				GameScene.getTileCenterY(pInitialPosition),
				getTexture(pTeam));

		setPosition(getX() - getWidth()/2, getY() - getHeight()/2);

		target = pInitialPosition;

		wayPoints = new Stack<WayPoint>();
		wayPoints.push(new WayPoint(null, pInitialPosition));

		//Selection Circle
		selectionMark = new Sprite(0, 0, Textures.SOLDIER_SELECTION_CIRCLE.deepCopy());
		selectionMark.setPosition((getWidth()-selectionMark.getWidth())/2, (getHeight()-selectionMark.getHeight())/2+5);

		stopAnimation();
		setRotationCenter(getWidth()/2, getHeight()/2);
		setZIndex(10);
	}

	public void addWayPoint(final WayPoint pWayPoint)
	{
		wayPoints.peek().isLast = false;
		wayPoints.push(pWayPoint);
	}

	@Override
	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
		return wayPoints.peek().findPath(pPathFinder, pTarget);
	}

	@Override
	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo) {
		return wayPoints.peek().getStepCost(pMap, pFrom, pTo);
	}

	@Override
	public TMXTile getTile() {
		return wayPoints.get(0).getTile();
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

		for(int i = 1; i < wayPoints.size(); ++i)
		{
			wayPoints.get(i).detachChildren();
			wayPoints.get(i).detachSelf();
		}
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
	// rotation== true: Rotation wird direkt auf Soldier ausgeführt
	//rotation == false: Es wird nur ein RotationByModifier zurückgegeben
	public RotationByModifier faceTarget(final float faceX,final float faceY, final boolean rotation){
		final float angleX=faceX-(getX()+getWidth()/2);
		final float angleY=faceY-(getY()+getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;


		if((angle-getRotation())>180)angle=(angle-getRotation())-360;
		else if((angle-getRotation())<-180)angle=360+(angle-getRotation());
		else angle=angle-getRotation();

		final RotationByModifier rotate=new RotationByModifier(0.6f, angle);
		if(rotation){
			registerEntityModifier(rotate);
		}

		return rotate;


	}

	public void fireShot(final Shot pShot, final TMXTile pTarget){

		final RotationByModifier rot=faceTarget(GameScene.getTileCenterX(pTarget), GameScene.getTileCenterY(pTarget),false);

		rot.addModifierListener(new IModifierListener<IEntity>() {

			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				pShot.fire(pTarget);
			}
		});
		registerEntityModifier(rot);

	}

	public int getHealthPercentage()
	{
		return -1;
	}

	public Weapon getWeapon()
	{
		return null;
	}

	public void removeWayPoint()
	{
		if(wayPoints.size() > 1)
		{
			wayPoints.pop();
			wayPoints.peek().isLast = true;
		}
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

	private TMXTile target;

	private final Stack<WayPoint> wayPoints;

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
