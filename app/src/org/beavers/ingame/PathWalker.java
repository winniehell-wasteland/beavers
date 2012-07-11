package org.beavers.ingame;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveModifier;
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
			if(pauseTimer==null){
				soldierContinue();
			}
			
		}
	}
	
	public void soldierContinue()
	{
		if(targetTile != null)
		{
			soldier.move(targetTile, aim, this);
		}
		else if(aim != null)
		{
			soldier.faceTarget(aim, null);
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
	private TimerHandler pauseTimer;
	private WayPoint waypoint;

	private int stepIndex;
	private Tile sourceTile, targetTile;
	private Tile aim;
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
			
			soldier.ignoreShots(waypoint.ignoresShots());

			final int wait=waypoint.getWait();
			if(wait>0){
				pauseTimer = new TimerHandler(wait, false, new ITimerCallback() {
					@Override
					public void onTimePassed(final TimerHandler pTimerHandler)
					{
						pauseTimer = null;
						soldierContinue();
					}
				});
				soldier.registerUpdateHandler(pauseTimer);
			}
			
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
			targetTile = new Tile(waypoint.getPath().getStep(stepIndex));

			++stepIndex;
		}
		else
		{
			targetTile = null;
		}
	}
}
