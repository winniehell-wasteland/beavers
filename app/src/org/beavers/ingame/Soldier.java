package org.beavers.ingame;

import java.util.Stack;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.IEntityModifier;
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

/**
 * soldier sprite
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Soldier extends AnimatedSprite implements GameObject {

	/**
	 * default constructor
	 * @param pTeam team this soldier is in
	 * @param pInitialPosition initial position
	 */
	public Soldier(final int pTeam, final TMXTile pInitialPosition) {
		super(GameScene.getTileCenterX(pInitialPosition),
				GameScene.getTileCenterY(pInitialPosition),
				getTexture(pTeam));

		setPosition(getX() - getWidth()/2, getY() - getHeight()/2);

		target = pInitialPosition;

		wayPoints = new Stack<WayPoint>();
		wayPoints.push(new WayPoint(this, null, pInitialPosition));

		//Selection Circle
		selectionMark = new Sprite(0, 0, Textures.SOLDIER_SELECTION_CIRCLE.deepCopy());
		selectionMark.setPosition((getWidth()-selectionMark.getWidth())/2, (getHeight()-selectionMark.getHeight())/2+5);

		stopAnimation();
		setRotationCenter(getWidth()/2, getHeight()/2);
		setZIndex(10);
	}

	/**
	 * adds a waypoint for this soldier
	 * @param pWayPoint waypoint to add
	 */
	public void addWayPoint(final WayPoint pWayPoint)
	{
		wayPoints.peek().isLast = false;
		wayPoints.push(pWayPoint);
	}

	/**
	 * draws the waypoints and paths in between to the scene
	 * @param pGameScene scene to draw on
	 */
	public void drawWaypoints(final GameScene pGameScene) {
		for(int i = 1; i < wayPoints.size(); ++i)
		{
			final WayPoint waypoint = wayPoints.get(i);

			pGameScene.drawPath(waypoint.getPath(), waypoint);
			pGameScene.attachChild(waypoint);
		}
	}

	/**
	 * @param pPathFinder path finder to use
	 * @param pTarget target position
	 * @return a path from last waypoint to the target position (or null if there is none)
	 */
	@Override
	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
		return wayPoints.peek().findPath(pPathFinder, pTarget);
	}

	/**
	 * @param pMap tile map to walk on
	 * @param pFrom start position
	 * @param pTo end position
	 * @return step cost for soldier and given tiles
	 */
	@Override
	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo) {
		return wayPoints.peek().getStepCost(pMap, pFrom, pTo);
	}

	/**
	 * @return current soldier position
	 */
	@Override
	public TMXTile getTile() {
		return wayPoints.get(0).getTile();
	}

	public WayPoint getWayPoint(final int pIndex) {
		if(pIndex < wayPoints.size())
		{
			return wayPoints.get(pIndex);
		}
		else
		{
			return null;
		}
	}

	@Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
            // TODO Auto-generated method stub
            super.onManagedUpdate(pSecondsElapsed);
            if(((int)getX()+getWidth()/2<=GameScene.getTileCenterX(target) && (int)getX()+getWidth()/2>=GameScene.getTileCenterX(target))
            		&&((int)getY()+getHeight()/2<=GameScene.getTileCenterY(target) && (int)getY()+getHeight()/2>=GameScene.getTileCenterY(target))){
            	stopAnimation();
            	// TODO was macht das?
            	setCurrentTileIndex(0);
            }

    }

	/**
	 * add selectionMark
	 */
	public void markSelected(){
		attachChild(selectionMark);
	}

	/**
	 * remove selectionMark and waypoints from GameScene
	 */
	public void markDeselected(){
		detachChild(selectionMark);

		for(int i = 1; i < wayPoints.size(); ++i)
		{
			wayPoints.get(i).detachChildren();
			wayPoints.get(i).detachSelf();
		}
	}

	public void move(final TMXTile pTarget) {
		move(pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				Soldier.this.stopAnimation();
			}
		});
	}

	public void move(final TMXTile pTarget, final IModifierListener<IEntity> pListener) {
		target = pTarget;

		//Bewegung nach pTarget
		final float distx = Math.abs(GameScene.getTileCenterX(target) - (getX()+getWidth()/2));
		final float disty = Math.abs(GameScene.getTileCenterY(target) - (getY()+getHeight()/2));

		final MoveModifier movement = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/SPEED), getX(),
				GameScene.getTileCenterX(target)-getWidth()/2, getY(), GameScene.getTileCenterY(target)-getHeight()/2);

		if(pListener != null)
		{
			movement.addModifierListener(pListener);
		}

		faceTarget(pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier,
					final IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier,
					final IEntity pItem) {
				lastModifier = movement;

				Soldier.this.registerEntityModifier(movement);
				animate(new long[]{200, 200}, 1, 2, true);
			}
		});
	}

	public void faceTarget(final TMXTile pTarget, final IModifierListener<IEntity> pListener){
		final float angleX = GameScene.getTileCenterX(pTarget)-(getX()+getWidth()/2);
		final float angleY = GameScene.getTileCenterY(pTarget)-(getY()+getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;


		if((angle-getRotation())>180)angle=(angle-getRotation())-360;
		else if((angle-getRotation())<-180)angle=360+(angle-getRotation());
		else angle=angle-getRotation();

		if(angle == 0)
		{
			if(pListener != null)
			{
				pListener.onModifierFinished(null, this);
			}
		}
		else
		{
			final RotationByModifier rotation = new RotationByModifier(0.6f, angle);

			if(pListener != null)
			{
				rotation.addModifierListener(pListener);
			}

			lastModifier = rotation;
			registerEntityModifier(rotation);
		}
	}

	public void fireShot(final Shot pShot, final TMXTile pTarget){
		faceTarget(pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				pShot.fire(pTarget);
			}
		});
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
			final WayPoint waypoint = wayPoints.pop();
			waypoint.detachChildren();
			waypoint.detachSelf();

			wayPoints.peek().isLast = true;
		}
	}

	public void stop()
	{
		stopAnimation();

		if(lastModifier != null)
		{
			unregisterEntityModifier(lastModifier);
			lastModifier = null;
		}
	}

	private static final int SPEED = 80;

	private TMXTile target;

	private final Stack<WayPoint> wayPoints;

	private IEntityModifier lastModifier;
	private final Sprite selectionMark;

	private static TiledTextureRegion getTexture(final int pTeam) {
		switch (pTeam) {
		case 0:
			return Textures.SOLDIER_TEAM0.deepCopy();
		default:
			return null;
		}
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
