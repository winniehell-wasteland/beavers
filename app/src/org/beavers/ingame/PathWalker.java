package org.beavers.ingame;

import java.util.Iterator;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.beavers.gameplay.GameActivity;

/**
 * let a soldier walk its waypoints
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class PathWalker implements IModifierListener<IEntity> {

	public PathWalker(final GameActivity pGameActivity, final Soldier pSoldier) {
		gameActivity = pGameActivity;
		soldier = pSoldier;

		waypoint = null;
		stepIndex = 0;



	}


	public void checkTargets(){
		final Iterator itr=gameActivity.getSoldiers(0).iterator();
		while(itr.hasNext()){
			final Soldier s=(Soldier)itr.next();
			final Iterator itr2=gameActivity.getSoldiers(0).iterator();
			while(itr2.hasNext()){
				final Soldier t=(Soldier)itr2.next();
				if(s!=t){
					/* parallelA = new Line(
							t.getCenter()[0]-(s.getLineA().getX1()-s.getLineA().getX2()),
							t.getCenter()[1]-(s.getLineA().getY1()-s.getLineA().getY2()),
							t.getCenter()[0]+(s.getLineA().getX1()-s.getLineA().getX2()),
							t.getCenter()[1]+(s.getLineA().getY1()-s.getLineA().getY2())
							);*/
					parallelB = new Line(
							t.getCenter()[0]-(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[0]),
							t.getCenter()[1]-(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[1]),
							t.getCenter()[0]+(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[0]),
							t.getCenter()[1]+(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[1])
							);

					parallelA = new Line(
							t.getCenter()[0]-(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[0]),
							t.getCenter()[1]-(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[1]),
							t.getCenter()[0]+(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[0]),
							t.getCenter()[1]+(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[1])
							);

					lineB=new Line(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[0],
							s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[1],
							s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[0],
							s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[1]);

					lineA=new Line(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[0],
							s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[1],
							s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[0],
							s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[1]);


					if(parallelA.collidesWith(lineB) && parallelB.collidesWith(lineA)){

							s.fireShot(t.getTile(), gameActivity);

					}

				}
			}
		}
	}

	@Override
	public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {

	}

	@Override
	public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
		if(pModifier instanceof MoveModifier)
		{
			soldier.stopAnimation(0);
			gameActivity.moveObject(soldier, sourceTile, targetTile);

			nextTile();

			if(targetTile != null)
			{
				soldier.move(targetTile, aim, this);
			}
			else if(aim != null)
			{
				soldier.faceTarget(aim, null);
			}
		}
	}

	public void start()
	{
		waypoint = soldier.getFirstWaypoint();

		nextWaypoint();

		// check if there are way points left
		if(waypoint != null)
		{
			targetTile = soldier.getTile();
			nextTile();

			soldier.move(targetTile, aim, this);
		}
		else if(aim != null)
		{
			soldier.faceTarget(aim, null);
		}
	}

	private final GameActivity gameActivity;
	private final Soldier soldier;

	private WayPoint waypoint;

	private int stepIndex;
	private Tile sourceTile, targetTile;
	private Tile aim;

	private Line lineA,lineB,parallelA,parallelB;

	private void nextWaypoint()
	{
		if(waypoint != null)
		{
			if(waypoint.getAim() != null)
			{
				aim = waypoint.getAim().getTile();
				waypoint.setAim(null);
			}
			else
			{
				aim = null;
			}

			waypoint.detachChildren();
			gameActivity.removeObject(waypoint);
		}

		stepIndex = 1;
		waypoint = soldier.popWayPoint();
	}

	public void nextTile() {
		sourceTile = targetTile;

		if(stepIndex >= waypoint.getPath().size())
		{
			nextWaypoint();
		}

		if(waypoint != null)
		{
			targetTile = new Tile(waypoint.getPath().get(stepIndex));

			++stepIndex;
		}
		else
		{
			targetTile = null;
		}
	}
}
