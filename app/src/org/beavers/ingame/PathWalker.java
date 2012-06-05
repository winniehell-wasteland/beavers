package org.beavers.ingame;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.Path.Step;
import org.beavers.gameplay.GameScene;

/**
 * let a soldier walk its waypoints
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class PathWalker implements IModifierListener<IEntity> {

	public PathWalker(final GameScene pGameScene, final Soldier pSoldier) {
		gameScene = pGameScene;
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
			gameScene.moveObject(soldier, sourceTile, targetTile);

			nextTile();

			if(targetTile != null)
			{
				soldier.move(targetTile, focus, this);
			}
		}
	}

	public void start()
	{
		targetTile = soldier.getTile();

		nextWaypoint();
		nextTile();

		soldier.move(targetTile, focus, this);
	}

	private final GameScene gameScene;
	private final Soldier soldier;

	private WayPoint waypoint;

	private int stepIndex;
	private TMXTile sourceTile, targetTile;
	private TMXTile focus;

	private void nextWaypoint()
	{
		if(waypoint != null)
		{
			waypoint.detachChildren();
			gameScene.removeObject(waypoint);
			if(waypoint.getFocus()!=null)gameScene.removeObject(waypoint.getFocus());
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
			if(waypoint.getFocus()!=null){
			focus=waypoint.getFocus().getTile();}
			else focus=null;

			++stepIndex;

			targetTile = gameScene.getTMXLayer().getTMXTile(nextStep.getTileColumn(), nextStep.getTileRow());
		}
		else
		{
			targetTile = null;
		}
	}
}
