package org.beavers.gameplay;

/**
 * class to uniquely identify a game
 * @author winniehell
 */
public final class GameInfo {

	/**
	 * default constructor
	 * @param pServer server of the game
	 * @param pGame unique game ID on server
	 */
	public GameInfo(final PlayerID pServer, final GameID pGame) {
		game = pGame;
		server = pServer;
		state = GameState.UNKNOWN;
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof GameInfo)
		{
			return server.equals(((GameInfo)other).server)
					&& game.equals(((GameInfo)other).game);
		}
		else
		{
			return false;
		}
	}

	/** @return unique game ID on server */
	public GameID getID() {
		return game;
	}

	// TODO move to Client?
	public int getInitialActionPoints()
	{
		return -1;
	}

	/** @return name of the map played */
	public String getMapName()
	{
		return null;
	}

	/** @return server of the game */
	public PlayerID getServer() {
		return server;
	}

	/** @return current game state */
	public GameState getState() {
		return state;
	}

	/**
	 * changes the server
	 * @param pServer new server
	 */
	public void setServer(final PlayerID pServer) {
		server = pServer;
	}

	/**
	 * changes the state
	 * @param pState new state
	 */
	public void setState(final GameState pState) {
		state = pState;
	}

	@Override
	public String toString() {
		return server.toString()+"/"+game.toString();
	}

	/** unique game ID on server */
	private final GameID game;
	/** server of the game */
	private PlayerID server;
	/** state of the game */
	private GameState state;
}
