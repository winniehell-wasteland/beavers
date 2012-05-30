package org.beavers.ingame;

import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.sprite.Sprite;
import org.beavers.Textures;

public class Shot extends Sprite implements GameObject {


	private static final int SPEED = 220;

	float targetX, targetY;

	private final Soldier soldier;


	public Shot(final Soldier soldier, final float startX, final float startY,  final float zielX, final float zielY){
		super(startX, startY, Textures.SHOT_BULLET);

		targetX=zielX;
		targetY=zielY;
		this.soldier=soldier;

		setPosition(startX, startY);
		move(startX,startY);
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		// TODO Auto-generated method stub
		super.onManagedUpdate(pSecondsElapsed);
		if(getX()==targetX && getY()==targetY);//scene.detachChild(bullet);
	}

	public void move(final float startX, final float startY){
		//Bullet an die M�ndung der Waffe setzen
		/*bullet.setPosition(soldier.getSprite().getX()+soldier.getSprite().getWidth()/2, soldier.getSprite().getY()+soldier.getSprite().getHeight()/2);
		bullet.setRotationCenter(soldier.getSprite().getX()+soldier.getSprite().getWidth()/2, soldier.getSprite().getY()+soldier.getSprite().getHeight()/2);
		final RotationByModifier rotate = new RotationByModifier(1f,soldier.getSprite().getRotation());

		bullet.registerEntityModifier(rotate);
		//
		//Bullet abschie�en
		rotate.addModifierListener(new IModifierListener<IEntity>() {

			@Override
			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
				// TODO Auto-generated method stub
				//final float distx=Math.abs(bullet.getX()-targetX);
			//	final float disty=Math.abs(bullet.getY()-targetY);
			//	final MoveModifier moveMod= new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/speed), bullet.getX(), targetX, bullet.getY(), targetY);
			//	bullet.registerEntityModifier(moveMod);
			}
		});*/
		final float distx=Math.abs(getX()-targetX);
			final float disty=Math.abs(getY()-targetY);
			final float distC=(float) (Math.sqrt(distx*distx+disty*disty));

			final MoveModifier moveMod= new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/SPEED), getX(), targetX, getY(), targetY);
			registerEntityModifier(moveMod);

	}

}
