package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;

import java.util.Objects;

public class Recipe implements Parcelable {
    private String author;
    private String photoAuthorUrl;

    private String title;
    private String description;
    private String photoUrl;
    private FieldValue creationDate;
    @Exclude
    private boolean isFavourite;

    public Recipe(String author, String photoAuthorUrl, String title, String description, String photoUrl, FieldValue creationDate) {
        this.author = author;
        this.photoAuthorUrl = photoAuthorUrl;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.creationDate = creationDate;
        this.isFavourite = false;
    }

    public Recipe(String author, String photoAuthorUrl, String title, String description, String photoUrl, FieldValue creationDate, boolean isFavourite) {
        this.author = author;
        this.photoAuthorUrl = photoAuthorUrl;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.creationDate = creationDate;
        this.isFavourite = isFavourite;
    }

    public Recipe(String author, String photoAuthorUrl, String title, String description) {
        this.author = author;
        this.photoAuthorUrl = photoAuthorUrl;
        this.title = title;
        this.description = description;
    }

    public Recipe() {
    }

    public String getAuthor() {
        return author;
    }

    public String getPhotoAuthorUrl() {
        return photoAuthorUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public FieldValue getCreationDate() {
        return creationDate;
    }

    @Exclude
    public Boolean isFavourite() {
        return isFavourite;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPhotoAuthorUrl(String photoAuthorUrl) {
        this.photoAuthorUrl = photoAuthorUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setCreationDate(FieldValue creationDate) {
        this.creationDate = creationDate;
    }

    @Exclude
    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(author, recipe.author)
                && Objects.equals(photoAuthorUrl, recipe.photoAuthorUrl)
                && Objects.equals(title, recipe.title)
                && Objects.equals(description, recipe.description)
                && Objects.equals(photoUrl, recipe.photoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, photoAuthorUrl, title, description, photoUrl);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.photoAuthorUrl);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.photoUrl);
        dest.writeParcelable((Parcelable) this.creationDate, flags);
        dest.writeByte(this.isFavourite ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.author = source.readString();
        this.photoAuthorUrl = source.readString();
        this.title = source.readString();
        this.description = source.readString();
        this.photoUrl = source.readString();
        this.creationDate = source.readParcelable(FieldValue.class.getClassLoader());
        this.isFavourite = source.readByte() != 0;
    }

    protected Recipe(Parcel in) {
        this.author = in.readString();
        this.photoAuthorUrl = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.photoUrl = in.readString();
        this.creationDate = in.readParcelable(FieldValue.class.getClassLoader());
        this.isFavourite = in.readByte() != 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel source) {
            return new Recipe(source);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };
}
