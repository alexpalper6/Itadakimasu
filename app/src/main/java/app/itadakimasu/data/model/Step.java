package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Step implements Parcelable {
    private int stepPosition;
    private String stepDescription;


    public Step(int stepPosition, String stepDescription) {
        this.stepPosition = stepPosition;
        this.stepDescription = stepDescription;
    }

    public Step(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public int getStepPosition() {
        return stepPosition;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepPosition(int stepPosition) {
        this.stepPosition = stepPosition;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(stepDescription, step.stepDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepDescription);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stepPosition);
        dest.writeString(this.stepDescription);
    }

    public void readFromParcel(Parcel source) {
        this.stepPosition = source.readInt();
        this.stepDescription = source.readString();
    }

    protected Step(Parcel in) {
        this.stepPosition = in.readInt();
        this.stepDescription = in.readString();
    }

    public static final Parcelable.Creator<Step> CREATOR = new Parcelable.Creator<Step>() {
        @Override
        public Step createFromParcel(Parcel source) {
            return new Step(source);
        }

        @Override
        public Step[] newArray(int size) {
            return new Step[size];
        }
    };
}
