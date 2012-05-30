package org.beavers.ingame;

import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.beavers.AppActivity;
import org.beavers.gameplay.GameScene;

public class Shot extends GameObject {

	
	
	private final int speed=220;
	float targetX, targetY;
	GameScene scene;
	
	private final BitmapTextureAtlas bulletTextureAtlas;
	private final TextureRegion bulletTextureRegion;
	private final Sprite bullet;
	private final Soldier soldier;
	
	
	public Shot(final Soldier soldier, final AppActivity pApp,final GameScene s,final float startX, final float startY,  final float zielX, final float zielY){
		targetX=zielX;
		targetY=zielY;
		scene=s;
		this.soldier=soldier;
		
		bulletTextureAtlas = new BitmapTextureAtlas(4,4,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		bulletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bulletTextureAtlas, pApp, "bullet.png", 0, 0);
		pApp.getEngine().getTextureManager().loadTexture(bulletTextureAtlas);
		bullet=new Sprite(startX, startY, bulletTextureRegion){
			@Override
			protected void onManagedUpdate(final float pSecondsElapsed) {
				// TODO Auto-generated method stub
				super.onManagedUpdate(pSecondsElapsed);
				if(bullet.getX()==targetX && bullet.getY()==targetY);//scene.detachChild(bullet);
			}
		};
		
		bullet.setPosition(startX, startY);
		scene.attachChild(bullet);
		move(startX,startY);
	}
	
	public void move(final float startX, final float startY){
		//Bullet an die Mündung der Waffe setzen
		/*bullet.setPosition(soldier.getSprite().getX()+soldier.getSprite().getWidth()/2, soldier.getSprite().getY()+soldier.getSprite().getHeight()/2);
		bullet.setRotationCenter(soldier.getSprite().getX()+soldier.getSprite().getWidth()/2, soldier.getSprite().getY()+soldier.getSprite().getHeight()/2);
		final RotationByModifier rotate = new RotationByModifier(1f,soldier.getSprite().getRotation());
		
		bullet.registerEntityModifier(rotate);
		//
		//Bullet abschießen
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
		final float distx=Math.abs(bullet.getX()-targetX);
			final float disty=Math.abs(bullet.getY()-targetY);
			final float distC=(float) (Math.sqrt(distx*distx+disty*disty));
			
			final MoveModifier moveMod= new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/speed), bullet.getX(), targetX, bullet.getY(), targetY);
			bullet.registerEntityModifier(moveMod);
			
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
