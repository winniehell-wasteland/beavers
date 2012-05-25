package org.beavers.gameplay;

/**
 * @author winniehell
 * represents the state a game is in
 */
public enum GameState {
	/** default state */
	UNKNOWN(0),
	/** server has broadcasted GameInfo */
	ANNOUNCED(1),
	/** server is waiting for ACK of players */
	STARTED(2),
	/** game is in planning phase */
	PLANNING_PHASE(3),
	/** lost connection to server (timeout) */
	ABORTED(4),
	/** won the game */
	WON(5),
	/** lost the game */
	LOST(6);

	@Override
	public String toString() {
		return magicValue+"";
	};

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
