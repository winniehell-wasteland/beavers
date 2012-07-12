package org.beavers.gameplay;

import android.os.Parcel;
import android.os.Parcelable;

public class OutcomeContainer implements Parcelable {

	/** tag for JSON files */
	public static final String JSON_TAG = "outcome";

	public OutcomeContainer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel pOut, final int pFlags) {
		// TODO Auto-generated constructor stub
	}

	public static final Parcelable.Creator<OutcomeContainer> CREATOR =
		new Parcelable.Creator<OutcomeContainer>() {
			@Override
			public OutcomeContainer createFromParcel(final Parcel parcel) {
				return new OutcomeContainer(parcel);
			}

			@Override
			public OutcomeContainer[] newArray(final int size) {
				return new OutcomeContainer[size];
			}
	};

	private OutcomeContainer(final Parcel parcel) {
		// TODO Auto-generated constructor stub
	}

}
