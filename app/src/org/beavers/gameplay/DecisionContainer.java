package org.beavers.gameplay;

import android.os.Parcel;
import android.os.Parcelable;

public class DecisionContainer implements Parcelable {
	public DecisionContainer() {
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

	public static final Parcelable.Creator<DecisionContainer> CREATOR =
		new Parcelable.Creator<DecisionContainer>() {
			@Override
			public DecisionContainer createFromParcel(final Parcel parcel) {
				return new DecisionContainer(parcel);
			}

			@Override
			public DecisionContainer[] newArray(final int size) {
				return new DecisionContainer[size];
			}
	};

	private DecisionContainer(final Parcel parcel) {
		// TODO Auto-generated constructor stub
	}
}
