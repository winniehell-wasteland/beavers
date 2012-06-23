package org.beavers.gameplay;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a game
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class GameInfo implements Parcelable {

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
	public int describeContents() {
        return 0;
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

	/** @return true if game is in given state */
	public boolean isInState(final GameState pState) {
		return getState().equals(pState);
	}

	/** @return true if given player is server of the game */
	public boolean isServer(final PlayerID pPlayer) {
		return getServer().equals(pPlayer);
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

	public Object toJSON() {
		return server.toString()+"/"+game.toString();
	}

	@Override
	public String toString() {
		return server.toString()+"/"+game.toString();
	}

    @Override
	public void writeToParcel(final Parcel out, final int flags) {
        out.writeSerializable(game);
        out.writeSerializable(server);
        out.writeSerializable(state);
    }

    public static final Parcelable.Creator<GameInfo> CREATOR
            = new Parcelable.Creator<GameInfo>() {
        @Override
		public GameInfo createFromParcel(final Parcel in) {
            return new GameInfo(in);
        }

        @Override
		public GameInfo[] newArray(final int size) {
            return new GameInfo[size];
        }
    };

	public static GameInfo fromJSON(final Object pJSON) {
		if((pJSON instanceof String))
		{
			final String[] parts = ((String) pJSON).split("/");

			if(parts.length == 2)
			{
				return new GameInfo(new PlayerID(parts[0]), new GameID(parts[1]));
			}
		}

		return null;
	}

	/** unique game ID on server */
	private final GameID game;
	/** server of the game */
	private PlayerID server;
	/** state of the game */
	private GameState state;

    private GameInfo(final Parcel in) {
		game = (GameID) in.readSerializable();
    	server = (PlayerID) in.readSerializable();
		state = (GameState) in.readSerializable();
    }
}
