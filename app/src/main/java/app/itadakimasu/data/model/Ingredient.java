package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Ingredient implements Parcelable {
    private int ingredientPosition;
    private String ingredientDescription;

    public Ingredient(int ingredientPosition, String ingredientDescription) {
        this.ingredientPosition = ingredientPosition;
        this.ingredientDescription = ingredientDescription;
    }

    public Ingredient(String ingredientDescription) {
        this.ingredientDescription = ingredientDescription;
    }

    public int getIngredientPosition() {
        return ingredientPosition;
    }

    public String getIngredientDescription() {
        return ingredientDescription;
    }

    public void setIngredientPosition(int ingredientPosition) {
        this.ingredientPosition = ingredientPosition;
    }

    public void setIngredientDescription(String ingredientDescription) {
        this.ingredientDescription = ingredientDescription;
    }

    public Ingredient() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(ingredientDescription, that.ingredientDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientDescription);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ingredientPosition);
        dest.writeString(this.ingredientDescription);
    }

    public void readFromParcel(Parcel source) {
        this.ingredientPosition = source.readInt();
        this.ingredientDescription = source.readString();
    }

    protected Ingredient(Parcel in) {
        this.ingredientPosition = in.readInt();
        this.ingredientDescription = in.readString();
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel source) {
            return new Ingredient(source);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };
}
