package org.beavers.ingame;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;
import org.beavers.gameplay.GameScene;

public class Shot extends Sprite implements GameObject {


	private static final int SPEED = 250;

	private final Soldier soldier;

	private final Sprite flash;

	public Line targetLine;

	public Shot(final Soldier pSoldier){
		super(pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[0], pSoldier.convertLocalToSceneCoordinates(pSoldier.getWidth()/2+5, pSoldier.getHeight()/2-18)[1], Textures.SHOT_BULLET);

		soldier = pSoldier;

		flash = new Sprite(0, 0, Textures.MUZZLE_FLASH.deepCopy());

		setAlpha(0.0f);
	}

	@Override
	public synchronized Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {

		final int distance = Math.max(Math.abs(getTile().getTileColumn() - pTarget.getTileColumn()),
				Math.abs(getTile().getTileRow() - pTarget.getTileRow()));

		targetLine = new Line(GameScene.getTileCenterX(getTile()), GameScene.getTileCenterY(getTile()),
				GameScene.getTileCenterX(pTarget), GameScene.getTileCenterY(pTarget));

		return pPathFinder.findPath(this, distance, getTile().getTileColumn(), getTile().getTileRow(),
        		pTarget.getTileColumn(), pTarget.getTileRow());
	}

	@Override
	public TMXTile getTile() {
		return soldier.getTile();
	}

	@Override
	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo) {
		// prevent diagonals at blocked tiles
		if((Math.abs(pTo.getTileRow() - pFrom.getTileRow()) == 1)
				&& (Math.abs(pTo.getTileColumn() - pFrom.getTileColumn()) == 1))
		{
			if(pMap.isTileBlocked(this, pFrom.getTileColumn(), pTo.getTileRow())
					|| pMap.isTileBlocked(this, pTo.getTileColumn(), pFrom.getTileRow()))
			{
				return Integer.MAX_VALUE;
			}
		}

		final Rectangle tileRect = new Rectangle(pTo.getTileX()+1, pTo.getTileY()+1, pTo.getTileWidth()-2, pTo.getTileHeight()-2);

		if(targetLine.collidesWith(tileRect))
		{
			return 1;
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}

	public void fire(final TMXTile pTarget){

		final float[] f=soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+5, soldier.getHeight()/2-18);

		flash.setPosition(f[0],f[1]);
		setPosition(f[0],f[1]);

		setAlpha(1.0f);

		final float distx=Math.abs(getX() - GameScene.getTileCenterX(pTarget));
			final float disty=Math.abs(getY() - GameScene.getTileCenterY(pTarget));


			final MoveModifier moveMod= new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/SPEED), getX(), GameScene.getTileCenterX(pTarget), getY(), GameScene.getTileCenterY(pTarget));

			moveMod.addModifierListener(new IModifierListener<IEntity>() {

				@Override
				public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
					// TODO Auto-generated method stub
					Shot.this.detachSelf();
				}
			});
			flash.setPosition(soldier.convertSceneToLocalCoordinates(getX(), getY())[0]-5, soldier.convertSceneToLocalCoordinates(getX(), getY())[1]-13);
			soldier.attachChild(flash);
			registerEntityModifier(moveMod);
			spriteExpire(flash);
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
