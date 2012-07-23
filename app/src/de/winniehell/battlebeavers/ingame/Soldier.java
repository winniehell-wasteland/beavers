/*
	(c) winniehell, wintermadnezz (2012)

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

package de.winniehell.battlebeavers.ingame;

import java.util.ArrayDeque;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
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
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.IWeightedPathFinder;
import org.anddev.andengine.util.path.NegativeStepCostException;
import org.anddev.andengine.util.path.WeightedPath;

import android.util.Log;
import de.winniehell.battlebeavers.Textures;
import de.winniehell.battlebeavers.gameplay.GameActivity;
import de.winniehell.battlebeavers.storage.CustomGSON;

/**
 * soldier sprite
 *
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Soldier extends AnimatedSprite implements IGameObject, IMovableObject {

	/**
	 * JSON tag
	 */
	public static final String JSON_TAG = "soldier";
	
	/**
	 * default constructor
	 * @param pTeam team this soldier is in
	 * @param pInitialPosition initial position
	 */
	public Soldier(final int pTeam, final Tile pInitialPosition) {
		this(pTeam, pInitialPosition, getTexture(pTeam));
	}

	public Attack getAttack(){
		return attack;
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
	public boolean getSimulation(){
		return simulation;
	}
	/**
	 * adds a waypoint for this soldier
	 * @param pWayPoint waypoint to add
	 */
	public WayPoint addWayPoint(final IWeightedPathFinder<IMovableObject> pPathFinder,
	                            final Tile pTile)
	{
		final WeightedPath path = findPath(pPathFinder, pTile);

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

		return waypoint;
	}

	/**
	 * increase HP by offset (can be negative)
	 * @param pOffset increment
	 * @return new HP
	 */
	public int changeHP(final int pOffset){
		hp += pOffset;
		try{
			if((gameListener != null) && !simulation) {
				gameListener.onHPEvent(System.currentTimeMillis(), this, pOffset);
			}
			
			}catch(final Exception e){
				Log.e(null, "no gameListener");
			}
	
		if(hp>100)hp=100;
		if(hp<=0){
			hp=0;
			die();
		}
		
		Log.d(getClass().getName(), "hp: "+hp);

		return hp;
	}

	private void setIgnoreShots(final boolean pIgnore){
		ignoreShots = pIgnore;
	}

	public boolean isIgnoringShots(){
		return ignoreShots;
	}

	private void die(){
		Log.e(Soldier.class.getName(), "die!!!");

		if(hasParent()){
			positionListener.onObjectRemoved(this);
			detachSelf();

			stopAttacking();

			for(final WayPoint waypoint : waypoints)
			{
				waypoint.remove();
			}

			waypoints.clear();
		}
		
		// let Pathwalker know, that we are dead
		if(walker != null) {
			walker.finish();
		}
	}
	/**
	 * turn soldier to face target tile
	 * @param pTarget target tile
	 * @param pListener listener for rotation
	 */
	private void faceTarget(final Tile pTarget, final IModifierListener<IEntity> pListener){
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
	public WeightedPath findPath(final IWeightedPathFinder<IMovableObject> pPathFinder, final Tile pTarget) {
		try {
			return pPathFinder.findPath(this, getAP(),
			                            getLastWaypoint().getTile().getColumn(),
			                            getLastWaypoint().getTile().getRow(),
			                            pTarget.getColumn(), pTarget.getRow());
		} catch (final NegativeStepCostException e) {
			Log.e(getClass().getName(),"This should not happen!");
			return null;
		}
	}

	/**
	 * attack target soldier
	 * @param pTarget target soldier
	 */
	public synchronized void attack(final Soldier pTarget, final GameActivity pActivity){
		// don't shoot zombies and don't let them shoot back
		if(isDead() || pTarget.isDead()) {
			return;
		}
		
		// can't you see, I'm busy
		if(isAttacking() || isIgnoringShots()) {
			return;
		}
		
		final Attack tmp = Attack.create(pActivity, this, pTarget);
		if(tmp == null) {
			return;
		}
		Log.e("Soldier", "attack!");
		attack = tmp;
		pause();

		if((gameListener != null) && !simulation) {
			gameListener.onShootEvent(System.currentTimeMillis(), this, pTarget);
		}
		
		faceTarget(pTarget.getTile(), new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				if(isAttacking()) {
					attack.fire();
				}
			}
		});
	}

	/**
	 * @return first way point assigned to this soldier
	 */
	public WayPoint getFirstWaypoint()
	{
		return waypoints.getFirst();
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
		return waypoints.getLast();
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
				return Float.POSITIVE_INFINITY;
			}
			else
			{
				return 1.5f;
			}
		}

		return 1.0f;
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

	public float getAP(){
		return ap;
	}
	
	public int getId() {
		return id;
	}

	public float getmaxAP(){
		return maxAP;
	}

	public float changeAP(final float points){
		ap += points;
		if(ap>maxAP) {
			ap=maxAP;
		}
		else if(ap<0){
			ap=0;
		}

		return ap;
	}

	public boolean isDead()
	{
		return (getHP() <= 0);
	}

	public boolean isAttacking(){
		return (attack != null);
	}
	
	public void setId(final int id) {
		this.id = id;
	}

	public void setAim(final Aim pAim) {
		aim = (pAim != null)?pAim.getTile():null;
	}
	
	public void stopAttacking() {
		attack = null;
	}

	/**
	 * add selection mark and draw waypoints
	 */
	public void markSelected(){
		attachChild(selectionMark);
		
		Log.w(getClass().getName(), "selected: "+CustomGSON.getInstance().toJson(this));

		assert getParent() instanceof Scene;
		final Scene scene = (Scene) getParent();

		for(final WayPoint waypoint : waypoints)
		{
			scene.attachChild(waypoint);
		}
	}

	/**
	 * remove selection mark and waypoints from GameScene
	 */
	public void markDeselected(){
		detachChild(selectionMark);

		for(final WayPoint waypoint : waypoints)
		{
			waypoint.detachSelf();
		}
	}



	/**
	 * move to target tile
	 * @param pTarget target tile
	 * @param pListener listener for movement
	 */
	private void move(final Tile pTarget) {
		final float distx =
			Math.abs(pTarget.getCenterX() - (getX()+getWidth()/2));
		final float disty =
			Math.abs(pTarget.getCenterY() - (getY()+getHeight()/2));

		final float duration = (float) (Math.sqrt(distx*distx+disty*disty)/WALKING_SPEED);

		final MoveModifier movement =
			new MoveModifier(duration,
				getX(), pTarget.getCenterX() - getWidth()/2,
				getY(), pTarget.getCenterY() - getHeight()/2);

		movement.addModifierListener(walker);
		
		faceTarget((aim != null)?aim:pTarget, new IModifierListener<IEntity>() {
			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier,
					final IEntity pItem) {

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier,
					final IEntity pItem) {
				Soldier.this.registerEntityModifier(movement);
				Soldier.this.animate(new long[]{200, 200}, 1, 2, true);

				if(aim != null)
				{
					final float angle =
						calcViewAngle(pTarget.getCenterX(), pTarget.getCenterY(), aim);
					final RotationByModifier rotation = new RotationByModifier(movement.getDuration(), angle);
					Soldier.this.registerEntityModifier(rotation);
				}
			}
		});
	}

	@Override
	public void onAttached() {
		super.onAttached();
		getParent().sortChildren();
	}

	/**
	 * remove last waypoint from list
	 * (does not remove it from {@link GameActivity})
	 */
	public void removeLastWayPoint()
	{
		if(!getFirstWaypoint().equals(getLastWaypoint()))
		{
			waypoints.removeLast();
		}
	}
	
	public void setGameEventsListener(final IGameEventsListener eListener){
		gameListener = eListener;
		//Log.e(null, ""+eListener.toString());
	}

	@Override
	public void setPositionListener(final IObjectPositionListener pListener) {
		positionListener = pListener;
	}

	/**
	 * pause movement or rotation
	 */
	private void pause()
	{
		stopAnimation(0);
		unregisterEntityModifiers(new IEntityModifierMatcher() {

			@Override
			public boolean matches(final IModifier<IEntity> pObject) {
				if(pObject.isFinished())return false;
				if(pObject instanceof MoveModifier){
					lastMovement=(MoveModifier)pObject;
					return true;
				}
				if(pObject instanceof RotationByModifier){
					lastRotation=(RotationByModifier)pObject;
					return true;
				}


				return false;
			}

		});
	}

	public void resume(){
		if(lastMovement!=null){
			registerEntityModifier(lastMovement);
		}
		if(lastRotation!=null){
			registerEntityModifier(lastRotation);
		}
	}
	
	public void setSimulation(final boolean simulation) {
		this.simulation = simulation;
	}
	
	public void startWalking(final GameActivity pGameActivity) {
		walker = new PathWalker(pGameActivity);
		walker.start();
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

	private int id;
	private boolean simulation=false;
	private final float maxAP=20;
	private float ap=maxAP;
	private int hp=100;
	private boolean ignoreShots=false;
	/** token to mark the selected soldier */
	private final Sprite selectionMark;

	/** team the soldier belongs to */
	private final int team;

	/** waypoints of the soldier */
	private final ArrayDeque<WayPoint> waypoints;

	private Tile aim;
	private Attack attack;
	private final Line lineA,lineB;

	/**
	 * @name stored modifiers
	 * @{
	 */
	private MoveModifier lastMovement;
	private RotationByModifier lastRotation;
	/**
	 * @}
	 */
	
	/**
	 * @name listeners
	 * @{
	 */
	private IObjectPositionListener positionListener;
	private IGameEventsListener gameListener;
	/**
	 * @}
	 */

	private PathWalker walker;
	
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
			getWidth()/2 - selectionTexture.getWidth()/2-1,
			getHeight()/2 - selectionTexture.getHeight()/2-1,
			selectionTexture);
	
		waypoints = new ArrayDeque<WayPoint>();
		waypoints.add(new WayPoint(this, null, pTile));
	
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
		if(pTarget == null) {
			return 0;
		}
		
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
	

	/**
	 * let a soldier walk its waypoints
	 * @author <a href="https://github.com/winniehell/">winniehell</a>
	 */
	private class PathWalker implements IModifierListener<IEntity>, ITimerCallback {
	
		public PathWalker(final GameActivity pGameActivity) {
			gameActivity = pGameActivity;
			finished = false;
			
			targetTile = null;
			stepIndex = 0;
		}
	
		public synchronized void finish() {
			if(!finished) {
				Log.d(PathWalker.class.getName(), "finished() "+id);
				gameActivity.onSoldierStopped();
					
				finished = true;
			}
		}

		@Override
		public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
	
		}
	
		@Override
		public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
			if(pModifier instanceof MoveModifier)
			{
				stopAnimation(0);
				nextTile();
			}
			else if(pModifier instanceof RotationByModifier) {
				aim = null;
			}
			
			soldierContinue();
		}

		@Override
		public void onTimePassed(final TimerHandler pTimerHandler) {
			pauseTimer = null;
			soldierContinue();
		}
		
		private void soldierContinue()
		{
			if(pauseTimer != null) {
				registerUpdateHandler(pauseTimer);
			}
			else if(targetTile != null)
			{
				move(targetTile);
			}
			else if(aim != null)
			{
				faceTarget(aim, this);
			}
			else {
				finish();
			}
		}
		
		public synchronized void start()
		{
			Log.d(PathWalker.class.getName(), "start() "+id);

			processWaypoint();
			
			if(nextWaypoint()) {
				nextTile();
			}
			
			soldierContinue();
		}

		private final GameActivity gameActivity;
		private boolean finished;
		
		private TimerHandler pauseTimer;
	
		private int stepIndex;
		private Tile targetTile;
		
		private void nextTile() {
			Log.d(PathWalker.class.getName(), "nextTile() "+waypoints.size()+" "+stepIndex);
			
			// finished walking the path
			if(stepIndex >= getFirstWaypoint().getPath().getLength())
			{
				processWaypoint();
				
				if(!nextWaypoint()) {
					targetTile = null;					
					return;
				}
			}
			
			targetTile = new Tile(getFirstWaypoint().getPath().getStep(stepIndex));
			++stepIndex;
			
			positionListener.onObjectMoved(Soldier.this, getTile(), targetTile);
		}

		private boolean nextWaypoint() {
			if(waypoints.size() > 1) {
				waypoints.removeFirst().remove();
				stepIndex = 1;
				
				getFirstWaypoint().dropPath();
				
				return true;
			}
			else {
				return false;
			}
		}
		
		private void processWaypoint() {
			final WayPoint waypoint = getFirstWaypoint();
			
			setAim(waypoint.getAim());
			setIgnoreShots(waypoint.ignoresShots());

			if(waypoint.getWait() > 0) {
				pauseTimer = new TimerHandler(waypoint.getWait(), false, this);
			}
		}
	}
}
