package org.beavers.ingame;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.Path.Step;
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
		}
	}

	public void start()
	{
		targetTile = soldier.getTile();

		nextWaypoint();
		nextTile();

		soldier.move(targetTile, aim, this);
	}

	private final GameActivity gameActivity;
	private final Soldier soldier;

	private WayPoint waypoint;

	private int stepIndex;
	private TMXTile sourceTile, targetTile;
	private TMXTile aim;
	
	private Line lineA,lineB,parallelA,parallelB;

	private void nextWaypoint()
	{
		if(waypoint != null)
		{
			waypoint.detachChildren();
			gameActivity.removeObject(waypoint);
		}

		stepIndex = 1;
		waypoint = soldier.popWayPoint();
	}

	public void nextTile() {
		sourceTile = targetTile;

		if(stepIndex >= waypoint.getPath().getLength())
		{
			nextWaypoint();
		}

		if(waypoint != null)
		{
			final Step nextStep = waypoint.getPath().getStep(stepIndex);

			if(waypoint.getAim() != null)
			{
				aim = waypoint.getAim().getTile();
			}
			else
			{
				aim = null;
			}

			++stepIndex;

			targetTile = gameActivity.getCollisionLayer().getTMXTile(nextStep.getTileColumn(), nextStep.getTileRow());
		}
		else
		{
			targetTile = null;
		}
	}
}

