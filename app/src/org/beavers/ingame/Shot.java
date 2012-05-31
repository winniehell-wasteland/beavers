package org.beavers.ingame;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.beavers.Textures;

public class Shot extends Sprite implements GameObject {


	private static final int SPEED = 250;

	float targetX, targetY;

	private final Soldier soldier;
	private final Sprite flash;

	public Shot(final Soldier soldier,  final float zielX, final float zielY){
		
		
		super(soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+5, soldier.getHeight()/2-18)[0], soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+5, soldier.getHeight()/2-18)[1], Textures.SHOT_BULLET);

		targetX=zielX;
		targetY=zielY;
		this.soldier=soldier;
		final float[] f=soldier.convertLocalToSceneCoordinates(soldier.getWidth()/2+5, soldier.getHeight()/2-18);
		
		flash = new Sprite(0, 0, Textures.MUZZLE_FLASH.deepCopy());
		flash.setPosition(f[0],f[1]);
		
		setPosition(f[0],f[1]);
		move(f[0],f[1]);
	}

	
	private final Shot self=this;
	public void move(final float startX, final float startY){
	
		final float distx=Math.abs(startX-targetX);
			final float disty=Math.abs(startY-targetY);
			

			final MoveModifier moveMod= new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/SPEED), startX, targetX, startY, targetY);
			
			moveMod.addModifierListener(new IModifierListener<IEntity>() {
				
				@Override
				public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
					// TODO Auto-generated method stub
					self.detachSelf();
				}
			});
			flash.setPosition(soldier.convertSceneToLocalCoordinates(startX, startY)[0]-5, soldier.convertSceneToLocalCoordinates(startX, startY)[1]-13);
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
