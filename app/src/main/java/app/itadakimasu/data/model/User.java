package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for user documents on its firebase collection.
 */
public class User implements Parcelable {
    private String uuid;
    private String username;
    private String photoUrl;

    /**
     * Sets user's id, username and its photo url reference.
     * @param uuid - the authentication id granted when registered.
     * @param username - the username settled by the user.
     * @param photoUrl - the photo's path where the image will be stored in firebase storage.
     */
    public User(String uuid, String username, String photoUrl) {
        this.uuid = uuid;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    /**
     * Sets user's id and username.
     * @param uuid - the authentication id granted when registered.
     * @param username - the username settled by the user.
     */
    public User(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.photoUrl = "";
    }

    /**
     * Default constructor that is required for firebase.
     */
    public User() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // Auto generated parcelable //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.username);
        dest.writeString(this.photoUrl);
    }

    public void readFromParcel(Parcel source) {
        this.uuid = source.readString();
        this.username = source.readString();
        this.photoUrl = source.readString();
    }

    protected User(Parcel in) {
        this.uuid = in.readString();
        this.username = in.readString();
        this.photoUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<>() {
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
