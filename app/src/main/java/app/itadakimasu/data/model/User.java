package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String uuid;
    private String username;
    private String photoUri;

    public User(String uuid, String username, String photoUri) {
        this.uuid = uuid;
        this.username = username;
        this.photoUri = photoUri;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }


    // AUTO-GENERATED PARCELABLE //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.username);
        dest.writeString(this.photoUri);
    }

    public void readFromParcel(Parcel source) {
        this.uuid = source.readString();
        this.username = source.readString();
        this.photoUri = source.readString();
    }

    protected User(Parcel in) {
        this.uuid = in.readString();
        this.username = in.readString();
        this.photoUri = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
