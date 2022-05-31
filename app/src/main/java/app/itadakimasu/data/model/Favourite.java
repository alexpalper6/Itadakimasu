package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Objects;

public class Favourite implements Parcelable {
    private String id;
    private String username;
    private String recipeId;
    @ServerTimestamp
    private Date additionDate;


    public Favourite(String username, String recipeId) {
        this.username = username;
        this.recipeId = recipeId;
    }

    public Favourite() {
    }

    public Favourite(String id, String username, String recipeId) {
        this.id = id;
        this.username = username;
        this.recipeId = recipeId;
    }

    public String getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public Date getAdditionDate() {
        return additionDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void setAdditionDate(Date additionDate) {
        this.additionDate = additionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favourite favourite = (Favourite) o;
        return Objects.equals(id, favourite.id) && Objects.equals(username, favourite.username) && Objects.equals(recipeId, favourite.recipeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, recipeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.username);
        dest.writeString(this.recipeId);
        dest.writeLong(this.additionDate != null ? this.additionDate.getTime() : -1);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.username = source.readString();
        this.recipeId = source.readString();
        long tmpAdditionDate = source.readLong();
        this.additionDate = tmpAdditionDate == -1 ? null : new Date(tmpAdditionDate);
    }

    protected Favourite(Parcel in) {
        this.id = in.readString();
        this.username = in.readString();
        this.recipeId = in.readString();
        long tmpAdditionDate = in.readLong();
        this.additionDate = tmpAdditionDate == -1 ? null : new Date(tmpAdditionDate);
    }

    public static final Creator<Favourite> CREATOR = new Creator<Favourite>() {
        @Override
        public Favourite createFromParcel(Parcel source) {
            return new Favourite(source);
        }

        @Override
        public Favourite[] newArray(int size) {
            return new Favourite[size];
        }
    };
}
