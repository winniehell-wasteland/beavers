package org.beavers.ingame;

import java.util.ArrayDeque;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.Direction;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;

/**
 * soldier sprite
 *
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Soldier extends AnimatedSprite implements IGameObject, IMovableObject {

	/**
	 * default constructor
	 * @param pTeam team this soldier is in
	 * @param pInitialPosition initial position
	 */
	public Soldier(final int pTeam, final Tile pInitialPosition) {
		this(pTeam, pInitialPosition, getTexture(pTeam));
	}

	public Shot getShot(){
		return shot;
	}

	public Line getLineA(){
		return lineA;
	}
	public Line getLineB(){
		return lineB;
	}
	public float[] getCenter(){
		return this.convertLocalToSceneCoordinates(getWidth()/2, getHeight()/2);
	}

	/**
	 * adds a waypoint for this soldier
	 * @param pWayPoint waypoint to add
	 */
	public WayPoint addWayPoint(final IPathFinder<IMovableObject> pPathFinder,
	                            final Tile pTile)
	{
		final Path path = findPath(pPathFinder, pTile);

		// there is no path
		if(path == null)
		{
			return null;
		}

		final WayPoint waypoint = new WayPoint(this, path, pTile);
		return addWayPoint(waypoint);
	}

	public WayPoint addWayPoint(final WayPoint waypoint) {
		waypoints.addLast(waypoint);

		lastWaypoint.setNext(waypoint);
		waypoint.setPrevious(lastWaypoint);

		lastWaypoint = waypoint;

		return waypoint;
	}

	/**
	 * increase HP by offset (can be negative)
	 * @param pOffset increment
	 * @return new HP
	 */
	public int changeHP(final int pOffset){
		hp += pOffset;

		if(hp>100)hp=100;
		if(hp<0){
			hp=0;
			dead=true;
		}

		return hp;

	}

	/**
	 * turn soldier to face target tile
	 * @param pTarget target tile
	 * @param pListener listener for rotation
	 */
	public void faceTarget(final Tile pTarget, final IModifierListener<IEntity> pListener){
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

			rotation.addModifierListener(pListener);
			registerEntityModifier(rotation);
		}
	}

	/**
	 * @param pPathFinder path finder to use
	 * @param pTarget target position
	 * @return a path from last waypoint to the target position (or null if there is none)
	 */
	@Override
	public Path findPath(final IPathFinder<IMovableObject> pPathFinder, final Tile pTarget) {
		return pPathFinder.findPath(this, 0,
			lastWaypoint.getTile().getColumn(), lastWaypoint.getTile().getRow(),
			pTarget.getColumn(), pTarget.getRow());
	}

	/**
	 * fire shot to target tile
	 * @param pShot shot object
	 * @param pTarget target soldier
	 */
	public void fireShot(final Soldier target, final GameActivity pActivity){
		final Tile pTarget=target.getTile();
		final Shot tmpshot = new Shot(this, pActivity);
		if(tmpshot.findPath(pActivity.getPathFinder(), pTarget)==null)return;
		if(shot!=null){
			shot.stopShooting();
		}
		shot = tmpshot;
		stop();

		faceTarget(pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				shot.fire(target);
				shooting=true;
			}
		});
	}

	/**
	 * @return first way point assigned to this soldier
	 */
	public WayPoint getFirstWaypoint()
	{
		return firstWaypoint;
	}

	/**
	 * @return health percentage
	 */
	public int getHP(){
		return hp;
	}
	/**
	 * @return last way point assigned to this soldier
	 */
	public WayPoint getLastWaypoint() {
		return lastWaypoint;
	}

	/**
	 * @param pMap tile map to walk on
	 * @param pFrom start position
	 * @param pTo end position
	 * @return step cost for soldier and given tiles
	 */
	@Override
	public float getStepCost(final ITiledMap<IMovableObject> pMap, final Tile pFrom, final Tile pTo) {
		final Direction direction = pFrom.getDirectionTo(pTo);

		// prevent diagonals at blocked tiles
		if(!direction.isHorizontal() && !direction.isVertical())
		{
			if(pMap.isTileBlocked(this, pFrom.getColumn(), pTo.getRow())
					|| pMap.isTileBlocked(this, pTo.getColumn(), pFrom.getRow()))
			{
				return Integer.MAX_VALUE;
			}
		}

		return 0;
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
	public Tile getTile() {
		return Tile.fromCoordinates(getCenter()[0], getCenter()[1]);
	}

	public ArrayDeque<WayPoint> getWaypoints() {
		return waypoints;
	}

	/**
	 * TODO not yet used
	 */
	public Weapon getWeapon()
	{
		return null;
	}

	
	


	
	public boolean isdead(){
		return dead;
	}
	
	
	public int getAP(){
		return ap;
	}
	
	public int getmaxAP(){
		return maxAP;
	}
	
	public int changeAP(final int points){
		ap+=points;
		if(ap>maxAP)ap=maxAP;
		else if(ap<0){
			ap=0;
		}
		return ap;
		
		
	}
	public boolean isShooting(){
		return shooting;
	}
	
	public void setShooting(final boolean shoot){
		shooting=shoot;
	}

	/**
	 * add selection mark and draw waypoints
	 */
	public void markSelected(){
		attachChild(selectionMark);

		assert getParent() instanceof Scene;
		final Scene scene = (Scene) getParent();

		WayPoint waypoint = firstWaypoint;

		while(waypoint != null)
		{
			scene.attachChild(waypoint);
			waypoint = waypoint.getNext();
		}
	}

	/**
	 * remove selection mark and waypoints from GameScene
	 */
	public void markDeselected(){
		detachChild(selectionMark);

		WayPoint waypoint = firstWaypoint;

		while(waypoint != null)
		{
			waypoint.detachSelf();
			waypoint = waypoint.getNext();
		}
	}
	


	/**
	 * move to target tile
	 * @param pTarget target tile
	 * @param pListener listener for movement
	 */
	public void move(final Tile pTarget, final Tile pAim, final IModifierListener<IEntity> pListener) {
		final float distx =
			Math.abs(pTarget.getCenterX() - (getX()+getWidth()/2));
		final float disty =
			Math.abs(pTarget.getCenterY() - (getY()+getHeight()/2));

		final float duartion = (float) (Math.sqrt(distx*distx+disty*disty)/WALKING_SPEED);

		final MoveModifier movement =
			new MoveModifier(duartion,
				getX(), pTarget.getCenterX() - getWidth()/2,
				getY(), pTarget.getCenterY() - getHeight()/2);

		movement.addModifierListener(pListener);

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
					final float angle =
						calcViewAngle(pTarget.getCenterX(), pTarget.getCenterY(), pAim);
					final RotationByModifier rotation = new RotationByModifier(movement.getDuration(), angle);
					Soldier.this.registerEntityModifier(rotation);
				}
			}
		});
	}

	/**
	 * remove first waypoint
	 * @return new first waypoint
	 */
	public WayPoint popWayPoint() {
		if(firstWaypoint != lastWaypoint)
		{
			firstWaypoint.getNext().setPrevious(null);
			firstWaypoint.detachSelf();
			firstWaypoint = firstWaypoint.getNext();

			waypoints.removeFirst();

			return firstWaypoint;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void setRemoveObjectListener(final IRemoveObjectListener pListener) {
		// TODO Auto-generated method stub

	}

	/**
	 * remove last waypoint
	 */
	public void removeLastWayPoint()
	{
		if(firstWaypoint != lastWaypoint)
		{
			

			lastWaypoint.detachSelf();
			lastWaypoint.getPrevious().setNext(null);
			lastWaypoint = lastWaypoint.getPrevious();

			waypoints.removeLast();
		}
	}

	/**
	 * stop moving or turning
	 */
	private MoveModifier lastMove;
	private RotationByModifier lastRotate;
	
	public void stop()
	{
		stopAnimation(0);
		unregisterEntityModifiers(new IEntityModifierMatcher() {

			@Override
			public boolean matches(final IModifier<IEntity> pObject) {
				if(pObject instanceof MoveModifier){
					lastMove=(MoveModifier)pObject;
					return true;
				}
				if(pObject instanceof RotationByModifier){
					lastRotate=(RotationByModifier)pObject;
					return true;
				}
				

				return false;
			}

		});
	}

	public void resume(){
		if(lastMove!=null){
			registerEntityModifier(lastMove);
		}
		if(lastRotate!=null){
			registerEntityModifier(lastRotate);
		}
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
		
	private final int maxAP=20;
	private int ap=maxAP;
	private int hp=100;
	private boolean dead=false;
	private boolean shooting=false;

	/** token to mark the selected soldier */
	private final Sprite selectionMark;

	private WayPoint firstWaypoint;
	private WayPoint lastWaypoint;

	/** team the soldier belongs to */
	private final int team;

	/** waypoints of the soldier */
	private final ArrayDeque<WayPoint> waypoints;

	private Shot shot;
	private final Line lineA,lineB;

		/**
		 * automatically center soldier on tile using the texture region
		 * @see Soldier#Soldier(int, Tile)
		 * @param pTiledTextureRegion
		 */
		private Soldier(final int pTeam, final Tile pTile,
		                final TiledTextureRegion pTiledTextureRegion) {
			super(pTile.getCenterX() - pTiledTextureRegion.getTileWidth()/2,
			      pTile.getCenterY() - pTiledTextureRegion.getTileHeight()/2,
			      pTiledTextureRegion);
			
			team=pTeam;
			//Selection Circle
			final TextureRegion selectionTexture =
				Textures.SOLDIER_SELECTION_CIRCLE.deepCopy();
			selectionMark = new Sprite(
				getWidth()/2 - selectionTexture.getWidth()/2,
				getHeight()/2 - selectionTexture.getHeight()/2 + 5,
				selectionTexture);

			waypoints = new ArrayDeque<WayPoint>();

			firstWaypoint = new WayPoint(this, null, pTile);
			lastWaypoint = firstWaypoint;

			waypoints.add(firstWaypoint);

			stopAnimation(0);
			setRotationCenter(getWidth()/2, getHeight()/2);

			setZIndex(GameActivity.ZINDEX_SOLDIERS);


			lineA= new Line(getWidth()/2,getHeight()/2, -160,-400 );
			lineB= new Line(getWidth()/2,getHeight()/2,160,-400);
			//this.attachChild(lineA);
			//this.attachChild(lineB);


			//cone.setVisible(false);
		}

	private float calcViewAngle(final float pCurrentX, final float pCurrentY, final Tile pTarget) {
		final float angleX = pTarget.getCenterX() - pCurrentX;
		final float angleY = pTarget.getCenterY() - pCurrentY;
		float angle = (float) Math.toDegrees(Math.atan2(angleY,angleX))+90;

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
		case 1:
			return Textures.SOLDIER_TEAM1.deepCopy();
		default: return null;
		}
	}
}
