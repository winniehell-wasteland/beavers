package org.beavers.ingame;

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
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;

public class Shot implements IMovableObject {


	private static final int SPEED = 350;

	private final Soldier soldier;
	private final GameActivity activity;



	public Line targetLine;

	public Shot(final Soldier pSoldier, final GameActivity pActivity){
		//super(pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[0], pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[1], Textures.SHOT_BULLET);
        activity = pActivity;
		soldier = pSoldier;

		//shot = new Sprite(pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[0], pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[1], Textures.SHOT_BULLET.deepCopy());



			//	new Sprite(0, 0, Textures.MUZZLE_FLASH.deepCopy());
		//shot.setPosition(pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[0], pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[1]);


	}

	@Override
	public synchronized Path findPath(final IPathFinder<IMovableObject> pPathFinder, final Tile pTarget) {

		final int distance = Math.max(
			Math.abs(soldier.getTile().getColumn() - pTarget.getColumn()),
			Math.abs(soldier.getTile().getRow() - pTarget.getRow()));

		targetLine =
			new Line(soldier.getTile().getCenterX(), soldier.getTile().getCenterY(),
				pTarget.getCenterX(), pTarget.getCenterY());

		targetLine.setLineWidth(4);
		targetLine.setColor(1, 0, 0);
		//activity.getMainScene().attachChild(targetLine);

		return pPathFinder.findPath(this, distance, soldier.getTile().getColumn(),
			soldier.getTile().getRow(), pTarget.getColumn(), pTarget.getRow());
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
				return Integer.MAX_VALUE;
			}
		}

		final Rectangle tileRect =
			new Rectangle(pTo.getX()+1, pTo.getY()+1,
			              pTo.getTileWidth()-2, pTo.getTileHeight()-2);

		if(targetLine.collidesWith(tileRect))
		{
			return 1;
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}

   Tile target;
   float delay=(float) (0.1+Math.random()*0.4);
   TimerHandler shootTimer;
   Sprite currentShot;

   public void fire(final Tile pTarget){


		shootTimer = new TimerHandler(delay,new ITimerCallback() {

			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				// TODO Auto-generated method stub
				target=pTarget;

				//Bullets
				final Sprite shot = new Sprite(soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+5, soldier.getHeight()/2-18)[0], soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+5, soldier.getHeight()/2-18)[1], Textures.SHOT_BULLET.deepCopy());
				currentShot=shot;
				final float[] s=soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+3, soldier.getHeight()/2-32);
				shot.setPosition(s[0]-shot.getWidth()/2,s[1]-shot.getHeight()/2);
				shot.setRotationCenter(shot.getWidth()/2, shot.getHeight()/2);
				shot.setRotation(soldier.getRotation()-270);
				activity.getMainScene().attachChild(shot);
				shot.setAlpha(0.5f);

				//muzzleflash
				final Sprite flash= new Sprite(0, 0, Textures.MUZZLE_FLASH.deepCopy());
				flash.setPosition(soldier.getWidth()/2,soldier.getHeight()/2-31);
				soldier.attachChild(flash);

				final float distx=Math.abs(shot.getX() - pTarget.getCenterX());
				final float disty=Math.abs(shot.getY() - pTarget.getCenterY());

				final float duration = (float) (Math.sqrt(distx*distx+disty*disty)/SPEED);

				final MoveModifier moveMod =
					new MoveModifier(duration,
						shot.getX(),
						(float) (target.getCenterX()-10+Math.random()*20),
						shot.getY(),
						(float) (target.getCenterY()-10+Math.random()*20));

				moveMod.addModifierListener(new IModifierListener<IEntity>() {
					Sprite current=currentShot;
					@Override
					public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
						// TODO Auto-generated method stub
						current.detachSelf();

					}
				});

				//flash.setPosition(soldier.convertSceneToLocalCoordinates(flash.getX(), flash.getY())[0]-5, soldier.convertSceneToLocalCoordinates(flash.getX(), flash.getY())[1]-13);
				//soldier.attachChild(flash);
				shot.registerEntityModifier(moveMod);
				spriteExpire(flash);

				delay=(float) (0.1+Math.random()*0.4);

				shootTimer.setTimerSeconds(delay);
				shootTimer.reset();

			}
		});
		activity.getEngine().registerUpdateHandler(shootTimer);

	}
 public void stopShooting(){

	 activity.getEngine().unregisterUpdateHandler(shootTimer);
	 shootTimer=null;
 }

	private void spriteExpire(final Sprite flash)
    {
		flash.registerUpdateHandler(new TimerHandler(0.06f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				flash.detachSelf();
			}
			}));


    }
}
