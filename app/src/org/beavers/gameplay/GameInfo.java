package org.beavers.gameplay;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a game and store its state
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class GameInfo implements Parcelable {

	/** name for parcel in intent extras */
	public static final String parcelName = GameInfo.class.getName();

	/**
	 * default constructor
	 * @param pServer server of the game
	 * @param pGame unique game ID on server
	 */
	public GameInfo(final Player pServer, final Game pID,
			final String pMap) {
		game = pID;
		map = pMap;
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

	/** @return unique game on server */
	public Game getGame() {
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
		return map;
	}

	/** @return server of the game */
	public Player getServer() {
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
	public boolean isServer(final Player pPlayer) {
		return getServer().equals(pPlayer);
	}

	/**
	 * changes the server
	 * @param pServer new server
	 */
	public void setServer(final Player pServer) {
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

    @Override
	public void writeToParcel(final Parcel pOut, final int pFlags) {
    	pOut.writeParcelable(game, pFlags);
    	pOut.writeString(map);
    	pOut.writeParcelable(server, pFlags);
    	pOut.writeParcelable(state, pFlags);
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

	/** unique game on server */
	private final Game game;
	/** map name */
	private final String map;
	/** server of the game */
	private Player server;
	/** state of the game */
	private GameState state;

    private GameInfo(final Parcel in) {
		game = in.readParcelable(Game.class.getClassLoader());
		map = in.readString();
    	server = in.readParcelable(Player.class.getClassLoader());
		state = in.readParcelable(GameState.class.getClassLoader());
    }
}
