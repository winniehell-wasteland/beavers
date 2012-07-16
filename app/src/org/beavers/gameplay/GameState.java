package org.beavers.gameplay;

import org.beavers.R;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * represents the state a game is in
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public enum GameState implements Parcelable {
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

	/** tag for JSON files */
	public static final String JSON_TAG = "state";

	@Override
	public int describeContents() {
		return 0;
	}

	public int getResId()
	{
		switch (this) {
		case UNKNOWN:
			return R.string.state_unknown;
		case ANNOUNCED:
			return R.string.state_announced;
		case JOINED:
			return R.string.state_joined;
		case PLANNING_PHASE:
			return R.string.state_planning;
		case EXECUTION_PHASE:
			return R.string.state_execution;
		case ABORTED:
			return R.string.state_aborted;
		case WON:
			return R.string.state_won;
		case LOST:
			return R.string.state_lost;
		default:
			return 0;
		}
	}

	@Override
	public void writeToParcel(final Parcel pOut, final int pFlags) {
		pOut.writeString(name());
	}

    public static final Parcelable.Creator<GameState> CREATOR
            = new Parcelable.Creator<GameState>() {
        @Override
		public GameState createFromParcel(final Parcel in) {
            return GameState.valueOf(in.readString());
        }

        @Override
		public GameState[] newArray(final int size) {
            return new GameState[size];
        }
    };
}
