package org.beavers.ingame;

import java.util.ArrayDeque;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
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

		//Selection Circle
		selectionMark = new Sprite(0, 0, Textures.SOLDIER_SELECTION_CIRCLE.deepCopy());
		selectionMark.setPosition((getWidth()-selectionMark.getWidth())/2, (getHeight()-selectionMark.getHeight())/2+5);

		team = pTeam;

		wayPoints = new ArrayDeque<WayPoint>();
		wayPoints.push(new WayPoint(this, null, pInitialPosition));

		stopAnimation(0);
		setRotationCenter(getWidth()/2, getHeight()/2);
		setZIndex(GameScene.ZINDEX_SOLDIERS);
	}

	/**
	 * adds a waypoint for this soldier
	 * @param pWayPoint waypoint to add
	 */
	public void addWayPoint(final WayPoint pWayPoint)
	{
		wayPoints.getLast().isLast = false;
		wayPoints.addLast(pWayPoint);
	}

	/**
	 * turn soldier to face target tile
	 * @param pTarget target tile
	 * @param pListener listener for rotation
	 */
	public void faceTarget(final TMXTile pTarget, final IModifierListener<IEntity> pListener){
		final float angle = calcViewAngle((getX()+getWidth()/2), (getY()+getHeight()/2), pTarget);

		if(angle == 0)
		{
			if(pListener != null)
			{
				pListener.onModifierFinished(null, this);
			}
		}
		else
		{
			final RotationByModifier rotation = new RotationByModifier(Math.abs(angle)/ROTATION_SPEED, angle);

			if(pListener != null)
			{
				rotation.addModifierListener(pListener);
			}

			registerEntityModifier(rotation);
		}
	}

	/**
	 * @param pPathFinder path finder to use
	 * @param pTarget target position
	 * @return a path from last waypoint to the target position (or null if there is none)
	 */
	@Override
	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
		return wayPoints.getLast().findPath(pPathFinder, pTarget);
	}

	/**
	 * fire shot to target tile
	 * @param pShot shot object
	 * @param pTarget target tile
	 */
	public void fireShot(final Shot pShot, final TMXTile pTarget){
		faceTarget(pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				pShot.fire(pTarget);
			}
		});
	}

	/**
	 * TODO not yet used
	 */
	public int getHealthPercentage()
	{
		return 1/0;
	}

	/**
	 * @param pMap tile map to walk on
	 * @param pFrom start position
	 * @param pTo end position
	 * @return step cost for soldier and given tiles
	 */
	@Override
	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo) {
		return wayPoints.getLast().getStepCost(pMap, pFrom, pTo);
	}

	/**
	 * @return team the soldier belongs to
	 */
	public int getTeam() {
		return team;
	}

	/**
	 * @return current soldier position
	 */
	@Override
	public TMXTile getTile() {
		return wayPoints.getFirst().getTile();
	}

	/**
	 * TODO not yet used
	 */
	public Weapon getWeapon()
	{
		return null;
	}

	private int hp=100;
	public int changeHP(final int health){
		hp+=health;
		if(hp>100)hp=100;
		if(hp<0)hp=0;
		return hp;
	}

	public int getHP(){
		return hp;
	}

	/**
	 * add selection mark and draw waypoints
	 */
	public void markSelected(){
		attachChild(selectionMark);

		assert getParent() instanceof GameScene;
		final GameScene gameScene = (GameScene) getParent();

	    for(final WayPoint waypoint : wayPoints)
		{
	    	if(waypoint == wayPoints.getFirst())
	    	{
	    		continue;
	    	}

			gameScene.attachChild(waypoint);
		}
	}

	/**
	 * remove selection mark and waypoints from GameScene
	 */
	public void markDeselected(){
		detachChild(selectionMark);

	    for(final WayPoint waypoint : wayPoints)
		{
	    	if(waypoint == wayPoints.getFirst())
	    	{
	    		continue;
	    	}

			waypoint.detachSelf();
		}
	}

	/**
	 * move to target tile
	 * @param pTarget target tile
	 */
	public void move(final TMXTile pTarget) {
		move(pTarget,null, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				Soldier.this.stopAnimation(0);
			}
		});
	}

	/**
	 * move to target tile
	 * @param pTarget target tile
	 * @param pListener listener for movement
	 */
	public void move(final TMXTile pTarget, final TMXTile pAim, final IModifierListener<IEntity> pListener) {
		final float distx = Math.abs(GameScene.getTileCenterX(pTarget) - (getX()+getWidth()/2));
		final float disty = Math.abs(GameScene.getTileCenterY(pTarget) - (getY()+getHeight()/2));

		final MoveModifier movement = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/WALKING_SPEED), getX(),
				GameScene.getTileCenterX(pTarget)-getWidth()/2, getY(), GameScene.getTileCenterY(pTarget)-getHeight()/2);

		if(pListener != null)
		{
			movement.addModifierListener(pListener);
		}

		faceTarget(pAim != null?pAim:pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier,
					final IEntity pItem) {

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier,
					final IEntity pItem) {
				Soldier.this.registerEntityModifier(movement);
				Soldier.this.animate(new long[]{200, 200}, 1, 2, true);

				if(pAim != null)
				{
					final float angle = calcViewAngle(GameScene.getTileCenterX(pTarget), GameScene.getTileCenterY(pTarget), pAim);
					final RotationByModifier rotation = new RotationByModifier(movement.getDuration(), angle);
					Soldier.this.registerEntityModifier(rotation);
				}
			}
		});
	}

	/**
	 * remove first waypoint
	 * @return removed waypoint
	 */
	public WayPoint popWayPoint() {
		if(wayPoints.size() > 1)
		{
			wayPoints.removeFirst();
			return wayPoints.getFirst();
		}
		else
		{
			return null;
		}
	}

	/**
	 * remove last waypoint
	 */
	public void removeWayPoint()
	{
		if(wayPoints.size() > 1)
		{
			wayPoints.removeLast();
			wayPoints.getLast().isLast = true;
		}
	}

	/**
	 * stop moving or turning
	 */
	public void stop()
	{
		stopAnimation(0);
		unregisterEntityModifiers(new IEntityModifierMatcher() {

			@Override
			public boolean matches(final IModifier<IEntity> pObject) {
				if((pObject instanceof MoveModifier)
						|| (pObject instanceof RotationByModifier))
				{
					return true;
				}

				return false;
			}

		});
	}

	/**
	 * @name speed constants
	 * @{
	 */
	/** speed for movement in pixel per second */
	private static final int WALKING_SPEED = 80;
	/** speed for rotation in degree per second */
	private static final int ROTATION_SPEED = 240;
	/**
	 * @}
	 */

	/** token to mark the selected soldier */
	private final Sprite selectionMark;

	/** team the soldier belongs to */
	private final int team;

	/** assigned waypoints */
	private final ArrayDeque<WayPoint> wayPoints;

	private float calcViewAngle(final float pCurrentX, final float pCurrentY, final TMXTile pTarget) {
		final float angleX = GameScene.getTileCenterX(pTarget)-pCurrentX;
		final float angleY = GameScene.getTileCenterY(pTarget)-pCurrentY;
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;


		if((angle-getRotation())>180)angle=(angle-getRotation())-360;
		else if((angle-getRotation())<-180)angle=360+(angle-getRotation());
		else angle=angle-getRotation();

		return angle;
	}

	/**
	 * @return TextureRegion for given team
	 */
	private static TiledTextureRegion getTexture(final int pTeam) {
		switch (pTeam) {
		case 0:
			return Textures.SOLDIER_TEAM0.deepCopy();
		default:
			return null;
		}
	}
}
