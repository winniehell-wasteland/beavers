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

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.Direction;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.IWeightedPathFinder;
import org.anddev.andengine.util.path.NegativeStepCostException;
import org.anddev.andengine.util.path.WeightedPath;

import de.winniehell.battlebeavers.Textures;
import de.winniehell.battlebeavers.gameplay.GameActivity;

public class Attack implements IMovableObject, ITimerCallback {

	@Override
	public synchronized WeightedPath findPath(final IWeightedPathFinder<IMovableObject> pPathFinder, final Tile pTarget) {
		final int distance = Math.max(
			Math.abs(soldier.getTile().getColumn() - pTarget.getColumn()),
			Math.abs(soldier.getTile().getRow() - pTarget.getRow()));

		try {
			return pPathFinder.findPath(this, distance, soldier.getTile().getColumn(),
				soldier.getTile().getRow(), pTarget.getColumn(), pTarget.getRow());
		} catch (final NegativeStepCostException e) {
			// should not happen
			return null;
		}
	}
	
	public boolean hasPath() {
		return findPath(activity.getPathFinder(), target.getTile()) != null;
	}

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
		}

		final Rectangle tileRect =
			new Rectangle(pTo.getX()+1, pTo.getY()+1,
			              pTo.getTileWidth()-2, pTo.getTileHeight()-2);

		final Line targetLine =
			new Line(soldier.getTile().getCenterX(), soldier.getTile().getCenterY(),
		             target.getTile().getCenterX(), target.getTile().getCenterY());

		
		if(targetLine.collidesWith(tileRect))
		{
			return 1;
		}
		else
		{
			return Float.POSITIVE_INFINITY;
		}
	}

	public void fire(){
		activity.getEngine().registerUpdateHandler(timer);
	}
	
	@Override
	public void onTimePassed(final TimerHandler pTimerHandler) {
		
		if(soldier.isAttacking()) {
			currentShot = this.new Shot();
			activity.getEngine().getScene().attachChild(currentShot);
		
			pTimerHandler.setTimerSeconds(calcDelay());
			pTimerHandler.reset();
		}
		else {
			activity.getEngine().unregisterUpdateHandler(pTimerHandler);
		}
	}

	public static Attack create(final GameActivity pActivity, final Soldier pSoldier,
			final Soldier pTarget) {
		final Attack attack = new Attack(pActivity, pSoldier, pTarget);
		
		if(attack.hasPath()) {
			return attack;
		}
		else {
			return null;
		}
	}

	/**
	 * @name constants
	 * @{
	 */
	private static final int BASE_DAMAGE = 10;
	private static final int SPEED = 350;
	/**
	 * @}
	 */

	private final GameActivity activity;
	
	private final Soldier soldier;
	private final Soldier target;
	
	private final TimerHandler timer;
	private Shot currentShot = null;

	private Attack(final GameActivity pActivity,
	             final Soldier pSoldier, final Soldier pTarget){
        activity = pActivity;

		soldier = pSoldier;
		target = pTarget;
		
		timer = new TimerHandler(calcDelay(), false, this);
	}
	
	private static float calcDelay() {
		return (float) (0.2+Math.random()*0.4);
	}
	
	// bullet
	private class Shot extends Sprite {
		public Shot() {
			super(
				soldier.convertLocalToSceneCoordinates(
					soldier.getWidth()/2+5, soldier.getHeight()/2-18)[0],
				soldier.convertLocalToSceneCoordinates(
					soldier.getWidth()/2+5, soldier.getHeight()/2-18
				)[1], Textures.SHOT_BULLET.deepCopy()
			);
			
			currentShot = this;

			final float[] s = soldier.convertLocalToSceneCoordinates(
				soldier.getWidth()/2+6, soldier.getHeight()/2-32
			);
			setPosition(s[0]-getWidth()/2,s[1]-getHeight()/2);
			setRotationCenter(getWidth()/2, getHeight()/2);
			setRotation(soldier.getRotation()-270);
			setAlpha(0.5f);
			
			/*
			//muzzleflash
			final Sprite flash= new Sprite(0, 0, Textures.MUZZLE_FLASH.deepCopy());
			flash.setPosition(soldier.getWidth()/2,soldier.getHeight()/2-31);
			soldier.attachChild(flash);
			*/
			
			final float distx=Math.abs(getX() - target.getCenter()[0]);
			final float disty=Math.abs(getY() - target.getCenter()[1]);
			final MoveModifier moveMod= new MoveModifier(
				(float) (Math.sqrt(distx*distx+disty*disty)/SPEED), getX(),
				(float) (target.getCenter()[0]-10+Math.random()*20), getY(),
				(float) (target.getCenter()[1]-10+Math.random()*20)
			);

			moveMod.addModifierListener(new IModifierListener<IEntity>() {

				@Override
				public void onModifierStarted(final IModifier<IEntity> pModifier,
						final IEntity pItem) {
					
				}

				@Override
				public void onModifierFinished(final IModifier<IEntity> pModifier,
						final IEntity pItem) {
					Shot.this.onHit(moveMod);
				}
			});

			registerEntityModifier(moveMod);
			//spriteExpire(flash);
		}
		
		private void onHit(final MoveModifier pMovement) {
			// remove bullet at destination
			detachSelf();
			
			//Calculate damage
			
			//bonus damage in close combat
			int damage = Math.max(0, 10 - (int) (pMovement.getDuration()*SPEED/60)*2);
			
			//bonus damage if shooting in targets back
			if(target.getRotation()>soldier.getRotation()-10
			   && target.getRotation()<soldier.getRotation()+10){
				damage+=10;
			}
			else if(target.getRotation()>soldier.getRotation()-20
			        && target.getRotation()<soldier.getRotation()+20){
				damage+=5;
			}
			
			damage+=BASE_DAMAGE;
			
			// he's dead, Jim!
			if(target.isDead()) {
				soldier.stopAttacking();
				soldier.resume();
			}
			else if(hasPath()) {
				if(!target.getSimulation()){
					target.changeHP(-damage);
				}

				//target soldier defends himself
				target.attack(soldier, activity);
			}
		}
	}
}
