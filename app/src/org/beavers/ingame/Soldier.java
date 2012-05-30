package org.beavers.ingame;

import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.beavers.AppActivity;
import org.beavers.gameplay.GameScene;


public class Soldier extends GameObject {
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion playerTextureRegion;
	private AnimatedSprite sprite;
	private final int speed=80;
	private boolean isSelected=false;
	private final GameScene gscene;
	private final Soldier self;
	public float x,y=0;
	//Circle
	private final BitmapTextureAtlas circleTextureAtlas;
	private final TextureRegion circleTextureRegion;
	private final Sprite circle;
	private final AppActivity pApp;
	
	
	public Soldier(final AppActivity pApp,final GameScene s, final int team){
		gscene=s;
		self=this;
		
		this.pApp=pApp;
		
		
		if(team==0){
			mBitmapTextureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, pApp, "96x96anim.png", 0, 0, 3, 2);
			pApp.getEngine().getTextureManager().loadTexture(mBitmapTextureAtlas);
			
		}
		else{}
		
		//Selection Circle
		circleTextureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		circleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(circleTextureAtlas, pApp, "circle.png", 0, 0);
		pApp.getEngine().getTextureManager().loadTexture(circleTextureAtlas);
		circle=new Sprite(0, 0, circleTextureRegion);
		
		
	}
	private int targetX;
	private int targetY;
		
	public void createSoldier(final int x, final int y){
		targetX=x;
		targetY=y;
		sprite=new AnimatedSprite(x,y,playerTextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				
				if(isSelected==false){
					
					markSelected();
					gscene.setSelectedSoldier(self);
				}
				else{
				
					markDeselected();
					gscene.setSelectedSoldier(null);
				}
				
				return true;
				
				
			}
			
			@Override
            protected void onManagedUpdate(final float pSecondsElapsed) {
                    // TODO Auto-generated method stub
                    super.onManagedUpdate(pSecondsElapsed);
                    if(((int)getX()+getWidth()/2<=targetX && (int)getX()+getWidth()/2>=targetX)
                    		&&((int)getY()+getHeight()/2<=targetY && (int)getY()+getHeight()/2>=targetY)){
                    	sprite.stopAnimation();
                    	sprite.setCurrentTileIndex(0);
                    }
                  
            }
			
			
		};
		circle.setPosition((sprite.getWidth()-circle.getWidth())/2, (sprite.getHeight()-circle.getHeight())/2+5);
		sprite.stopAnimation();
		sprite.setRotationCenter(sprite.getWidth()/2, sprite.getHeight()/2);
		sprite.setZIndex(1);
		gscene.attachChild(sprite);
	}
	

	public AnimatedSprite getSprite(){
		return sprite;
	}
	
	public float[] getXY(){
		
		final float[] pos= new float[2];
		pos[0]=sprite.getX()+sprite.getWidth()/2;
		pos[1]=sprite.getY()+sprite.getHeight()/2;
		return pos;
	}
	
	public void markSelected(){
		sprite.attachChild(circle);
		isSelected=true;
	}
	
	public void markDeselected(){
		sprite.detachChild(circle);
		isSelected=false;
	}
	
	public float[] getSceneCoordinates(){
		return sprite.convertLocalToSceneCoordinates(10, 10); //10x10 TMX Map
	}
	
	private MoveModifier mod;
	public void move(final int x, final int y){
		targetX=x;
		targetY=y;
		//Bewegung nach x,y
		final float distx=Math.abs(x-(sprite.getX()+sprite.getWidth()/2));
		final float disty=Math.abs(y-(sprite.getY()+sprite.getHeight()/2));
		mod = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/speed),sprite.getX(),x-sprite.getWidth()/2, sprite.getY(),y-sprite.getHeight()/2);
		sprite.registerEntityModifier(mod);
		
		//Rotation
		final float angleX=x-(sprite.getX()+sprite.getWidth()/2);
		final float angleY=y-(sprite.getY()+sprite.getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
		RotationByModifier rotate;
	    
		if((angle-sprite.getRotation())>180)angle=(angle-sprite.getRotation())-360;
		else if((angle-sprite.getRotation())<-180)angle=360+(angle-sprite.getRotation());
		else angle=angle-sprite.getRotation();
		
		rotate= new RotationByModifier(0.6f, angle);
		
		sprite.registerEntityModifier(rotate);
		
		sprite.animate(new long[]{200, 200}, 1, 2, true);
		
		
	
	}
	
	public void faceTarget(final float faceX,final float faceY){
		final float angleX=faceX-(sprite.getX()+sprite.getWidth()/2);
		final float angleY=faceY-(sprite.getY()+sprite.getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
		
	    
		if((angle-sprite.getRotation())>180)angle=(angle-sprite.getRotation())-360;
		else if((angle-sprite.getRotation())<-180)angle=360+(angle-sprite.getRotation());
		else angle=angle-sprite.getRotation();
		
		final RotationByModifier rotateView= new RotationByModifier(0.6f, angle);
		
		sprite.registerEntityModifier(rotateView);
	}
	
	public void shootAt(final float centerX, final float centerY){
		faceTarget(centerX, centerY);
		
		new Shot(this, pApp,gscene,sprite.getX()+sprite.getWidth()/2,sprite.getY()+sprite.getHeight()/2,centerX,centerY);
	}
	
	public void stop()
	{
		sprite.stopAnimation();
		if(mod!=null)sprite.unregisterEntityModifier(mod);
	}
	
	public int getHealthPercentage()
	{
		return -1;
	}
	
	public Weapon getWeapon()
	{
		return null;
	}
	
	public ViewDirection getViewDirection()
	{
		return null;
	}
	
	public Action getAction(final int index)
	{
		return null;
	}

	@Override
	public String getType() {
		return this.getClass().getSimpleName();
	}



	

	
	
}
