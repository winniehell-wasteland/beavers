package org.beavers.ingame;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.modifier.MoveByModifier;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
import org.anddev.andengine.util.path.Path.Step;
import org.beavers.gameplay.GameScene;

/**
 * let a soldier walk its waypoints
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class PathWalker implements IModifierListener<IEntity> {

	private TMXTile nextTile;

	public PathWalker(final GameScene pGameScene, final Soldier pSoldier) {
		gameScene = pGameScene;
		soldier = pSoldier;
		wayPointIndex = 0;
	}

	@Override
	public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
		if(pModifier instanceof MoveByModifier)
		{
			++stepIndex;

			if(stepIndex == nextWaypoint.getPath().getLength())
			{
				nextWaypoint = getNextWaypoint();
			}

			if(nextWaypoint != null)
			{
				final Step nextStep = nextWaypoint.getPath().getStep(stepIndex);
				nextTile = gameScene.getTMXLayer().getTMXTile(nextStep.getTileColumn(), nextStep.getTileRow());

				soldier.move(nextTile, this);
			}
		}
	}

	public void start()
	{
		nextWaypoint = getNextWaypoint();

		final Step nextStep = nextWaypoint.getPath().getStep(stepIndex);
		nextTile = gameScene.getTMXLayer().getTMXTile(nextStep.getTileColumn(), nextStep.getTileRow());

		soldier.move(nextTile, this);
	}

	private final GameScene gameScene;
	private final Soldier soldier;

	private WayPoint nextWaypoint;
	private int stepIndex;
	private int wayPointIndex;

	private WayPoint getNextWaypoint()
	{
		stepIndex = 1;
		return soldier.getWayPoint(++wayPointIndex);
	}
}
