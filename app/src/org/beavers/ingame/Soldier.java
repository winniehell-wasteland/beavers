package org.beavers.ingame;

import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.HoldDetector;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.beavers.AppActivity;
import org.beavers.gameplay.GameScene;


public class Soldier extends GameObject {
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion playerTextureRegion;
	private AnimatedSprite sprite;
	private int speed=80;
	private boolean isSelected=false;
	private GameScene gscene;
	private Soldier self;
	public float x,y=0;
	
	public Soldier(final AppActivity pApp,GameScene s, int team){
		gscene=s;
		self=this;
		
		if(team==0){
			mBitmapTextureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, pApp, "96x96anim.png", 0, 0, 3, 2);
			pApp.getEngine().getTextureManager().loadTexture(mBitmapTextureAtlas);
			
		}
		else{}
	}
	private int targetX;
	private int targetY;
	private Rectangle r;	
	public void createSoldier(int x, int y){
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
            protected void onManagedUpdate(float pSecondsElapsed) {
                    // TODO Auto-generated method stub
                    super.onManagedUpdate(pSecondsElapsed);
                    if(((int)this.getX()+this.getWidth()/2<=targetX && (int)this.getX()+this.getWidth()/2>=targetX)
                    		&&((int)this.getY()+this.getHeight()/2<=targetY && (int)this.getY()+this.getHeight()/2>=targetY)){
                    	sprite.stopAnimation();
                    	sprite.setCurrentTileIndex(0);
                    }
                  
            }
			
			
		};
		sprite.stopAnimation();
		sprite.setRotationCenter(sprite.getWidth()/2, sprite.getHeight()/2);
		gscene.attachChild(sprite);
	}
	

	public AnimatedSprite getSprite(){
		return sprite;
	}
	
	public float[] getXY(){
		
		float[] pos= new float[2];
		pos[0]=sprite.getX()+sprite.getWidth()/2;
		pos[1]=sprite.getY()+sprite.getHeight()/2;
		return pos;
	}
	
	public void markSelected(){
		r= new Rectangle(sprite.getWidth()/2, sprite.getHeight()/2, 10, 10);
		r.setColor(1,0 , 0);
		sprite.attachChild(r);
		isSelected=true;
	}
	
	public void markDeselected(){
		sprite.detachChild(r);
		isSelected=false;
	}
	
	public float[] getSceneCoordinates(){
		return sprite.convertLocalToSceneCoordinates(10, 10); //10x10 TMX Map
	}
	
	private MoveModifier mod;
	public void move(int x, int y){
		targetX=x;
		targetY=y;
		//Bewegung nach x,y
		float distx=Math.abs(x-(sprite.getX()+sprite.getWidth()/2));
		float disty=Math.abs(y-(sprite.getY()+sprite.getHeight()/2));
		mod = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/speed),sprite.getX(),x-sprite.getWidth()/2, sprite.getY(),y-sprite.getHeight()/2);
		sprite.registerEntityModifier(mod);
		
		//Rotation
		float angleX=x-(sprite.getX()+sprite.getWidth()/2);
		float angleY=y-(sprite.getY()+sprite.getHeight()/2);
		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
		RotationByModifier rotate;
	    
		if((angle-sprite.getRotation())>180)angle=(angle-sprite.getRotation())-360;
		else if((angle-sprite.getRotation())<-180)angle=360+(angle-sprite.getRotation());
		else angle=angle-sprite.getRotation();
		
		rotate= new RotationByModifier(0.2f, angle);
		
		sprite.registerEntityModifier(rotate);
		
		sprite.animate(new long[]{200, 200}, 1, 2, true);
		
		
	
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
	
	public Action getAction(int index)
	{
		return null;
	}

	@Override
	public String getType() {
		return this.getClass().getSimpleName();
	}



	

	
	
}
