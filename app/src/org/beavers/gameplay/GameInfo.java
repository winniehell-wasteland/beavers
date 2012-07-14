package org.beavers.gameplay;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * class to uniquely identify a game and store its state
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class GameInfo implements Parcelable {

	/** name for parcel in intent extras */
	public static final String PARCEL_NAME = GameInfo.class.getName();

	/** tag for JSON files */
	public static final String JSON_TAG = "gameinfo";

	/** {@link #JSON_TAG} for game server */
	public static final String JSON_TAG_SERVER = "server";

	/**
	 * default constructor
	 * @param pServer server of the game
	 * @param pGame unique game ID on server
	 */
	public GameInfo(final Player pServer, final Game pID) {
		game = pID;
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

	/** @return server of the game */
	public Player getServer() {
		return server;
	}

	/** @return current game state */
	public GameState getState() {
		return state;
	}

	/** @return team for player */
	public int getTeam(final Player pPlayer)
	{
		return isServer(pPlayer)?0:1;
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
    @SerializedName(Game.JSON_TAG)
	private final Game game;
	/** server of the game */
    @SerializedName(JSON_TAG_SERVER)
	private final Player server;
	/** state of the game */
    @SerializedName(GameState.JSON_TAG)
	private GameState state;

    private GameInfo(final Parcel in) {
		game = in.readParcelable(Game.class.getClassLoader());
    	server = in.readParcelable(Player.class.getClassLoader());
		state = in.readParcelable(GameState.class.getClassLoader());
    }
}
