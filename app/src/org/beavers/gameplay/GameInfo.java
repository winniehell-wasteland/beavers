package org.beavers.gameplay;

/**
 * @author winniehell
 * class to uniquely identify a game
 */
public final class GameInfo {

	/**
	 * @param pServer server of the game
	 * @param pGame unique game ID on server
	 */
	public GameInfo(final PlayerID pServer, final GameID pGame) {
		server = pServer;
		game = pGame;
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

	/** @return game ID on server */
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
	 * change server
	 * @param pServer new Server
	 */
	public void setServer(final PlayerID pServer) {
		server = pServer;
	}

	/**
	 * change game state
	 * @param pState new state
	 */
	public void setState(final GameState pState) {
		state = pState;
	}

	@Override
	public String toString() {
		return server.toString()+"/"+game.toString();
	}

	private final GameID game;
	private PlayerID server;
	private GameState state;
}
