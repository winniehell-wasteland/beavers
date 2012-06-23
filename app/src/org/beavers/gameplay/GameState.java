package org.beavers.gameplay;

import org.beavers.R;

import android.content.Context;

/**
 * represents the state a game is in
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public enum GameState {
	/** default state */
	UNKNOWN,
	/** server has broadcasted GameInfo */
	ANNOUNCED,
	/** client sent join request to server */
	JOINED,
	/** game is in planning phase */
	PLANNING_PHASE,
	/** game is in execution phase */
	EXECUTION_PHASE,
	/** lost connection to server (timeout) */
	ABORTED,
	/** won the game */
	WON,
	/** lost the game */
	LOST;

	public String getName(final Context pContext) {
		switch (this) {
		case UNKNOWN:
			return pContext.getString(R.string.state_unknown);
		case ANNOUNCED:
			return pContext.getString(R.string.state_announced);
		case JOINED:
			return pContext.getString(R.string.state_joined);
		case PLANNING_PHASE:
			return pContext.getString(R.string.state_planning);
		case EXECUTION_PHASE:
			return pContext.getString(R.string.state_execution);
		case ABORTED:
			return pContext.getString(R.string.state_aborted);
		case WON:
			return pContext.getString(R.string.state_won);
		case LOST:
			return pContext.getString(R.string.state_lost);
		default:
			return null;
		}
	}

	public Object toJSON() {
		return name();
	}
}
