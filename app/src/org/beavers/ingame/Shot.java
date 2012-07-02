package org.beavers.ingame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.IEntityModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.Transformation;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;

public class Shot implements GameObject{


	private static final int SPEED = 350;

	private final Soldier soldier;
	private final GameActivity activity;



	public Line targetLine;

	public Shot(final Soldier pSoldier, final GameActivity pActivity){
		
        activity = pActivity;
		soldier = pSoldier;

	}

	@Override
	public synchronized Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {

		final int distance = Math.max(Math.abs(getTile().getTileColumn() - pTarget.getTileColumn()),
				Math.abs(getTile().getTileRow() - pTarget.getTileRow()));

		targetLine = new Line(GameActivity.getTileCenterX(getTile()), GameActivity.getTileCenterY(getTile()),
				GameActivity.getTileCenterX(pTarget), GameActivity.getTileCenterY(pTarget));
		
		targetLine.setLineWidth(4);
		targetLine.setColor(1, 0, 0);
		//activity.getMainScene().attachChild(targetLine);

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

   TMXTile target;
   float delay=(float) (0.1+Math.random()*0.4);
   TimerHandler shootTimer;
   Sprite currentShot;
	
   public void fire(final Soldier targetSoldier){
		final TMXTile pTarget=targetSoldier.getTile();
	
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
				
				final float distx=Math.abs(shot.getX() - targetSoldier.getCenter()[0]);//GameActivity.getTileCenterX(pTarget));
					final float disty=Math.abs(shot.getY() - targetSoldier.getCenter()[1]);//GameActivity.getTileCenterY(pTarget));


					final MoveModifier moveMod= new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/SPEED), shot.getX(), (float) (targetSoldier.getCenter()[0]-10+Math.random()*20), shot.getY(), (float) (targetSoldier.getCenter()[1]-10+Math.random()*20));
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
							if(targetSoldier.getRotation()>soldier.getRotation()-10 && targetSoldier.getRotation()<soldier.getRotation()+10){
								targetSoldier.changeHP(-20);
							}
							else targetSoldier.changeHP(-10);
							if(!targetSoldier.isShooting())targetSoldier.fireShot(soldier, activity);
							if(soldier.isdead())stopShooting();
							if(targetSoldier.isdead()){
								stopShooting();
								soldier.setShooting(false);
								soldier.resume();
								if(targetSoldier.getShot()!=null)targetSoldier.getShot().stopShooting();
							}
						}
					});
					
					
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
	 soldier.setShooting(false);
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
	


	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVisible(final boolean pVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isIgnoreUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIgnoreUpdate(final boolean pIgnoreUpdate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isChildrenVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChildrenVisible(final boolean pChildrenVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isChildrenIgnoreUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChildrenIgnoreUpdate(final boolean pChildrenIgnoreUpdate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getZIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setZIndex(final int pZIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasParent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IEntity getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(final IEntity pEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getInitialX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getInitialY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInitialPosition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPosition(final IEntity pOtherEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPosition(final float pX, final float pY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRotated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getRotation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRotation(final float pRotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getRotationCenterX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getRotationCenterY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRotationCenterX(final float pRotationCenterX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRotationCenterY(final float pRotationCenterY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRotationCenter(final float pRotationCenterX, final float pRotationCenterY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isScaled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getScaleX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getScaleY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setScaleX(final float pScaleX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScaleY(final float pScaleY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScale(final float pScale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScale(final float pScaleX, final float pScaleY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getScaleCenterX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getScaleCenterY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setScaleCenterX(final float pScaleCenterX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScaleCenterY(final float pScaleCenterY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScaleCenter(final float pScaleCenterX, final float pScaleCenterY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getRed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGreen() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getBlue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAlpha() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(final float pAlpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColor(final float pRed, final float pGreen, final float pBlue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColor(final float pRed, final float pGreen, final float pBlue, final float pAlpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float[] getSceneCenterCoordinates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertLocalToSceneCoordinates(final float pX, final float pY) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertLocalToSceneCoordinates(final float pX, final float pY,
			final float[] pReuse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertLocalToSceneCoordinates(final float[] pCoordinates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertLocalToSceneCoordinates(final float[] pCoordinates,
			final float[] pReuse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertSceneToLocalCoordinates(final float pX, final float pY) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertSceneToLocalCoordinates(final float pX, final float pY,
			final float[] pReuse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertSceneToLocalCoordinates(final float[] pCoordinates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] convertSceneToLocalCoordinates(final float[] pCoordinates,
			final float[] pReuse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transformation getLocalToSceneTransformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transformation getSceneToLocalTransformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onAttached() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDetached() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attachChild(final IEntity pEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean attachChild(final IEntity pEntity, final int pIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IEntity getChild(final int pIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEntity getFirstChild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEntity getLastChild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildIndex(final IEntity pEntity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean setChildIndex(final IEntity pEntity, final int pIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IEntity findChild(final IEntityMatcher pEntityMatcher) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<IEntity> query(final IEntityMatcher pEntityMatcher) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <L extends List<IEntity>> L query(final IEntityMatcher pEntityMatcher,
			final L pResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends IEntity> ArrayList<S> queryForSubclass(
			final IEntityMatcher pEntityMatcher) throws ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <L extends List<S>, S extends IEntity> L queryForSubclass(
			final IEntityMatcher pEntityMatcher, final L pResult) throws ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean swapChildren(final int pIndexA, final int pIndexB) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean swapChildren(final IEntity pEntityA, final IEntity pEntityB) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sortChildren() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sortChildren(final Comparator<IEntity> pEntityComparator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean detachSelf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean detachChild(final IEntity pEntity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IEntity detachChild(final IEntityMatcher pEntityMatcher) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean detachChildren(final IEntityMatcher pEntityMatcher) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void detachChildren() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callOnChildren(final IEntityCallable pEntityCallable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callOnChildren(final IEntityMatcher pEntityMatcher,
			final IEntityCallable pEntityCallable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerUpdateHandler(final IUpdateHandler pUpdateHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean unregisterUpdateHandler(final IUpdateHandler pUpdateHandler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unregisterUpdateHandlers(
			final IUpdateHandlerMatcher pUpdateHandlerMatcher) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearUpdateHandlers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerEntityModifier(final IEntityModifier pEntityModifier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean unregisterEntityModifier(final IEntityModifier pEntityModifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unregisterEntityModifiers(
			final IEntityModifierMatcher pEntityModifierMatcher) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearEntityModifiers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUserData(final Object pUserData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getUserData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDraw(final GL10 pGL, final Camera pCamera) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdate(final float pSecondsElapsed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	
}
