package org.beavers.gameplay;

/**
 * @author winniehell
 * class to uniquely identify a game
 */
public class GameInfo {

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
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof GameInfo)
		{
			return (server == ((GameInfo)other).server) && (game == ((GameInfo)other).game);
		}
		else
		{
			return false;
		}
	}
}
