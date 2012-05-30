package org.beavers.gameplay;

import org.beavers.R;

import android.content.Context;

/**
 * represents the state a game is in
 *
 * @author winniehell
 */
public enum GameState {
	/** default state */
	UNKNOWN(0),
	/** server has broadcasted GameInfo */
	ANNOUNCED(1),
	/** client sent join request to server */
	JOINED(2),
	/** server is waiting for ACK of players */
	STARTED(3),
	/** game is in planning phase */
	PLANNING_PHASE(4),
	/** game is in execution phase */
	EXECUTION_PHASE(5),
	/** lost connection to server (timeout) */
	ABORTED(6),
	/** won the game */
	WON(7),
	/** lost the game */
	LOST(8);

	public String getName(final Context pContext) {
		switch (this) {
		case UNKNOWN:
			return pContext.getString(R.string.state_unknown);
		case ANNOUNCED:
			return pContext.getString(R.string.state_announced);
		case JOINED:
			return pContext.getString(R.string.state_joined);
		case STARTED:
			return pContext.getString(R.string.state_started);
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

	public int getMagicValue() {
		return magicValue;
	}

	/**
	 * default constructor
	 * @param pMagicValue magic value for communication
	 */
	private GameState(final int pMagicValue) {
		magicValue = pMagicValue;
	}

	/** magic value for communication */
	private final int magicValue;
}
