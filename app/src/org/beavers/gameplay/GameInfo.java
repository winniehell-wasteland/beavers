package org.beavers.gameplay;

/**
 * @author winniehell
 * class to uniquely identify a game
 */
public final class GameInfo {

	private PlayerID server;
	private final GameID game;
	
	/**
	 * @param pServer server of the game
	 * @param pGame unique game ID on server
	 */
	public GameInfo(final PlayerID pServer, final GameID pGame) {
		this.server = pServer;
		this.game = pGame;
	}

	public PlayerID getServer() {
		return server;
	}

	public void setServer(PlayerID pID) {
		server = pID;
	}

	public GameID getID() {
		return game;
	}
	
	public String getFilename()
	{
		return null;
	}
	
	public int getInitialActionPoints()
	{
		return -1;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof GameInfo)
		{
			return server.equals(((GameInfo)other).server) && game.equals(((GameInfo)other).game);
		}
		else
		{
			return false;
		}
	}
}
